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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.datatables.TeleportLocationTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.DayNightSpawnManager;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

import javolution.text.TextBuilder;

/**
 * This class handles following admin commands: - show_spawns = shows menu - spawn_index lvl = shows menu for monsters with respective level - spawn_monster id = spawns monster id on target
 * @version $Revision: 1.2.2.5.2.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminSpawn implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_show_spawns",
		"admin_spawn",
		"admin_spawn_monster",
		"admin_spawn_index",
		"admin_unspawnall",
		"admin_respawnall",
		"admin_spawn_reload",
		"admin_npc_index",
		"admin_show_npcs",
		"admin_teleport_reload",
		"admin_spawnnight",
		"admin_spawnday"
	};
	
	public static Logger _log = Logger.getLogger(AdminSpawn.class.getName());
	
	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
	private static final int REQUIRED_LEVEL2 = Config.GM_TELEPORT_OTHER;
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (!(checkLevel(activeChar.getAccessLevel()) && activeChar.isGM()))
			{
				return false;
			}
		}
		
		if (command.equals("admin_show_spawns"))
		{
			AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
		}
		else if (command.startsWith("admin_spawn_index"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				final int level = Integer.parseInt(st.nextToken());
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (final NoSuchElementException nsee)
				{
				}
				
				showMonsters(activeChar, level, from);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
			catch (final NumberFormatException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "spawns.htm");
			}
		}
		else if (command.equals("admin_show_npcs"))
		{
			AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
		}
		else if (command.startsWith("admin_npc_index"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				final String letter = st.nextToken();
				int from = 0;
				try
				{
					from = Integer.parseInt(st.nextToken());
				}
				catch (final NoSuchElementException nsee)
				{
				}
				
				showNpcs(activeChar, letter, from);
			}
			catch (final StringIndexOutOfBoundsException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
			catch (final NumberFormatException e)
			{
				AdminHelpPage.showHelpPage(activeChar, "npcs.htm");
			}
		}
		else if (command.startsWith("admin_spawn ") || command.startsWith("admin_spawn_monster"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			try
			{
				st.nextToken();
				final String id = st.nextToken();
				int respawnTime = 0;
				// FIXME: 0 time should mean never respawn.
				// At the moment it will just be set to d elsewhere.
				int mobCount = 1;
				if (st.hasMoreTokens())
				{
					mobCount = Integer.parseInt(st.nextToken());
				}
				if (st.hasMoreTokens())
				{
					respawnTime = Integer.parseInt(st.nextToken());
				}
				spawnMonster(activeChar, id, respawnTime, mobCount);
			}
			catch (final Exception e)
			{
				// Case of wrong monster data
			}
		}
		else if (command.startsWith("admin_unspawnall"))
		{
			for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(new SystemMessage(SystemMessage.NPC_SERVER_NOT_OPERATING));
			}
			
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			L2World.getInstance().deleteVisibleNpcSpawns();
			GmListTable.broadcastMessageToGMs("NPC Unspawn completed!");
		}
		else if (command.startsWith("admin_spawnday"))
		{
			DayNightSpawnManager.getInstance().spawnDayCreatures();
			DayNightSpawnManager.getInstance().specialNightBoss(0);
		}
		else if (command.startsWith("admin_spawnnight"))
		{
			DayNightSpawnManager.getInstance().spawnNightCreatures();
			DayNightSpawnManager.getInstance().specialNightBoss(1);
		}
		else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload"))
		{
			// make shure all spawns are deleted
			RaidBossSpawnManager.getInstance().cleanUp();
			DayNightSpawnManager.getInstance().cleanUp();
			L2World.getInstance().deleteVisibleNpcSpawns();
			
			// now respawn all
			NpcTable.getInstance().reloadAllNpc();
			SpawnTable.getInstance().reloadAll();
			RaidBossSpawnManager.getInstance().reloadBosses();
			DayNightSpawnManager.getInstance().notifyChangeMode();
			QuestManager.getInstance().reloadAllQuests();
			GmListTable.broadcastMessageToGMs("NPC Respawn completed!");
		}
		else if (command.startsWith("admin_teleport_reload"))
		{
			TeleportLocationTable.getInstance().reloadAll();
			GmListTable.broadcastMessageToGMs("Teleport List Table reloaded.");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
	private void spawnMonster(L2PcInstance activeChar, String monsterId, int respawnTime, int mobCount)
	{
		L2Object target = activeChar.getTarget();
		if (target == null)
		{
			target = activeChar;
		}
		
		if ((target != activeChar) && (activeChar.getAccessLevel() < REQUIRED_LEVEL2))
		{
			return;
		}
		
		final Pattern pattern = Pattern.compile("[0-9]*");
		final Matcher regexp = pattern.matcher(monsterId);
		L2NpcTemplate template1;
		if (regexp.matches())
		{
			// First parameter was an ID number
			final int monsterTemplate = Integer.parseInt(monsterId);
			template1 = NpcTable.getInstance().getTemplate(monsterTemplate);
		}
		else
		{
			// First parameter wasn't just numbers so go by name not ID
			monsterId = monsterId.replace('_', ' ');
			template1 = NpcTable.getInstance().getTemplateByName(monsterId);
		}
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template1);
			if (Config.SAVE_GMSPAWN_ON_CUSTOM)
			{
				spawn.setCustom(true);
			}
			
			spawn.setLocx(target.getX());
			spawn.setLocy(target.getY());
			spawn.setLocz(target.getZ());
			spawn.setAmount(mobCount);
			spawn.setHeading(activeChar.getHeading());
			spawn.setRespawnDelay(respawnTime);
			
			if (RaidBossSpawnManager.getInstance().isDefined(spawn.getNpcid()))
			{
				activeChar.sendMessage("You cannot spawn another instance of " + template1.name + ".");
			}
			else
			{
				if (RaidBossSpawnManager.getInstance().getValidTemplate(spawn.getNpcid()) != null)
				{
					spawn.setRespawnMinDelay(43200);
					spawn.setRespawnMaxDelay(129600);
					RaidBossSpawnManager.getInstance().addNewSpawn(spawn, 0, template1.baseHpMax, template1.baseMpMax, true);
				}
				else
				{
					SpawnTable.getInstance().addNewSpawn(spawn, true);
					spawn.init();
				}
				
				activeChar.sendMessage("Created " + template1.name + " on " + target.getObjectId() + ".");
			}
		}
		catch (final Exception e)
		{
			activeChar.sendMessage("Target is not ingame.");
		}
	}
	
	private void showMonsters(L2PcInstance activeChar, int level, int from)
	{
		final TextBuilder tb = new TextBuilder();
		
		final L2NpcTemplate[] mobs = NpcTable.getInstance().getAllMonstersOfLevel(level);
		
		// Start
		tb.append("<html><title>Spawn Monster:</title><body><p> Level " + level + ":<br>Total Npc's : " + mobs.length + "<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index " + level + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		final String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.length; i++)
		{
			final String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";
			
			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;
				break;
			}
			
			tb.append(txt);
		}
		
		// End
		if (ended)
		{
			tb.append(end2);
		}
		else
		{
			tb.append(end1);
		}
		
		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
	
	private void showNpcs(L2PcInstance activeChar, String starting, int from)
	{
		final TextBuilder tb = new TextBuilder();
		final L2NpcTemplate[] mobs = NpcTable.getInstance().getAllNpcStartingWith(starting);
		
		// Start
		tb.append("<html><title>Spawn NPC:</title><body><p> There are " + mobs.length + " NPCs whose name starts with " + starting + ":<br>");
		String end1 = "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index " + starting + " $from$\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		final String end2 = "<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>";
		
		// Loop
		boolean ended = true;
		for (int i = from; i < mobs.length; i++)
		{
			final String txt = "<a action=\"bypass -h admin_spawn_monster " + mobs[i].npcId + "\">" + mobs[i].name + "</a><br1>";
			
			if ((tb.length() + txt.length() + end2.length()) > 8192)
			{
				end1 = end1.replace("$from$", "" + i);
				ended = false;
				break;
			}
			tb.append(txt);
		}
		
		// End
		if (ended)
		{
			tb.append(end2);
		}
		else
		{
			tb.append(end1);
		}
		
		activeChar.sendPacket(new NpcHtmlMessage(5, tb.toString()));
	}
}