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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jmobius.gameserver.handler.AdminCommandHandler;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.util.GMAudit;

/**
 * This class handles all GM commands triggered by //command
 */
public final class SendBypassBuildCmd extends L2GameClientPacket
{
	protected static final Logger LOGGER = Logger.getLogger(SendBypassBuildCmd.class.getName());
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;
	
	private String _command;
	
	@Override
	protected void readImpl()
	{
		_command = "admin_" + readS().trim();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		// Checks The Access and notify requester if requester access it not allowed for that command
		if (!AdminCommandAccessRights.getInstance().hasAccess(_command, activeChar.getAccessLevel()))
		{
			activeChar.sendMessage("You don't have the access right to use this command!");
			LOGGER.warning("Character " + activeChar.getName() + " tried to use admin command " + _command + ", but doesn't have access to it!");
			return;
		}
		
		// gets the Handler of That Commmand
		final IAdminCommandHandler ach = AdminCommandHandler.getInstance().getAdminCommandHandler(_command);
		
		// if handler is valid we Audit and use else we notify in console.
		if (ach != null)
		{
			if (Config.GMAUDIT)
			{
				GMAudit.auditGMAction(activeChar.getName() + " [" + activeChar.getObjectId() + "]", _command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"));
			}
			
			ach.useAdminCommand(_command, activeChar);
		}
		else
		{
			activeChar.sendMessage("The command " + _command + " doesn't exists!");
			LOGGER.warning("No handler registered for admin command '" + _command + "'");
			return;
		}
	}
}
