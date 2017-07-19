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

import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.Olympiad;
import com.l2jmobius.gameserver.cache.HtmCache;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.NpcWalkerRoutesTable;
import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.datatables.TeleportLocationTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.Manager;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.model.EventEngine;
import com.l2jmobius.gameserver.model.L2Multisell;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.network.serverpackets.SignsSky;
import com.l2jmobius.gameserver.network.serverpackets.SunRise;
import com.l2jmobius.gameserver.network.serverpackets.SunSet;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - admin = shows menu
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminAdmin implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_admin",
		"admin_play_sounds",
		"admin_play_sound",
		"admin_gmliston",
		"admin_gmlistoff",
		"admin_silence",
		
		"admin_atmosphere",
		"admin_diet",
		"admin_tradeoff",
		
		"admin_reload",
		"admin_set",
		"admin_saveolymp",
		
		"admin_endolympiad",
		"admin_sethero",
		"admin_setnoble"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_MENU;
	
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
		
		if (command.equals("admin_admin"))
		{
			showMainPage(activeChar);
		}
		else if (command.equals("admin_play_sounds"))
		{
			AdminHelpPage.showHelpPage(activeChar, "songs/songs.htm");
		}
		else if (command.startsWith("admin_play_sounds"))
		{
			try
			{
				AdminHelpPage.showHelpPage(activeChar, "songs/songs" + command.substring(17) + ".htm");
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_play_sound"))
		{
			try
			{
				playAdminSound(activeChar, command.substring(17));
			}
			catch (final StringIndexOutOfBoundsException e)
			{
			}
		}
		else if (command.startsWith("admin_gmliston"))
		{
			GmListTable.getInstance().showGm(activeChar);
			activeChar.sendMessage("Registered into gm list.");
		}
		else if (command.startsWith("admin_gmlistoff"))
		{
			GmListTable.getInstance().hideGm(activeChar);
			
			activeChar.sendMessage("Removed from gm list.");
		}
		
		else if (command.startsWith("admin_silence"))
		{
			if (activeChar.getMessageRefusal()) // already in message refusal mode
			{
				activeChar.setMessageRefusal(false);
				activeChar.sendPacket(new SystemMessage(SystemMessage.MESSAGE_ACCEPTANCE_MODE));
			}
			else
			{
				activeChar.setMessageRefusal(true);
				activeChar.sendPacket(new SystemMessage(SystemMessage.MESSAGE_REFUSAL_MODE));
			}
		}
		
		else if (command.startsWith("admin_saveolymp"))
		{
			try
			{
				Olympiad.getInstance().save();
				activeChar.sendMessage("Olympiad data saved!!");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
		else if (command.startsWith("admin_endolympiad"))
		{
			try
			{
				Olympiad.getInstance().manualSelectHeroes();
				activeChar.sendMessage("Heroes were formed.");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
			
		}
		else if (command.startsWith("admin_sethero"))
		{
			L2PcInstance target = activeChar;
			if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof L2PcInstance))
			{
				target = (L2PcInstance) activeChar.getTarget();
			}
			
			target.setHero(target.isHero() ? false : true);
			target.broadcastUserInfo();
		}
		else if (command.startsWith("admin_setnoble"))
		{
			L2PcInstance target = activeChar;
			if ((activeChar.getTarget() != null) && (activeChar.getTarget() instanceof L2PcInstance))
			{
				target = (L2PcInstance) activeChar.getTarget();
			}
			
			target.setNoble(target.isNoble() ? false : true);
			
			if (target.isNoble())
			{
				activeChar.sendMessage(target.getName() + " has gained Noblesse status.");
			}
			else
			{
				activeChar.sendMessage(target.getName() + " has lost Noblesse status.");
			}
		}
		else if (command.startsWith("admin_atmosphere"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				final String type = st.nextToken();
				final String state = st.nextToken();
				adminAtmosphere(type, state, activeChar);
			}
			catch (final Exception ex)
			{
			}
		}
		else if (command.startsWith("admin_diet"))
		{
			try
			{
				if (!activeChar.getDietMode())
				{
					activeChar.setDietMode(true);
					activeChar.sendMessage("Diet mode on.");
				}
				else
				{
					activeChar.setDietMode(false);
					activeChar.sendMessage("Diet mode off.");
				}
				activeChar.refreshOverloaded();
			}
			catch (final Exception ex)
			{
			}
		}
		else if (command.startsWith("admin_tradeoff"))
		{
			try
			{
				final String mode = command.substring(15);
				if (mode.equalsIgnoreCase("on"))
				{
					activeChar.setTradeRefusal(true);
					activeChar.sendMessage("Tradeoff enabled.");
				}
				else if (mode.equalsIgnoreCase("off"))
				{
					activeChar.setTradeRefusal(false);
					activeChar.sendMessage("Tradeoff disabled.");
				}
			}
			catch (final Exception ex)
			{
				if (activeChar.getTradeRefusal())
				{
					activeChar.sendMessage("Tradeoff currently enabled.");
				}
				else
				{
					activeChar.sendMessage("Tradeoff currently disabled.");
				}
			}
		}
		else if (command.startsWith("admin_reload"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			try
			{
				final String type = st.nextToken();
				
				if (type.startsWith("multisell"))
				{
					L2Multisell.getInstance().reload();
					activeChar.sendMessage("All Multisells have been reloaded.");
				}
				else if (type.startsWith("teleport"))
				{
					TeleportLocationTable.getInstance().reloadAll();
					activeChar.sendMessage("Teleport location table has been reloaded.");
				}
				else if (type.startsWith("skill"))
				{
					SkillTable.getInstance().reload();
					activeChar.sendMessage("All Skills have been reloaded.");
				}
				else if (type.startsWith("npc"))
				{
					NpcTable.getInstance().reloadAllNpc();
					activeChar.sendMessage("All NPCs have been reloaded.");
				}
				else if (type.startsWith("htm"))
				{
					HtmCache.getInstance().reload();
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " megabytes on " + HtmCache.getInstance().getLoadedFiles() + " files loaded.");
					
				}
				else if (type.startsWith("item"))
				{
					ItemTable.getInstance().reload();
					
					activeChar.sendMessage("All Item templates have been reloaded.");
				}
				else if (type.startsWith("config"))
				{
					Config.load();
					activeChar.sendMessage("All config settings have been reload");
				}
				else if (type.startsWith("instancemanager"))
				{
					Manager.reloadAll();
					activeChar.sendMessage("All instance managers have been reloaded.");
				}
				else if (type.startsWith("npcwalker"))
				{
					NpcWalkerRoutesTable.getInstance().load();
					activeChar.sendMessage("All NPC walker routes have been reloaded.");
				}
				else if (type.startsWith("quest"))
				{
					QuestManager.getInstance().reloadAllQuests();
					activeChar.sendMessage("All Quests have been reloaded.");
				}
				else if (type.startsWith("event"))
				{
					EventEngine.load();
					activeChar.sendMessage("All Events have been reloaded.");
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage:  //reload <multisell|skill|npc|htm|item|instancemanager>");
			}
		}
		else if (command.startsWith("admin_set"))
		{
			final StringTokenizer st = new StringTokenizer(command);
			st.nextToken();
			
			try
			{
				final String[] parameter = st.nextToken().split("=");
				
				final String pName = parameter[0].trim();
				final String pValue = parameter[1].trim();
				
				if (Config.setParameterValue(pName, pValue))
				{
					activeChar.sendMessage("Parameter set succesfully.");
				}
				else
				{
					activeChar.sendMessage("Invalid parameter!");
				}
			}
			catch (final Exception e)
			{
				activeChar.sendMessage("Usage:  //set parameter=value");
			}
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
	
	/**
	 * @param type - atmosphere type (signssky,sky)
	 * @param state - atmosphere state(night,day)
	 * @param activeChar
	 */
	public void adminAtmosphere(String type, String state, L2PcInstance activeChar)
	{
		L2GameServerPacket packet = null;
		
		if (type.equals("signsky"))
		{
			if (state.equals("dawn"))
			{
				packet = new SignsSky(2);
			}
			else if (state.equals("dusk"))
			{
				packet = new SignsSky(1);
			}
			
		}
		else if (type.equals("sky"))
		{
			if (state.equals("night"))
			{
				packet = new SunSet();
			}
			else if (state.equals("day"))
			{
				packet = new SunRise();
			}
			
		}
		else
		{
			activeChar.sendMessage("Only sky and signsky atmosphere type allowed, damn u!");
		}
		
		if (packet != null)
		{
			for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				player.sendPacket(packet);
			}
			
		}
	}
	
	public void playAdminSound(L2PcInstance activeChar, String sound)
	{
		final PlaySound _snd = new PlaySound(1, sound, 0, 0, 0, 0, 0);
		activeChar.sendPacket(_snd);
		activeChar.broadcastPacket(_snd);
		showMainPage(activeChar);
		
		activeChar.sendMessage("Playing " + sound + ".");
	}
	
	public void showMainPage(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(5);
		html.setFile("data/html/admin/adminpanel.htm");
		activeChar.sendPacket(html);
	}
}