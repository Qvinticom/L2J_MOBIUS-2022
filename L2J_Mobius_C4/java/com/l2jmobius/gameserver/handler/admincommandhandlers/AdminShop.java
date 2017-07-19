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

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.TradeController;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2TradeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.BuyList;
import com.l2jmobius.gameserver.network.serverpackets.SellList;

/**
 * This class handles following admin commands: - gmshop = shows menu - buy id = shows shop with respective id - sell = sells items
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminShop implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminShop.class.getName());
	
	private static String[] _adminCommands =
	{
		"admin_buy",
		"admin_sell",
		"admin_gmshop"
	};
	
	private static final int REQUIRED_LEVEL = Config.GM_CREATE_ITEM;
	
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
		
		if (command.startsWith("admin_buy"))
		{
			try
			{
				handleBuyRequest(activeChar, command.substring(10));
			}
			catch (final IndexOutOfBoundsException e)
			{
				activeChar.sendMessage("Please specify a buylist.");
			}
		}
		else if (command.startsWith("admin_sell"))
		{
			activeChar.sendPacket(new SellList(activeChar));
			
			if (Config.DEBUG)
			{
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM Shop sell list");
			}
			
			activeChar.sendPacket(new ActionFailed());
		}
		else if (command.equals("admin_gmshop"))
		{
			AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
		}
		
		final String target = (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target");
		GMAudit.auditGMAction(activeChar.getName(), command, target, "");
		
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
	
	private void handleBuyRequest(L2PcInstance activeChar, String command)
	{
		int val = -1;
		
		try
		{
			val = Integer.parseInt(command);
		}
		catch (final Exception e)
		{
			_log.warning("admin buylist failed:" + command);
		}
		
		final L2TradeList list = TradeController.getInstance().getBuyList(val);
		if (list != null)
		{
			final BuyList bl = new BuyList(list, activeChar.getAdena());
			activeChar.sendPacket(bl);
			
			if (Config.DEBUG)
			{
				_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ") opened GM shop id " + val);
			}
		}
		else
		{
			_log.warning("no buylist with id:" + val);
		}
		
		activeChar.sendPacket(new ActionFailed());
	}
}