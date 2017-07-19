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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GeoData;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author -Nemesiss-
 */
public class AdminGeodata implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_geo_pos",
		"admin_geo_spawn_pos",
		"admin_geo_can_move",
		"admin_geo_can_see"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_MIN;
	
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
		
		if (command.equals("admin_geo_pos"))
		{
			final int worldX = activeChar.getX();
			final int worldY = activeChar.getY();
			final int worldZ = activeChar.getZ();
			final int geoX = GeoData.getInstance().getGeoX(worldX);
			final int geoY = GeoData.getInstance().getGeoY(worldY);
			
			if (GeoData.getInstance().hasGeoPos(geoX, geoY))
			{
				activeChar.sendMessage("WorldX: " + worldX + ", WorldY: " + worldY + ", WorldZ: " + worldZ + ", GeoX: " + geoX + ", GeoY: " + geoY + ", GeoZ: " + GeoData.getInstance().getNearestZ(geoX, geoY, worldZ));
			}
			else
			{
				activeChar.sendMessage("There is no geodata at this position.");
			}
		}
		else if (command.equals("admin_geo_spawn_pos"))
		{
			final int worldX = activeChar.getX();
			final int worldY = activeChar.getY();
			final int worldZ = activeChar.getZ();
			final int geoX = GeoData.getInstance().getGeoX(worldX);
			final int geoY = GeoData.getInstance().getGeoY(worldY);
			
			if (GeoData.getInstance().hasGeoPos(geoX, geoY))
			{
				activeChar.sendMessage("WorldX: " + worldX + ", WorldY: " + worldY + ", WorldZ: " + worldZ + ", GeoX: " + geoX + ", GeoY: " + geoY + ", GeoZ: " + GeoData.getInstance().getSpawnHeight(worldX, worldY, worldZ, worldZ));
			}
			else
			{
				activeChar.sendMessage("There is no geodata at this position.");
			}
		}
		else if (command.equals("admin_geo_can_move"))
		{
			final L2Object target = activeChar.getTarget();
			if (target != null)
			{
				if (GeoData.getInstance().canSeeTarget(activeChar, target))
				{
					activeChar.sendMessage("Can move beeline.");
				}
				else
				{
					activeChar.sendMessage("Can not move beeline!");
				}
			}
			else
			{
				activeChar.sendMessage("Incorrect Target.");
			}
		}
		else if (command.equals("admin_geo_can_see"))
		{
			final L2Object target = activeChar.getTarget();
			if (target != null)
			{
				if (GeoData.getInstance().canSeeTarget(activeChar, target))
				{
					activeChar.sendMessage("Can see target.");
				}
				else
				{
					activeChar.sendMessage("Cannot see Target.");
				}
			}
			else
			{
				activeChar.sendMessage("Incorrect Target.");
			}
		}
		else
		{
			return false;
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
}