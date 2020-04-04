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
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
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
	private static final int FAFURION_FINAL_FORM = 29367;
	// Item
	private static final int FONDUS_STONE = 80322;
	// Locations
	private static final Location ENTER_NPC_LOC = new Location(190856, 257112, -3328);
	private static final Location RAID_ENTER_LOC = new Location(180059, 212896, -14727);
	private static final Location FAFURION_SPAWN_LOC = new Location(180712, 210664, -14823, 22146);
	// Zone
	private static final NoRestartZone zone = ZoneManager.getInstance().getZoneById(85002, NoRestartZone.class); // Fafurion Nest zone
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int FIGHTING = 2;
	private static final int DEAD = 3;
	// Misc
	private static final int RAID_DURATION = 5; // hours
	
	private Fafurion()
	{
		addStartNpc(HEART_OF_TSUNAMI);
		addTalkId(HEART_OF_TSUNAMI);
		addFirstTalkId(HEART_OF_TSUNAMI);
		addKillId(FAFURION_FINAL_FORM);
		final StatSet info = GrandBossManager.getInstance().getStatSet(FAFURION_GRANDBOSS_ID);
		final long respawnTime = info.getLong("respawn_time");
		// Unlock
		if (getStatus() == DEAD)
		{
			final long time = respawnTime - System.currentTimeMillis();
			if (time > 0)
			{
				startQuestTimer("unlock_fafurion", time, null, null);
			}
			else
			{
				setStatus(ALIVE);
			}
		}
		else if (getStatus() != ALIVE)
		{
			setStatus(ALIVE);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final String htmltext = null;
		switch (event)
		{
			case "unlock_fafurion":
			{
				setStatus(ALIVE);
				break;
			}
			case "warning":
			{
				if (player.calculateDistance2D(FAFURION_SPAWN_LOC) < 5000)
				{
					player.sendPacket(new ExShowScreenMessage(NpcStringId.ALL_WHO_FEAR_FAFURION_LEAVE_THIS_PLACE_AT_ONCE, ExShowScreenMessage.TOP_CENTER, 10000, true));
					for (PlayerInstance plr : World.getInstance().getVisibleObjectsInRange(player, PlayerInstance.class, 5000))
					{
						plr.sendPacket(new ExShowScreenMessage(NpcStringId.ALL_WHO_FEAR_FAFURION_LEAVE_THIS_PLACE_AT_ONCE, ExShowScreenMessage.TOP_CENTER, 10000, true));
					}
				}
				break;
			}
			case "beginning":
			{
				if (getStatus() == WAITING)
				{
					setStatus(FIGHTING);
					final Npc bossInstance = addSpawn(FAFURION_FINAL_FORM, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
					startQuestTimer("resetRaid", RAID_DURATION * 60 * 60 * 1000, bossInstance, null);
				}
				break;
			}
			case "SKIP_WAITING":
			{
				if (getStatus() == WAITING)
				{
					cancelQuestTimer("warning", null, null);
					cancelQuestTimer("beginning", null, null);
					notifyEvent("beginning", null, null);
					player.sendMessage(getClass().getSimpleName() + ": Skipping waiting time ...");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": You can't skip waiting time right now!");
				}
				break;
			}
			case "RESPAWN_FAFURION":
			{
				if (getStatus() == DEAD)
				{
					setRespawn(0);
					cancelQuestTimer("unlock_fafurion", null, null);
					notifyEvent("unlock_fafurion", null, null);
					player.sendMessage(getClass().getSimpleName() + ": Fafurion has been respawned.");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": You can't respawn Fafurion while he is alive!");
				}
				break;
			}
			case "ABORT_FIGHT":
			{
				if (getStatus() == FIGHTING)
				{
					setStatus(ALIVE);
					cancelQuestTimer("resetRaid", npc, null);
					for (Creature creature : zone.getCharactersInside())
					{
						if (creature != null)
						{
							if (creature.isNpc())
							{
								if (creature.getId() == FAFURION_FINAL_FORM)
								{
									creature.teleToLocation(FAFURION_SPAWN_LOC);
								}
								else
								{
									creature.deleteMe();
								}
							}
							else if (creature.isPlayer() && !creature.isGM())
							{
								creature.teleToLocation(ENTER_NPC_LOC);
							}
						}
					}
					player.sendMessage(getClass().getSimpleName() + ": Fight has been aborted!");
				}
				else
				{
					player.sendMessage(getClass().getSimpleName() + ": You can't abort fight right now!");
				}
				break;
			}
			case "resetRaid":
			{
				if ((getStatus() > ALIVE) && (getStatus() < DEAD))
				{
					for (PlayerInstance plr : World.getInstance().getVisibleObjectsInRange(npc, PlayerInstance.class, 5000))
					{
						plr.sendPacket(new ExShowScreenMessage(NpcStringId.EXCEEDED_THE_FAFURION_S_NEST_RAID_TIME_LIMIT, ExShowScreenMessage.TOP_CENTER, 10000, true));
					}
					setStatus(ALIVE);
					setRespawn(0);
					npc.deleteMe();
					
				}
				break;
			}
			case "enter_area":
			{
				if (player.isGM())
				{
					player.teleToLocation(RAID_ENTER_LOC, true);
				}
				else
				{
					if (((getStatus() > ALIVE) && (getStatus() < DEAD)) || (getStatus() == DEAD))
					{
						return "34488-02.html";
					}
					if (!player.isInParty())
					{
						return "34488-01.html";
					}
					final Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<PlayerInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
					final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
					if (!isPartyLeader)
					{
						return "34488-02.html";
					}
					if ((members.size() < Config.FAFURION_MIN_PLAYERS) || (members.size() > Config.FAFURION_MAX_PLAYERS))
					{
						return "34488-01.html";
					}
					for (PlayerInstance member : members)
					{
						if (member.getLevel() < Config.FAFURION_MIN_PLAYER_LVL)
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
					for (PlayerInstance member : members)
					{
						if ((member.calculateDistance2D(npc) < 1000) && (npc.getId() == HEART_OF_TSUNAMI))
						{
							member.teleToLocation(RAID_ENTER_LOC, true);
						}
					}
				}
				if (getStatus() == ALIVE)
				{
					setStatus(WAITING);
					startQuestTimer("beginning", Config.FAFURION_WAIT_TIME * 60000, null, null);
					startQuestTimer("warning", Config.FAFURION_WAIT_TIME > 0 ? (Config.FAFURION_WAIT_TIME * 60000) - 30000 : 0, null, player);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		for (PlayerInstance player : World.getInstance().getVisibleObjectsInRange(npc, PlayerInstance.class, 5000))
		{
			player.sendPacket(new ExShowScreenMessage(NpcStringId.HONORED_WARRIORS_HAVE_DEFEATED_THE_WATER_DRAGON_FAFURION, ExShowScreenMessage.TOP_CENTER, 10000, true));
		}
		
		setStatus(DEAD);
		final long respawnTime = (Config.FAFURION_SPAWN_INTERVAL + getRandom(-Config.FAFURION_SPAWN_RANDOM, Config.FAFURION_SPAWN_RANDOM)) * 3600000;
		startQuestTimer("unlock_fafurion", respawnTime, null, null);
		setRespawn(respawnTime);
		return super.onKill(npc, killer, isSummon);
	}
	
	private int getStatus()
	{
		return GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID);
	}
	
	private void setStatus(int status)
	{
		GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, status);
	}
	
	private void setRespawn(long respawnTime)
	{
		GrandBossManager.getInstance().getStatSet(FAFURION_GRANDBOSS_ID).set("respawn_time", System.currentTimeMillis() + respawnTime);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		return "34488.html";
	}
	
	public static void main(String[] args)
	{
		new Fafurion();
	}
}
