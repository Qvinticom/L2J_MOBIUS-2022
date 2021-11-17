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
package ai.bosses.Orfen;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.skills.SkillCaster;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Orfen's AI
 * @author Emperorc
 */
public class Orfen extends AbstractNpcAI
{
	private static final Location POS = new Location(43728, 17220, -4342);
	
	private static final NpcStringId[] TEXT =
	{
		NpcStringId.S1_STOP_KIDDING_YOURSELF_ABOUT_YOUR_OWN_POWERLESSNESS,
		NpcStringId.S1_I_LL_MAKE_YOU_FEEL_WHAT_TRUE_FEAR_IS,
		NpcStringId.YOU_RE_REALLY_STUPID_TO_HAVE_CHALLENGED_ME_S1_GET_READY,
		NpcStringId.S1_DO_YOU_THINK_THAT_S_GOING_TO_WORK
	};
	
	private static final int ORFEN = 29325;
	private static final int ARIMA = 29326;
	private static final int ARIMUS = 29327;
	
	private static Set<Attackable> _minions = ConcurrentHashMap.newKeySet();
	
	private static final byte ALIVE = 0;
	private static final byte DEAD = 1;
	
	private static final SkillHolder BLOW = new SkillHolder(4067, 4);
	private static final SkillHolder ORFEN_HEAL = new SkillHolder(4516, 1);
	private static final SkillHolder ORFEN_SLASHER = new SkillHolder(32486, 1);
	private static final SkillHolder ORFEN_FATAL_SLASHER = new SkillHolder(32487, 1);
	private static final SkillHolder ORFEN_ENERGY_SCATTER = new SkillHolder(32488, 1);
	private static final SkillHolder ORFEN_FURY_ENERGY_WAVE = new SkillHolder(32489, 1);
	private static final SkillHolder YOKE_OF_ORFEN = new SkillHolder(32490, 1);
	private static final SkillHolder ORFEN_BLOW_UP = new SkillHolder(32491, 1);
	private static final SkillHolder ORFEN_FATAL_STAMP = new SkillHolder(32492, 1);
	private static final SkillHolder ORFEN_RAISE_SPORE = new SkillHolder(32493, 1);
	private static final SkillHolder HALLUCINATING_DUST = new SkillHolder(32494, 1);
	private static final SkillHolder ORFEN_RAGE = new SkillHolder(32495, 1);
	
	private Orfen()
	{
		final int[] mobs =
		{
			ORFEN,
			ARIMA,
			ARIMUS
		};
		registerMobs(mobs);
		final StatSet info = GrandBossManager.getInstance().getStatSet(ORFEN);
		final int status = GrandBossManager.getInstance().getBossStatus(ORFEN);
		if (status == DEAD)
		{
			// load the unlock date and time for Orfen from DB
			final long temp = info.getLong("respawn_time") - Chronos.currentTimeMillis();
			// if Orfen is locked until a certain time, mark it so and start the unlock timer
			// the unlock time has not yet expired.
			if (temp > 0)
			{
				startQuestTimer("orfen_unlock", temp, null, null);
			}
			else
			{
				// the time has already expired while the server was offline. Immediately spawn Orfen.
				final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, POS, false, 0);
				GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
				spawnBoss(orfen);
			}
		}
		else
		{
			final int loc_x = info.getInt("loc_x");
			final int loc_y = info.getInt("loc_y");
			final int loc_z = info.getInt("loc_z");
			final int heading = info.getInt("heading");
			final double hp = info.getDouble("currentHP");
			final double mp = info.getDouble("currentMP");
			final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
			orfen.setCurrentHpMp(hp, mp);
			spawnBoss(orfen);
		}
	}
	
	public void spawnBoss(GrandBoss npc)
	{
		GrandBossManager.getInstance().addBoss(npc);
		npc.broadcastPacket(new PlaySound(1, "BS01_A", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
		// Spawn minions
		final int x = npc.getX();
		final int y = npc.getY();
		Attackable mob;
		mob = (Attackable) addSpawn(ARIMA, x + 100, y + 100, npc.getZ(), 0, false, 0);
		mob.setIsRaidMinion(true);
		_minions.add(mob);
		mob = (Attackable) addSpawn(ARIMA, x + 100, y - 100, npc.getZ(), 0, false, 0);
		mob.setIsRaidMinion(true);
		_minions.add(mob);
		mob = (Attackable) addSpawn(ARIMA, x - 100, y + 100, npc.getZ(), 0, false, 0);
		mob.setIsRaidMinion(true);
		_minions.add(mob);
		mob = (Attackable) addSpawn(ARIMA, x - 100, y - 100, npc.getZ(), 0, false, 0);
		mob.setIsRaidMinion(true);
		_minions.add(mob);
		startQuestTimer("check_minion_loc", 10000, npc, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("orfen_unlock"))
		{
			final GrandBoss orfen = (GrandBoss) addSpawn(ORFEN, POS, false, 0);
			GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE);
			spawnBoss(orfen);
		}
		else if (event.equalsIgnoreCase("check_minion_loc"))
		{
			for (Attackable mob : _minions)
			{
				if (!npc.isInsideRadius2D(mob, 3000))
				{
					mob.teleToLocation(npc.getLocation());
					((Attackable) npc).clearAggroList();
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				}
			}
		}
		else if (event.equalsIgnoreCase("despawn_minions"))
		{
			for (Attackable mob : _minions)
			{
				mob.decayMe();
			}
			_minions.clear();
		}
		else if (event.equalsIgnoreCase("spawn_minion"))
		{
			final Attackable mob = (Attackable) addSpawn(ARIMA, npc.getX(), npc.getY(), npc.getZ(), 0, false, 0);
			mob.setIsRaidMinion(true);
			_minions.add(mob);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFactionCall(Npc npc, Npc caller, Player attacker, boolean isSummon)
	{
		if ((caller == null) || (npc == null) || npc.isCastingNow(SkillCaster::isAnyNormalType))
		{
			return super.onFactionCall(npc, caller, attacker, isSummon);
		}
		final int npcId = npc.getId();
		final int callerId = caller.getId();
		if ((npcId == ARIMA) && (getRandom(20) == 0))
		{
			npc.setTarget(attacker);
			npc.doCast(BLOW.getSkill());
		}
		else if (npcId == ARIMUS)
		{
			int chance = 1;
			if (callerId == ORFEN)
			{
				chance = 9;
			}
			if ((callerId != ARIMUS) && (caller.getCurrentHp() < (caller.getMaxHp() / 2.0)) && (getRandom(10) < chance))
			{
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
				npc.setTarget(caller);
				npc.doCast(ORFEN_HEAL.getSkill());
			}
		}
		return super.onFactionCall(npc, caller, attacker, isSummon);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		final int npcId = npc.getId();
		if (npcId == ORFEN)
		{
			if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_FATAL_SLASHER.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_ENERGY_SCATTER.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_FURY_ENERGY_WAVE.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(YOKE_OF_ORFEN.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_BLOW_UP.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_FATAL_STAMP.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_RAISE_SPORE.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(HALLUCINATING_DUST.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000) && (getRandom(10) == 0) && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_RAGE.getSkill());
			}
			else if (manageSkills(npc) && npc.isInsideRadius2D(attacker, 1000))
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, TEXT[getRandom(3)], attacker.getName());
				npc.setTarget(attacker);
				npc.doCast(ORFEN_SLASHER.getSkill());
			}
		}
		else if (npcId == ARIMUS)
		{
			if (!npc.isCastingNow(SkillCaster::isAnyNormalType) && ((npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2.0)))
			{
				npc.setTarget(attacker);
				npc.doCast(ORFEN_HEAL.getSkill());
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	private boolean manageSkills(Npc npc)
	{
		if (npc.isCastingNow())
		{
			return false;
		}
		return true;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (npc.getId() == ORFEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", 1, npc.getObjectId(), npc.getX(), npc.getY(), npc.getZ()));
			GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD);
			// Calculate Min and Max respawn times randomly.
			long respawnTime = Config.ORFEN_SPAWN_INTERVAL + getRandom(-Config.ORFEN_SPAWN_RANDOM, Config.ORFEN_SPAWN_RANDOM);
			respawnTime *= 3600000;
			startQuestTimer("orfen_unlock", respawnTime, null, null);
			// also save the respawn time so that the info is maintained past reboots
			final StatSet info = GrandBossManager.getInstance().getStatSet(ORFEN);
			info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(ORFEN, info);
			cancelQuestTimer("check_minion_loc", npc, null);
			startQuestTimer("despawn_minions", 20000, null, null);
			cancelQuestTimers("spawn_minion");
		}
		else if ((GrandBossManager.getInstance().getBossStatus(ORFEN) == ALIVE) && (npc.getId() == ARIMA))
		{
			_minions.remove(npc);
			startQuestTimer("spawn_minion", 360000, npc, null);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Orfen();
	}
}
