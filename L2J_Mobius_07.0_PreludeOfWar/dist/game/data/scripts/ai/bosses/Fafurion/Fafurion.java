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

package ai.bosses.Fafurion;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.instancemanager.GlobalVariablesManager;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.zone.type.NoRestartZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Fafurion extends AbstractNpcAI
{
	// NPCs
	private static final int HEART_OF_TSUNAMI = 34488;
	private static final int FAFURION_GRANDBOSS_ID = 19740;
	private static final int FAFURION_STAGE_1 = 29361;
	private static final int FAFURION_STAGE_2 = 29362;
	private static final int FAFURION_STAGE_3 = 29363;
	private static final int FAFURION_STAGE_4 = 29364;
	private static final int FAFURION_STAGE_5 = 29365;
	private static final int FAFURION_STAGE_6 = 29366;
	private static final int FAFURION_STAGE_7 = 29367;
	private static final int EMERALD_DRAGON = 29368;
	private static final int BLUE_WATER_DRAGON = 29369;
	private static final int FAFURION_DEFENDER = 29370;
	// Item
	private static final int FONDUS_STONE = 80322;
	// Zone
	private static final NoRestartZone FAFURION_ZONE = ZoneManager.getInstance().getZoneById(85002, NoRestartZone.class);
	// Locations
	private static final Location RAID_ENTER_LOC = new Location(180059, 212896, -14727);
	private static final Location FAFURION_SPAWN_LOC = new Location(180712, 210664, -14823, 22146);
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int FIGHTING = 2;
	private static final int DEAD = 3;
	// Misc
	private static final int MAX_PEOPLE = 200;
	private static final int RAID_DURATION = 5; // hours
	private static GrandBoss _fafurion;
	private int _stage;
	
	private Fafurion()
	{
		addStartNpc(HEART_OF_TSUNAMI);
		addTalkId(HEART_OF_TSUNAMI);
		addFirstTalkId(HEART_OF_TSUNAMI);
		addKillId(FAFURION_STAGE_1, FAFURION_STAGE_2, FAFURION_STAGE_3, FAFURION_STAGE_4, FAFURION_STAGE_5, FAFURION_STAGE_6, FAFURION_STAGE_7);
		// Unlock
		final StatSet info = GrandBossManager.getInstance().getStatSet(FAFURION_GRANDBOSS_ID);
		final int status = GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID);
		if (status == DEAD)
		{
			final long time = info.getLong("respawn_time") - Chronos.currentTimeMillis();
			if (time > 0)
			{
				startQuestTimer("unlock_fafurion", time, null, null);
			}
			else
			{
				final GrandBoss fafurion = (GrandBoss) addSpawn(FAFURION_GRANDBOSS_ID, -126920, -234182, -15563, 0, false, 0);
				GrandBossManager.getInstance().addBoss(fafurion);
				GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
			}
		}
		else
		{
			final GrandBoss fafurion = (GrandBoss) addSpawn(FAFURION_GRANDBOSS_ID, -126920, -234182, -15563, 0, false, 0);
			GrandBossManager.getInstance().addBoss(fafurion);
			GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "unlock_fafurion":
			{
				final GrandBoss fafurion = (GrandBoss) addSpawn(FAFURION_GRANDBOSS_ID, -126920, -234182, -15563, 0, false, 0);
				GrandBossManager.getInstance().addBoss(fafurion);
				GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
				break;
			}
			case "warning":
			{
				if (player.calculateDistance2D(FAFURION_SPAWN_LOC) < 5000)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.ALL_WHO_FEAR_OF_FAFURION_LEAVE_THIS_PLACE_AT_ONCE, ExShowScreenMessage.TOP_CENTER, 10000, true));
					for (Player plr : World.getInstance().getVisibleObjectsInRange(player, Player.class, 5000))
					{
						plr.sendPacket(new ExShowScreenMessage(NpcStringId.ALL_WHO_FEAR_OF_FAFURION_LEAVE_THIS_PLACE_AT_ONCE, ExShowScreenMessage.TOP_CENTER, 10000, true));
					}
				}
				break;
			}
			case "beginning":
			{
				if (GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID) == WAITING)
				{
					// checking fafurion stage.
					_stage = GlobalVariablesManager.getInstance().getInt("Fafurion_Stage", 1);
					FAFURION_ZONE.getPlayersInside().forEach(p ->
					{
						playMovie(p, Movie.SC_FAFURION_INTRO);
					});
					GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, FIGHTING);
					switch (_stage)
					{
						case 1:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_1, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
						case 2:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_2, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
						case 3:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_3, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
						case 4:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_4, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
						case 5:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_5, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
						case 6:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_6, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
						case 7:
						{
							_fafurion = (GrandBoss) addSpawn(FAFURION_STAGE_7, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
							break;
						}
					}
					startQuestTimer("resetRaid", RAID_DURATION * 60 * 60 * 1000, _fafurion, null);
				}
				break;
			}
			case "resetRaid":
			{
				final int status = GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID);
				if ((status > ALIVE) && (status < DEAD))
				{
					for (Player plr : World.getInstance().getVisibleObjectsInRange(npc, Player.class, 5000))
					{
						plr.sendPacket(new ExShowScreenMessage(NpcStringId.EXCEEDED_THE_FAFURION_S_NEST_RAID_TIME_LIMIT, ExShowScreenMessage.TOP_CENTER, 10000, true));
					}
					GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
					FAFURION_ZONE.oustAllPlayers();
					npc.deleteMe();
					if (_stage > 1)
					{
						_stage--;
					}
					GlobalVariablesManager.getInstance().set("Fafurion_Stage", _stage);
				}
				break;
			}
			case "enter_area":
			{
				final int status = GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID);
				if (player.isGM())
				{
					player.teleToLocation(RAID_ENTER_LOC, true);
				}
				else
				{
					if (((status > ALIVE) && (status < DEAD)) || (status == DEAD))
					{
						return "34488-02.html";
					}
					if (FAFURION_ZONE.getPlayersInside().size() >= MAX_PEOPLE)
					{
						return "34488-03.html";
					}
					if (!player.isInParty())
					{
						return "34488-01.html";
					}
					final Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<Player> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
					final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
					if (!isPartyLeader)
					{
						return "34488-02.html";
					}
					if ((members.size() < Config.FAFURION_MIN_PLAYERS) || (members.size() > Config.FAFURION_MAX_PLAYERS))
					{
						return "34488-01.html";
					}
					if (members.size() > (MAX_PEOPLE - FAFURION_ZONE.getPlayersInside().size()))
					{
						return "34488-03.html";
					}
					for (Player member : members)
					{
						if (member.getLevel() < Config.FAFURION_MIN_PLAYER_LEVEL)
						{
							return "34488-01.html";
						}
					}
					if (!hasQuestItems(player, FONDUS_STONE))
					{
						// TODO: Retail message.
						player.sendMessage("You need to own a fondus stone.");
						return null;
					}
					takeItems(player, FONDUS_STONE, 1);
					for (Player member : members)
					{
						if ((member.calculateDistance2D(npc) < 1000) && (npc.getId() == HEART_OF_TSUNAMI))
						{
							member.teleToLocation(RAID_ENTER_LOC, true);
						}
					}
				}
				if (status == ALIVE)
				{
					GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, WAITING);
					startQuestTimer("beginning", Config.FAFURION_WAIT_TIME * 60000, null, null);
					startQuestTimer("warning", Config.FAFURION_WAIT_TIME > 0 ? (Config.FAFURION_WAIT_TIME * 60000) - 30000 : 0, null, player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		if (npc.getId() == _fafurion.getId())
		{
			switch (npc.getId())
			{
				// TODO: Manage Rage and feature do decrease rage in rooms.
				case FAFURION_STAGE_1:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
					}
					break;
				}
				case FAFURION_STAGE_2:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
					}
					break;
				}
				case FAFURION_STAGE_3:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						for (int i = 1; i < 3; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						for (int i = 1; i < 3; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					break;
				}
				case FAFURION_STAGE_4:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						for (int i = 1; i < 3; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						for (int i = 1; i < 3; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					break;
				}
				case FAFURION_STAGE_5:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						for (int i = 1; i < 3; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						for (int i = 1; i < 3; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					break;
				}
				case FAFURION_STAGE_6:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						for (int i = 1; i < 4; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						for (int i = 1; i < 4; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					break;
				}
				case FAFURION_STAGE_7:
				{
					if (npc.getCurrentHp() < (npc.getMaxHp() * 0.6))
					{
						for (int i = 1; i < 4; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					else if (npc.getCurrentHp() < (npc.getMaxHp() * 0.3))
					{
						for (int i = 1; i < 4; i++)
						{
							addSpawn(EMERALD_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(BLUE_WATER_DRAGON, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
							addSpawn(FAFURION_DEFENDER, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading(), true, 0);
						}
					}
					break;
				}
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		if (npc.getId() == _fafurion.getId())
		{
			FAFURION_ZONE.getPlayersInside().forEach(p ->
			{
				playMovie(p, Movie.SC_FAFURION_ENDING);
				FAFURION_ZONE.getCharactersInside().forEach(m ->
				{
					if (m.isMonster())
					{
						m.deleteMe();
					}
				});
				p.sendPacket(new ExShowScreenMessage(NpcStringId.HONORED_WARRIORS_HAVE_DEFEATED_THE_WATER_DRAGON_FAFURION, ExShowScreenMessage.TOP_CENTER, 20000, true));
			});
			
			GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, DEAD);
			final long respawnTime = (Config.FAFURION_SPAWN_INTERVAL + getRandom(-Config.FAFURION_SPAWN_RANDOM, Config.FAFURION_SPAWN_RANDOM)) * 3600000;
			final StatSet info = GrandBossManager.getInstance().getStatSet(FAFURION_GRANDBOSS_ID);
			info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(FAFURION_GRANDBOSS_ID, info);
			startQuestTimer("unlock_fafurion", respawnTime, null, null);
			if (_stage < 7)
			{
				_stage++;
			}
			GlobalVariablesManager.getInstance().set("Fafurion_Stage", _stage);
		}
		
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34488.html";
	}
	
	public static void main(String[] args)
	{
		new Fafurion();
	}
}