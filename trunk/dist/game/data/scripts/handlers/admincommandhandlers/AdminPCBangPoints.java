/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Admin Player Commendation Point commands (PC Cafe/Bang).
 * @author Mobius
 */
public class AdminPCBangPoints implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_bang_points",
		"admin_count_bang_points",
		"admin_bangpoints",
		"admin_set_bang_points",
		"admin_subtract_bang_points"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_add_bang_points"))
		{
			try
			{
				if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				{
					String val = command.substring(22);
					if (!addGamePoints(activeChar, val))
					{
						activeChar.sendMessage("Usage: //add_bang_points count");
					}
				}
				else
				{
					activeChar.sendMessage("You must select a player first.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of missing parameter
				activeChar.sendMessage("Usage: //add_bang_points count");
			}
		}
		else if (command.equals("admin_count_bang_points"))
		{
			if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				L2PcInstance target = (L2PcInstance) activeChar.getTarget();
				activeChar.sendMessage(target.getName() + " has a total of " + target.getPcBangPoints() + " PC points.");
			}
			else
			{
				activeChar.sendMessage("You must select a player first.");
			}
		}
		else if (command.equals("admin_bangpoints"))
		{
			openGamePointsMenu(activeChar);
		}
		else if (command.startsWith("admin_set_bang_points"))
		{
			try
			{
				if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				{
					String val = command.substring(22);
					if (!setPcBangPoints(activeChar, val))
					{
						activeChar.sendMessage("Usage: //set_bang_points count");
					}
				}
				else
				{
					activeChar.sendMessage("You must select a player first.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of missing parameter
				activeChar.sendMessage("Usage: //set_bang_points count");
			}
		}
		else if (command.startsWith("admin_subtract_bang_points"))
		{
			try
			{
				if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				{
					String val = command.substring(27);
					if (!subtractGamePoints(activeChar, val))
					{
						activeChar.sendMessage("Usage: //subtract_bang_points count");
					}
				}
				else
				{
					activeChar.sendMessage("You must select a player first.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{
				// Case of missing parameter
				activeChar.sendMessage("Usage: //subtract_bang_points count");
			}
		}
		return true;
	}
	
	private void openGamePointsMenu(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/pcbang.htm");
		activeChar.sendPacket(html);
	}
	
	private boolean addGamePoints(L2PcInstance admin, String val)
	{
		L2Object target = admin.getTarget();
		L2PcInstance player = null;
		if (target.isPlayer())
		{
			player = (L2PcInstance) target;
		}
		else
		{
			admin.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		
		final int points = Integer.valueOf(val);
		if (points < 1)
		{
			admin.sendMessage("Invalid points count.");
			return false;
		}
		
		final int currentPoints = player.getPcBangPoints();
		if (currentPoints < 1)
		{
			player.setPcBangPoints(points);
		}
		else
		{
			player.setPcBangPoints(currentPoints + points);
		}
		
		player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), points, 1));
		admin.sendMessage("Added " + points + " PC points to " + player.getName() + ".");
		admin.sendMessage(player.getName() + " has now a total of " + player.getPcBangPoints() + " PC points.");
		return true;
	}
	
	private boolean setPcBangPoints(L2PcInstance admin, String val)
	{
		L2Object target = admin.getTarget();
		L2PcInstance player = null;
		if (target.isPlayer())
		{
			player = (L2PcInstance) target;
		}
		else
		{
			admin.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		
		final int points = Integer.valueOf(val);
		if (points < 0)
		{
			admin.sendMessage("Invalid points count.");
			return false;
		}
		
		player.setPcBangPoints(points);
		player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), points, 1));
		admin.sendMessage(player.getName() + " has now a total of " + points + " PC points.");
		return true;
	}
	
	private boolean subtractGamePoints(L2PcInstance admin, String val)
	{
		L2Object target = admin.getTarget();
		L2PcInstance player = null;
		if (target.isPlayer())
		{
			player = (L2PcInstance) target;
		}
		else
		{
			admin.sendPacket(SystemMessageId.THAT_IS_AN_INCORRECT_TARGET);
			return false;
		}
		
		final int points = Integer.valueOf(val);
		if (points < 1)
		{
			admin.sendMessage("Invalid points count.");
			return false;
		}
		
		final int currentPoints = player.getPcBangPoints();
		if (currentPoints <= points)
		{
			player.setPcBangPoints(0);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), 0, 1));
		}
		else
		{
			player.setPcBangPoints(currentPoints - points);
			player.sendPacket(new ExPCCafePointInfo(player.getPcBangPoints(), currentPoints - points, 1));
		}
		admin.sendMessage(player.getName() + " has now a total of " + player.getPcBangPoints() + " PC points.");
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}