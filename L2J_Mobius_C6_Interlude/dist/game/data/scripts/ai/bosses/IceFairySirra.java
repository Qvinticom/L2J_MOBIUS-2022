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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.quest.EventType;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.model.zone.type.BossZone;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Ice Fairy Sirra AI
 * @author Kerberos
 */
public class IceFairySirra extends Quest
{
	// NPC
	private static final int STEWARD = 32029;
	// Item
	private static final int SILVER_HEMOCYTE = 8057;
	// Spawns
	// @formatter:off
	private static final int[][] MONSTER_SPAWNS =
	{
		{29060, 105546, -127892, -2768},
		{29056, 102779, -125920, -2840},
		{22100, 111719, -126646, -2992},
		{22102, 109509, -128946, -3216},
		{22104, 109680, -125756, -3136}
	};
	// @formatter:on
	// Misc.
	private static BossZone _freyasZone;
	private static Player _player = null;
	protected Collection<Npc> _allMobs = ConcurrentHashMap.newKeySet();
	protected Future<?> _onDeadEventTask = null;
	
	public IceFairySirra()
	{
		super(-1, "ai/bosses");
		
		final int[] mobs =
		{
			STEWARD,
			22100,
			22102,
			22104
		};
		
		for (int mob : mobs)
		{
			addEventId(mob, EventType.QUEST_START);
			addEventId(mob, EventType.QUEST_TALK);
			addEventId(mob, EventType.NPC_FIRST_TALK);
		}
		
		init();
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (player.getQuestState(getName()) == null)
		{
			newQuestState(player);
		}
		player.setLastQuestNpcObject(npc.getObjectId());
		String filename = "";
		if (npc.isBusy())
		{
			filename = getHtmlPath(10);
		}
		else
		{
			filename = getHtmlPath(0);
		}
		sendHtml(npc, player, filename);
		return null;
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "check_condition":
			{
				if (npc.isBusy())
				{
					return super.onAdvEvent(event, npc, player);
				}
				String filename = "";
				if (player.isInParty() && (player.getParty().getPartyLeaderOID() == player.getObjectId()))
				{
					if (checkItems(player))
					{
						startQuestTimer("start", 100000, null, player);
						_player = player;
						destroyItems(player);
						player.getInventory().addItem("Scroll", 8379, 3, player, null);
						npc.setBusy(true);
						screenMessage(player, "Steward: Please wait a moment.", 100000);
						filename = getHtmlPath(3);
					}
					else
					{
						filename = getHtmlPath(2);
					}
				}
				else
				{
					filename = getHtmlPath(1);
				}
				sendHtml(npc, player, filename);
				break;
			}
			case "start":
			{
				if (_freyasZone == null)
				{
					LOGGER.warning("IceFairySirraManager: Failed to load zone");
					cleanUp();
					return super.onAdvEvent(event, npc, player);
				}
				_freyasZone.setZoneEnabled(true);
				closeGates();
				doSpawns();
				startQuestTimer("Party_Port", 2000, null, player);
				startQuestTimer("End", 1802000, null, player);
				break;
			}
			case "Party_Port":
			{
				teleportInside(player);
				screenMessage(player, "Steward: Please restore the Queen's appearance!", 10000);
				startQuestTimer("30MinutesRemaining", 300000, null, player);
				break;
			}
			case "30MinutesRemaining":
			{
				screenMessage(player, "30 minute(s) are remaining.", 10000);
				startQuestTimer("20minutesremaining", 600000, null, player);
				break;
			}
			case "20MinutesRemaining":
			{
				screenMessage(player, "20 minute(s) are remaining.", 10000);
				startQuestTimer("10minutesremaining", 600000, null, player);
				break;
			}
			case "10MinutesRemaining":
			{
				screenMessage(player, "Steward: Waste no time! Please hurry!", 10000);
				break;
			}
			case "End":
			{
				screenMessage(player, "Steward: Was it indeed too much to ask.", 10000);
				cleanUp();
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void init()
	{
		_freyasZone = GrandBossManager.getInstance().getZone(105546, -127892, -2768);
		if (_freyasZone == null)
		{
			LOGGER.warning("IceFairySirraManager: Failed to load zone");
			return;
		}
		_freyasZone.setZoneEnabled(false);
		final Npc steward = findTemplate(STEWARD);
		if (steward != null)
		{
			steward.setBusy(false);
		}
		openGates();
	}
	
	public void cleanUp()
	{
		init();
		cancelQuestTimer("30MinutesRemaining", null, _player);
		cancelQuestTimer("20MinutesRemaining", null, _player);
		cancelQuestTimer("10MinutesRemaining", null, _player);
		cancelQuestTimer("End", null, _player);
		for (Npc mob : _allMobs)
		{
			try
			{
				mob.getSpawn().stopRespawn();
				mob.deleteMe();
			}
			catch (Exception e)
			{
				LOGGER.warning("IceFairySirraManager: Failed deleting mob. " + e);
			}
		}
		_allMobs.clear();
	}
	
	public Npc findTemplate(int npcId)
	{
		Npc npc = null;
		for (Spawn spawn : SpawnTable.getInstance().getSpawnTable().values())
		{
			if ((spawn != null) && (spawn.getNpcId() == npcId))
			{
				npc = spawn.getLastSpawn();
				break;
			}
		}
		return npc;
	}
	
	protected void openGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				final Door door = DoorData.getInstance().getDoor(i);
				if (door != null)
				{
					door.openMe();
				}
				else
				{
					LOGGER.warning("IceFairySirraManager: Attempted to open undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("IceFairySirraManager: Failed closing door " + e);
			}
		}
	}
	
	protected void closeGates()
	{
		for (int i = 23140001; i < 23140003; i++)
		{
			try
			{
				final Door door = DoorData.getInstance().getDoor(i);
				if (door != null)
				{
					door.closeMe();
				}
				else
				{
					LOGGER.warning("IceFairySirraManager: Attempted to close undefined door. doorId: " + i);
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("IceFairySirraManager: Failed closing door " + e);
			}
		}
	}
	
	public boolean checkItems(Player player)
	{
		if (player.getParty() != null)
		{
			for (Player pc : player.getParty().getPartyMembers())
			{
				final Item i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				if ((i == null) || (i.getCount() < 10))
				{
					return false;
				}
			}
		}
		else
		{
			return false;
		}
		return true;
	}
	
	public void destroyItems(Player player)
	{
		if (player.getParty() != null)
		{
			for (Player pc : player.getParty().getPartyMembers())
			{
				final Item i = pc.getInventory().getItemByItemId(SILVER_HEMOCYTE);
				pc.destroyItem("Hemocytes", i.getObjectId(), 10, null, false);
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void teleportInside(Player player)
	{
		if (player.getParty() != null)
		{
			for (Player pc : player.getParty().getPartyMembers())
			{
				pc.teleToLocation(113533, -126159, -3488);
				if (_freyasZone == null)
				{
					LOGGER.warning("IceFairySirraManager: Failed to load zone");
					cleanUp();
					return;
				}
				_freyasZone.allowPlayerEntry(pc, 2103);
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void screenMessage(Player player, String text, int time)
	{
		if (player.getParty() != null)
		{
			for (Player pc : player.getParty().getPartyMembers())
			{
				pc.sendPacket(new ExShowScreenMessage(text, time));
			}
		}
		else
		{
			cleanUp();
		}
	}
	
	public void doSpawns()
	{
		Spawn spawnDat;
		NpcTemplate template;
		try
		{
			for (int i = 0; i < 5; i++)
			{
				template = NpcTable.getInstance().getTemplate(MONSTER_SPAWNS[i][0]);
				if (template != null)
				{
					spawnDat = new Spawn(template);
					spawnDat.setAmount(1);
					spawnDat.setX(MONSTER_SPAWNS[i][1]);
					spawnDat.setY(MONSTER_SPAWNS[i][2]);
					spawnDat.setZ(MONSTER_SPAWNS[i][3]);
					spawnDat.setHeading(0);
					spawnDat.setRespawnDelay(60);
					SpawnTable.getInstance().addNewSpawn(spawnDat, false);
					_allMobs.add(spawnDat.doSpawn());
					spawnDat.stopRespawn();
				}
				else
				{
					LOGGER.warning("IceFairySirraManager: Data missing in NPC table for ID: " + MONSTER_SPAWNS[i][0]);
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.warning("IceFairySirraManager: Spawns could not be initialized: " + e);
		}
	}
	
	public String getHtmlPath(int value)
	{
		String pom = "";
		pom = "32029-" + value;
		if (value == 0)
		{
			pom = "32029";
		}
		
		final String temp = "data/html/default/" + pom + ".htm";
		if (!Config.LAZY_CACHE)
		{
			// If not running lazy cache the file must be in the cache or it does not exist.
			if (HtmCache.getInstance().contains(temp))
			{
				return temp;
			}
		}
		else if (HtmCache.getInstance().isLoadable(temp))
		{
			return temp;
		}
		
		// If the file is not found, the standard message "I have nothing to say to you" is returned.
		return "data/html/npcdefault.htm";
	}
	
	public void sendHtml(Npc npc, Player player, String filename)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(npc.getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public static void main(String[] args)
	{
		new IceFairySirra();
	}
}
