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
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;

/**
 * Admin Ncoin commands.
 * @author Mobius
 */
public class AdminGamePoints implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_add_game_points",
		"admin_count_game_points",
		"admin_gamepoints",
		"admin_set_game_points",
		"admin_subtract_game_points"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_add_game_points"))
		{
			try
			{
				if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				{
					String val = command.substring(22);
					if (!addGamePoints(activeChar, val))
					{
						activeChar.sendMessage("Usage: //add_game_points count");
					}
				}
				else
				{
					activeChar.sendMessage("You must select a player first.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //add_game_points count");
			}
		}
		else if (command.equals("admin_count_game_points"))
		{
			if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
			{
				L2PcInstance target = (L2PcInstance) activeChar.getTarget();
				activeChar.sendMessage(target.getName() + " has a total of " + target.getPrimePoints() + " NCoins.");
			}
			else
			{
				activeChar.sendMessage("You must select a player first.");
			}
		}
		else if (command.equals("admin_gamepoints"))
		{
			openGamePointsMenu(activeChar);
		}
		else if (command.startsWith("admin_set_game_points"))
		{
			try
			{
				if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				{
					String val = command.substring(22);
					if (!setPrimePoints(activeChar, val))
					{
						activeChar.sendMessage("Usage: //set_game_points count");
					}
				}
				else
				{
					activeChar.sendMessage("You must select a player first.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //set_game_points count");
			}
		}
		else if (command.startsWith("admin_subtract_game_points"))
		{
			try
			{
				if ((activeChar.getTarget() != null) && activeChar.getTarget().isPlayer())
				{
					String val = command.substring(27);
					if (!subtractGamePoints(activeChar, val))
					{
						activeChar.sendMessage("Usage: //subtract_game_points count");
					}
				}
				else
				{
					activeChar.sendMessage("You must select a player first.");
				}
			}
			catch (StringIndexOutOfBoundsException e)
			{ // Case of missing parameter
				activeChar.sendMessage("Usage: //subtract_game_points count");
			}
		}
		return true;
	}
	
	private void openGamePointsMenu(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage();
		html.setFile(activeChar.getHtmlPrefix(), "data/html/admin/NCoins.htm");
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
			admin.sendMessage("Invalid Ncoin count.");
			return false;
		}
		
		final int currentPoints = player.getPrimePoints();
		if (currentPoints < 1)
		{
			player.setPrimePoints(points);
		}
		else
		{
			player.setPrimePoints(currentPoints + points);
		}
		
		admin.sendMessage("Added " + points + " NCoins to " + player.getName() + ".");
		admin.sendMessage(player.getName() + " has now a total of " + player.getPrimePoints() + " NCoins.");
		return true;
	}
	
	private boolean setPrimePoints(L2PcInstance admin, String val)
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
			admin.sendMessage("Invalid Ncoin count.");
			return false;
		}
		
		player.setPrimePoints(points);
		admin.sendMessage(player.getName() + " has now a total of " + points + " NCoins.");
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
			admin.sendMessage("Invalid Ncoin count.");
			return false;
		}
		
		final int currentPoints = player.getPrimePoints();
		if (currentPoints <= points)
		{
			player.setPrimePoints(0);
		}
		else
		{
			player.setPrimePoints(currentPoints - points);
		}
		admin.sendMessage(player.getName() + " has now a total of " + player.getPrimePoints() + " NCoins.");
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}