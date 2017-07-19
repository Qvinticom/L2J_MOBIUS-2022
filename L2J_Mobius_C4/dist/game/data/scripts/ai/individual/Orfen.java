/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.individual;

import java.util.List;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.zone.type.L2BossZone;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * Orfen AI
 * @author Emperorc
 */
public class Orfen extends Quest
{
	private static final int[][] Pos =
	{
		{
			43728,
			17220,
			-4342
		},
		{
			55024,
			17368,
			-5412
		},
		{
			53504,
			21248,
			-5486
		},
		{
			53248,
			24576,
			-5262
		}
	};
	
	private static final String[] Text =
	{
		"PLAYERNAME. Stop kidding yourself about your own powerlessness!",
		"PLAYERNAME. I'll make you feel what true fear is!",
		"You're really stupid to have challenged me. PLAYERNAME! Get ready!",
		"PLAYERNAME. Do you think that's going to work?!"
	};
	
	private static final int ORFEN = 12169;
	private static final int RAIKEL_LEOS = 12171;
	private static final int RIBA_IREN = 12173;
	
	private static boolean _IsTeleported;
	private static List<L2Attackable> _Minions = new FastList<>();
	private static L2BossZone _Zone;
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	public Orfen(int id, String name, String descr)
	{
		super(id, name, descr);
		final int[] mobs =
		{
			ORFEN,
			RAIKEL_LEOS,
			RIBA_IREN
		};
		registerMobs(mobs);
		_IsTeleported = false;
		_Zone = GrandBossManager.getInstance().getZone(Pos[0][0], Pos[0][1], Pos[0][2]);
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
		final int status = GrandBossManager.getInstance().getBossStatus(ORFEN);
		if (status == DEAD)
		{
			// load the unlock date and time for Orfen from DB
			final long temp = info.getLong("respawn_time") - System.currentTimeMillis();
			// if Orfen is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if (temp > 0)
			{
				startQuestTimer("orfen_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline. Immediately spawn Orfen.
				final int i = Rnd.get(10);
				int x = 0;
				int y = 0;
				int z = 0;
				if (i < 4)
				{
					x = Pos[1][0];
					y = Pos[1][1];
					z = Pos[1][2];
				}
				else if (i < 7)
				{
					x = Pos[2][0];
					y = Pos[2][1];
					z = Pos[2][2];
				}
				else
				{
					x = Pos[3][0];
					y = Pos[3][1];
					z = Pos[3][2];
				}
				
				final L2GrandBossInstance orfen = (L2GrandBossInstance) addSpawn(ORFEN, x, y, z, 0, false, 0);
				GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
				spawnBoss(orfen);
			}
		}
		else
		{
			final int loc_x = info.getInteger("loc_x");
			final int loc_y = info.getInteger("loc_y");
			final int loc_z = info.getInteger("loc_z");
			final int heading = info.getInteger("heading");
			final int hp = info.getInteger("currentHP");
			final int mp = info.getInteger("currentMP");
			final L2GrandBossInstance orfen = (L2GrandBossInstance) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
			orfen.setCurrentHpMp(hp, mp);
			spawnBoss(orfen);
		}
	}
	
	public void setSpawnPoint(L2NpcInstance npc, int index)
	{
		((L2Attackable) npc).clearAggroList();
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
		final L2Spawn spawn = npc.getSpawn();
		spawn.setLocx(Pos[index][0]);
		spawn.setLocy(Pos[index][1]);
		spawn.setLocz(Pos[index][2]);
		npc.teleToLocation(Pos[index][0], Pos[index][1], Pos[index][2]);
	}
	
	public void spawnBoss(L2GrandBossInstance npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		startQuestTimer("check_orfen_pos", 10000, npc, null, true);
		
		// Spawn minions
		final int x = npc.getX();
		final int y = npc.getY();
		_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x + 100, y + 100, npc.getZ(), 0, false, 0));
		_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x + 100, y - 100, npc.getZ(), 0, false, 0));
		_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x - 100, y + 100, npc.getZ(), 0, false, 0));
		_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, x - 100, y - 100, npc.getZ(), 0, false, 0));
		startQuestTimer("check_minion_loc", 10000, npc, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, L2NpcInstance npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("orfen_unlock"))
		{
			final int i = Rnd.get(10);
			int x = 0;
			int y = 0;
			int z = 0;
			if (i < 4)
			{
				x = Pos[1][0];
				y = Pos[1][1];
				z = Pos[1][2];
			}
			else if (i < 7)
			{
				x = Pos[2][0];
				y = Pos[2][1];
				z = Pos[2][2];
			}
			else
			{
				x = Pos[3][0];
				y = Pos[3][1];
				z = Pos[3][2];
			}
			
			final L2GrandBossInstance orfen = (L2GrandBossInstance) addSpawn(ORFEN, x, y, z, 0, false, 0);
			GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
			spawnBoss(orfen);
		}
		else if (event.equalsIgnoreCase("check_orfen_pos"))
		{
			if ((_IsTeleported && (npc.getCurrentHp() > (npc.getMaxHp() * 0.95))) || (!_Zone.isInsideZone(npc) && !_IsTeleported))
			{
				setSpawnPoint(npc, Rnd.get(3) + 1);
				_IsTeleported = false;
			}
			else if (_IsTeleported && !_Zone.isInsideZone(npc))
			{
				setSpawnPoint(npc, 0);
			}
		}
		else if (event.equalsIgnoreCase("check_minion_loc"))
		{
			for (int i = 0; i < _Minions.size(); i++)
			{
				final L2Attackable mob = _Minions.get(i);
				if (!npc.isInsideRadius(mob, 3000, false, false))
				{
					mob.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					((L2Attackable) npc).clearAggroList();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (int i = 0; i < _Minions.size(); i++)
			{
				final L2Attackable mob = _Minions.get(i);
				if (mob != null)
				{
					mob.decayMe();
				}
			}
			_Minions.clear();
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			_Minions.add((L2Attackable) addSpawn(RAIKEL_LEOS, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0));
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSkillSee(L2NpcInstance npc, L2PcInstance caster, L2Skill skill, L2Object[] targets, boolean isPet)
	{
		if (npc.getNpcId() == ORFEN)
		{
			final L2Character originalCaster = isPet ? caster.getPet() : caster;
			if (skill.hasAggro() && (Rnd.get(5) == 0) && npc.isInsideRadius(originalCaster, 1000, false, false))
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), Text[Rnd.get(4)].replace("PLAYERNAME", caster.getName().toString())));
				originalCaster.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				npc.setTarget(originalCaster);
				npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
			}
		}
		return super.onSkillSee(npc, caster, skill, targets, isPet);
	}
	
	@Override
	public String onFactionCall(L2NpcInstance npc, L2NpcInstance caller, L2PcInstance attacker, boolean isPet)
	{
		if ((caller == null) || (npc == null))
		{
			return super.onFactionCall(npc, caller, attacker, isPet);
		}
		
		final int npcId = npc.getNpcId();
		final int callerId = caller.getNpcId();
		if ((npcId == RAIKEL_LEOS) && (Rnd.get(20) == 0))
		{
			npc.setTarget(attacker);
			npc.doCast(SkillTable.getInstance().getInfo(4067, 4));
		}
		else if (npcId == RIBA_IREN)
		{
			int chance = 1;
			if (callerId == ORFEN)
			{
				chance = 9;
			}
			if ((callerId != RIBA_IREN) && (caller.getCurrentHp() < (caller.getMaxHp() / 2)) && (Rnd.get(10) < chance))
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				npc.setTarget(caller);
				npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
			}
		}
		return super.onFactionCall(npc, caller, attacker, isPet);
	}
	
	@Override
	public String onAttack(L2NpcInstance npc, L2PcInstance attacker, int damage, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (npcId == ORFEN)
		{
			if (((npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2)) && !_IsTeleported)
			{
				setSpawnPoint(npc, 0);
				_IsTeleported = true;
			}
			else if (npc.isInsideRadius(attacker, 1000, false, false) && !npc.isInsideRadius(attacker, 300, false, false) && (Rnd.get(10) == 0))
			{
				npc.broadcastPacket(new CreatureSay(npc.getObjectId(), 0, npc.getName(), Text[Rnd.get(3)].replace("PLAYERNAME", attacker.getName().toString())));
				attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4064, 1));
			}
		}
		else if (npcId == RIBA_IREN)
		{
			if ((npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2))
			{
				npc.setTarget(attacker);
				npc.doCast(SkillTable.getInstance().getInfo(4516, 1));
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(L2NpcInstance npc, L2PcInstance killer, boolean isPet)
	{
		if (npc.getNpcId() == ORFEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD);
			// time is 48hour +/- 20hour
			final long respawnTime = (28 + (Rnd.get(41) * 3600000));
			startQuestTimer("orfen_unlock", respawnTime, null, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(ORFEN);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(ORFEN, info);
			cancelQuestTimer("check_minion_loc", npc, null);
			cancelQuestTimer("check_orfen_pos", npc, null);
			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if ((GrandBossManager.getInstance().getBossStatus(ORFEN) == ALIVE) && (npc.getNpcId() == RAIKEL_LEOS))
		{
			_Minions.remove(npc);
			startQuestTimer("spawn_minion", 360000, npc, null);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		// Quest class and state definition
		new Orfen(-1, "orfen", "ai");
	}
}