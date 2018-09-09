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

import java.util.Collection;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.TownManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Announcements;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.model.zone.type.L2TownZone;

public class AdminTownWar implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_townwar_start",
		"admin_townwar_end"
	};
	private L2Object _activeObject;
	
	public final L2Object getActiveObject()
	{
		return _activeObject;
	}
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_townwar_start")) // townwar_start
		{
			startTW(activeChar);
		}
		if (command.startsWith("admin_townwar_end")) // townwar_end
		{
			endTW(activeChar);
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	private void startTW(L2PcInstance activeChar)
	{
		// All Towns will become War Zones
		if (Config.TW_ALL_TOWNS)
		{
			TownManager.getInstance().getTown(1).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(2).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(3).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(4).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(5).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(6).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(7).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(8).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(9).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(10).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(11).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(12).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(13).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(14).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(15).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(16).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(17).setParameter("noPeace", "true");
			TownManager.getInstance().getTown(19).setParameter("noPeace", "true");
		}
		
		// A Town will become War Zone
		if (!Config.TW_ALL_TOWNS && (Config.TW_TOWN_ID != 18) && (Config.TW_TOWN_ID != 21) && (Config.TW_TOWN_ID != 22))
		{
			TownManager.getInstance().getTown(Config.TW_TOWN_ID).setParameter("noPeace", "true");
		}
		
		final Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		{
			int x;
			int y;
			int z;
			L2TownZone Town;
			
			for (L2PcInstance onlinePlayer : pls)
			{
				if (onlinePlayer.isOnline() == 1)
				{
					x = onlinePlayer.getX();
					y = onlinePlayer.getY();
					z = onlinePlayer.getZ();
					
					Town = TownManager.getInstance().getTown(x, y, z);
					if (Town != null)
					{
						if ((Town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(ZoneId.PVP, false);
							onlinePlayer.revalidateZone(true);
						}
						else if (Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(ZoneId.PVP, false);
							onlinePlayer.revalidateZone(true);
						}
					}
					onlinePlayer.setInTownWar(true);
				}
			}
		}
		
		// Announce for all towns
		if (Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().gameAnnounceToAll("Town War Event!");
			Announcements.getInstance().gameAnnounceToAll("All towns have been set to war zone by " + activeChar.getName() + ".");
		}
		
		// Announce for one town
		if (!Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().gameAnnounceToAll("Town War Event!");
			Announcements.getInstance().gameAnnounceToAll(TownManager.getInstance().getTown(Config.TW_TOWN_ID).getName() + " has been set to war zone by " + activeChar.getName() + ".");
		}
	}
	
	@SuppressWarnings("deprecation")
	private void endTW(L2PcInstance activeChar)
	{
		// All Towns will become Peace Zones
		if (Config.TW_ALL_TOWNS)
		{
			TownManager.getInstance().getTown(1).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(2).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(3).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(4).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(5).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(6).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(7).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(8).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(9).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(10).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(11).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(12).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(13).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(14).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(15).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(16).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(17).setParameter("noPeace", "false");
			TownManager.getInstance().getTown(19).setParameter("noPeace", "false");
		}
		
		// A Town will become Peace Zone
		if (!Config.TW_ALL_TOWNS && (Config.TW_TOWN_ID != 18) && (Config.TW_TOWN_ID != 21) && (Config.TW_TOWN_ID != 22))
		{
			TownManager.getInstance().getTown(Config.TW_TOWN_ID).setParameter("noPeace", "false");
		}
		
		final Collection<L2PcInstance> pls = L2World.getInstance().getAllPlayers();
		{
			int xx;
			int yy;
			int zz;
			L2TownZone Town;
			
			for (L2PcInstance onlinePlayer : pls)
			{
				if (onlinePlayer.isOnline() == 1)
				{
					xx = onlinePlayer.getX();
					yy = onlinePlayer.getY();
					zz = onlinePlayer.getZ();
					
					Town = TownManager.getInstance().getTown(xx, yy, zz);
					if (Town != null)
					{
						if ((Town.getTownId() == Config.TW_TOWN_ID) && !Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(ZoneId.PVP, true);
							onlinePlayer.revalidateZone(true);
						}
						else if (Config.TW_ALL_TOWNS)
						{
							onlinePlayer.setInsideZone(ZoneId.PVP, true);
							onlinePlayer.revalidateZone(true);
						}
					}
					onlinePlayer.setInTownWar(false);
				}
			}
		}
		
		// Announce for all towns
		if (Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().gameAnnounceToAll("All towns have been set back to normal by " + activeChar.getName() + ".");
		}
		
		// Announce for one town
		if (!Config.TW_ALL_TOWNS)
		{
			Announcements.getInstance().gameAnnounceToAll(TownManager.getInstance().getTown(Config.TW_TOWN_ID).getName() + " has been set back to normal by " + activeChar.getName() + ".");
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}