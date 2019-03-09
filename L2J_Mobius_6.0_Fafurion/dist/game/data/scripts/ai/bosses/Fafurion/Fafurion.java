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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

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
	private static final Location RAID_ENTER_LOC = new Location(180059, 212896, -14727);
	private static final Location FAFURION_SPAWN_LOC = new Location(180712, 210664, -14823, 22146);
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int FIGHTING = 2;
	private static final int DEAD = 3;
	// Misc
	private static final int RAID_DURATION = 5; // hours
	private static L2Npc bossInstance;
	
	private Fafurion()
	{
		addStartNpc(HEART_OF_TSUNAMI);
		addTalkId(HEART_OF_TSUNAMI);
		addFirstTalkId(HEART_OF_TSUNAMI);
		addKillId(FAFURION_FINAL_FORM);
		// Unlock
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(FAFURION_GRANDBOSS_ID);
		final int status = GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID);
		if (status == DEAD)
		{
			final long time = info.getLong("respawn_time") - System.currentTimeMillis();
			if (time > 0)
			{
				startQuestTimer("unlock_fafurion", time, null, null);
			}
			else
			{
				GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
			}
		}
		else if (status != ALIVE)
		{
			GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
		}
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "unlock_fafurion":
			{
				GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
				break;
			}
			case "beginning":
			{
				if (GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID) == WAITING)
				{
					GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, FIGHTING);
					bossInstance = addSpawn(FAFURION_FINAL_FORM, FAFURION_SPAWN_LOC.getX(), FAFURION_SPAWN_LOC.getY(), FAFURION_SPAWN_LOC.getZ(), FAFURION_SPAWN_LOC.getHeading(), false, 0, false);
					startQuestTimer("resetRaid", RAID_DURATION * 60 * 60 * 1000, bossInstance, null);
				}
				break;
			}
			case "resetRaid":
			{
				final int status = GrandBossManager.getInstance().getBossStatus(FAFURION_GRANDBOSS_ID);
				if ((status > ALIVE) && (status < DEAD))
				{
					GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, ALIVE);
					npc.deleteMe();
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
					if (!player.isInParty())
					{
						return "34488-01.html";
					}
					final L2Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<L2PcInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
					final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
					if (!isPartyLeader)
					{
						return "34488-02.html";
					}
					if ((members.size() < Config.FAFURION_MIN_PLAYERS) || (members.size() > Config.FAFURION_MAX_PLAYERS))
					{
						return "34488-01.html";
					}
					for (L2PcInstance member : members)
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
					for (L2PcInstance member : members)
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
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		// TODO: More ids.
		// switch (npc.getId())
		// {
		// case FAFURION:
		// {
		GrandBossManager.getInstance().setBossStatus(FAFURION_GRANDBOSS_ID, DEAD);
		final long respawnTime = (Config.FAFURION_SPAWN_INTERVAL + getRandom(-Config.FAFURION_SPAWN_RANDOM, Config.FAFURION_SPAWN_RANDOM)) * 3600000;
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(FAFURION_GRANDBOSS_ID);
		info.set("respawn_time", System.currentTimeMillis() + respawnTime);
		GrandBossManager.getInstance().setStatsSet(FAFURION_GRANDBOSS_ID, info);
		startQuestTimer("unlock_fafurion", respawnTime, null, null);
		// break;
		// }
		// }
		return super.onKill(npc, killer, isSummon);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "34488.html";
	}
	
	public static void main(String[] args)
	{
		new Fafurion();
	}
}
