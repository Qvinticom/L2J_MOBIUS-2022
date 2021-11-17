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
package ai.bosses;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.quest.EventType;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

/**
 * @author Shyla
 */
public class Orfen extends Quest
{
	private static final int ORFEN = 29014;
	private static final int LIVE = 0;
	private static final int DEAD = 1;
	
	private boolean _firstAttacked = false;
	private boolean _teleported = false;
	
	GrandBoss _orfen = null;
	
	enum Event
	{
		ORFEN_SPAWN,
		ORFEN_REFRESH,
		ORFEN_RETURN
	}
	
	public Orfen()
	{
		super(-1, "ai/bosses");
		
		final StatSet info = GrandBossManager.getInstance().getStatSet(ORFEN);
		final Integer status = GrandBossManager.getInstance().getBossStatus(ORFEN);
		
		addEventId(ORFEN, EventType.ON_KILL);
		addEventId(ORFEN, EventType.ON_ATTACK);
		
		switch (status)
		{
			case DEAD:
			{
				final long temp = info.getLong("respawn_time") - Chronos.currentTimeMillis();
				if (temp > 0)
				{
					startQuestTimer("ORFEN_SPAWN", temp, null, null);
				}
				else
				{
					_orfen = (GrandBoss) addSpawn(ORFEN, 55024, 17368, -5412, 0, false, 0);
					if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
					{
						AnnouncementsTable.getInstance().announceToAll("Raid boss " + _orfen.getName() + " spawned in world.");
					}
					GrandBossManager.getInstance().setBossStatus(ORFEN, LIVE);
					GrandBossManager.getInstance().addBoss(_orfen);
				}
				break;
			}
			case LIVE:
			{
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_orfen = (GrandBoss) addSpawn(ORFEN, 55024, 17368, -5412, 0, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					AnnouncementsTable.getInstance().announceToAll("Raid boss " + _orfen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().addBoss(_orfen);
				_orfen.setCurrentHpMp(hp, mp);
				break;
			}
			default:
			{
				final int loc_x = 55024;
				final int loc_y = 17368;
				final int loc_z = -5412;
				final int heading = 0;
				_orfen = (GrandBoss) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					AnnouncementsTable.getInstance().announceToAll("Raid boss " + _orfen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(ORFEN, LIVE);
				GrandBossManager.getInstance().addBoss(_orfen);
				break;
			}
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final Event eventEnum = Event.valueOf(event.toUpperCase());
		
		switch (eventEnum)
		{
			case ORFEN_SPAWN:
			{
				final int loc_x = 55024;
				final int loc_y = 17368;
				final int loc_z = -5412;
				final int heading = 0;
				_orfen = (GrandBoss) addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0);
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					AnnouncementsTable.getInstance().announceToAll("Raid boss " + _orfen.getName() + " spawned in world.");
				}
				GrandBossManager.getInstance().setBossStatus(ORFEN, LIVE);
				GrandBossManager.getInstance().addBoss(_orfen);
				break;
			}
			case ORFEN_REFRESH:
			{
				if ((npc == null) || (npc.getSpawn() == null))
				{
					cancelQuestTimer("ORFEN_REFRESH", npc, null);
					break;
				}
				double hp = -1;
				if (npc.getNpcId() == ORFEN)
				{
					hp = GrandBossManager.getInstance().getStatSet(ORFEN).getDouble("currentHP");
					if (hp < npc.getCurrentHp())
					{
						npc.setCurrentHp(hp);
						GrandBossManager.getInstance().getStatSet(ORFEN).set("currentHP", npc.getMaxHp());
					}
				}
				if ((_teleported && (npc.getCurrentHp() > (npc.getMaxHp() * 0.95))))
				{
					cancelQuestTimer("ORFEN_REFRESH", npc, null);
					startQuestTimer("ORFEN_RETURN", 10000, npc, null);
				}
				else
				{
					// Restart the refresh scheduling.
					startQuestTimer("ORFEN_REFRESH", 10000, npc, null);
				}
				break;
			}
			case ORFEN_RETURN:
			{
				if ((npc == null) || (npc.getSpawn() == null))
				{
					break;
				}
				_teleported = false;
				_firstAttacked = false;
				npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				npc.getSpawn().setX(55024);
				npc.getSpawn().setY(17368);
				npc.getSpawn().setZ(-5412);
				npc.teleToLocation(55024, 17368, -5412);
				break;
			}
			default:
			{
				LOGGER.info("ORFEN: Not defined event: " + event + "!");
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		final int npcId = npc.getNpcId();
		if (npcId == ORFEN)
		{
			if (_firstAttacked)
			{
				if (((npc.getCurrentHp() - damage) < (npc.getMaxHp() / 2)) && !_teleported)
				{
					GrandBossManager.getInstance().getStatSet(ORFEN).set("currentHP", npc.getCurrentHp());
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
					_teleported = true;
					npc.getSpawn().setX(43577);
					npc.getSpawn().setY(15985);
					npc.getSpawn().setZ(-4396);
					npc.teleToLocation(43577, 15985, -4396);
					startQuestTimer("ORFEN_REFRESH", 10000, npc, null);
				}
				else if (npc.isInsideRadius2D(attacker, 1000) && !npc.isInsideRadius2D(attacker, 300) && (Rnd.get(10) == 0))
				{
					attacker.teleToLocation(npc.getX(), npc.getY(), npc.getZ());
					npc.setTarget(attacker);
					npc.doCast(SkillTable.getInstance().getSkill(4064, 1));
				}
			}
			else
			{
				_firstAttacked = true;
			}
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if (npc.getNpcId() == ORFEN)
		{
			npc.broadcastPacket(new PlaySound(1, "BS02_D", npc));
			GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD);
			// Time is 48hour +/- 20hour.
			final long respawnTime = (Config.ORFEN_RESP_FIRST + Rnd.get(Config.ORFEN_RESP_SECOND)) * 3600000;
			cancelQuestTimer("ORFEN_REFRESH", npc, null);
			startQuestTimer("ORFEN_SPAWN", respawnTime, null, null);
			// Also save the respawn time so that the info is maintained past restarts.
			final StatSet info = GrandBossManager.getInstance().getStatSet(ORFEN);
			info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(ORFEN, info);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Orfen();
	}
}