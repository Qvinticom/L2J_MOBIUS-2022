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

import com.l2jmobius.gameserver.datatables.csv.DoorTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.siege.Castle;
import com.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands:<br>
 * - open1 = open coloseum door 24190001<br>
 * - open2 = open coloseum door 24190002<br>
 * - open3 = open coloseum door 24190003<br>
 * - open4 = open coloseum door 24190004<br>
 * - openall = open all coloseum door<br>
 * - close1 = close coloseum door 24190001<br>
 * - close2 = close coloseum door 24190002<br>
 * - close3 = close coloseum door 24190003<br>
 * - close4 = close coloseum door 24190004<br>
 * - closeall = close all coloseum door<br>
 * <br>
 * - open = open selected door<br>
 * - close = close selected door<br>
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminDoorControl implements IAdminCommandHandler
{
	// private static Logger LOGGER = Logger.getLogger(AdminDoorControl.class);
	private static DoorTable _doorTable;
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_open",
		"admin_close",
		"admin_openall",
		"admin_closeall"
	};
	
	// private static final Map<String, Integer> doorMap = new HashMap<String, Integer>(); //FIXME: should we jute remove this?
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		_doorTable = DoorTable.getInstance();
		
		L2Object target2 = null;
		
		if (command.startsWith("admin_close ")) // id
		{
			try
			{
				final int doorId = Integer.parseInt(command.substring(12));
				
				if (_doorTable.getDoor(doorId) != null)
				{
					_doorTable.getDoor(doorId).closeMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).closeMe();
						}
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong ID door.");
				e.printStackTrace();
				return false;
			}
		}
		else if (command.equals("admin_close")) // target
		{
			target2 = activeChar.getTarget();
			
			if (target2 instanceof L2DoorInstance)
			{
				((L2DoorInstance) target2).closeMe();
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Incorrect target.");
			}
		}
		else if (command.startsWith("admin_open ")) // id
		{
			try
			{
				final int doorId = Integer.parseInt(command.substring(11));
				
				if (_doorTable.getDoor(doorId) != null)
				{
					_doorTable.getDoor(doorId).openMe();
				}
				else
				{
					for (Castle castle : CastleManager.getInstance().getCastles())
					{
						if (castle.getDoor(doorId) != null)
						{
							castle.getDoor(doorId).openMe();
						}
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Wrong ID door.");
				e.printStackTrace();
				return false;
			}
		}
		else if (command.equals("admin_open")) // target
		{
			target2 = activeChar.getTarget();
			
			if (target2 instanceof L2DoorInstance)
			{
				((L2DoorInstance) target2).openMe();
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Incorrect target.");
			}
		}
		
		// need optimize cycle
		// set limits on the ID doors that do not cycle to close doors
		else if (command.equals("admin_closeall"))
		{
			try
			{
				for (L2DoorInstance door : _doorTable.getDoors())
				{
					door.closeMe();
				}
				
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (L2DoorInstance door : castle.getDoors())
					{
						door.closeMe();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		else if (command.equals("admin_openall"))
		{
			// need optimize cycle
			// set limits on the PH door to do a cycle of opening doors.
			try
			{
				for (L2DoorInstance door : _doorTable.getDoors())
				{
					door.openMe();
				}
				
				for (Castle castle : CastleManager.getInstance().getCastles())
				{
					for (L2DoorInstance door : castle.getDoors())
					{
						door.openMe();
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
