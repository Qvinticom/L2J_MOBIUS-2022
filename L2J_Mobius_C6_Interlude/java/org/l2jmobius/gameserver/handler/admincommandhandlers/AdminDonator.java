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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.logging.Logger;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.util.BuilderUtil;
import org.l2jmobius.gameserver.util.Util;

public class AdminDonator implements IAdminCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AdminDonator.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setdonator"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.startsWith("admin_setdonator"))
		{
			final String value = command.replace("admin_setdonator ", "");
			if (!Util.isDigit(value))
			{
				BuilderUtil.sendSysMessage(activeChar, "Syntax: //setdonator [duration in days / 0 to remove]");
				return false;
			}
			
			final WorldObject target = activeChar.getTarget();
			if (target.isPlayer())
			{
				final Player targetPlayer = target.getActingPlayer();
				final long donatorTime = Long.valueOf(value) * 24 * 60 * 60 * 1000;
				if (donatorTime > 0)
				{
					targetPlayer.setDonator(true);
					targetPlayer.updateNameTitleColor();
					targetPlayer.getVariables().set("CustomDonatorEnd", Chronos.currentTimeMillis() + donatorTime);
					targetPlayer.sendMessage(activeChar.getName() + " has granted you donator status!");
					activeChar.sendMessage("You have granted donator status to " + targetPlayer.getName());
					AdminData.broadcastMessageToGMs("Warn: " + activeChar.getName() + " has set " + targetPlayer.getName() + " as donator !");
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
					targetPlayer.broadcastUserInfo();
				}
				else
				{
					targetPlayer.setDonator(false);
					targetPlayer.updateNameTitleColor();
					targetPlayer.getVariables().remove("CustomDonatorEnd");
					targetPlayer.sendMessage(activeChar.getName() + " has revoked donator status from you!");
					activeChar.sendMessage("You have revoked donator status from " + targetPlayer.getName());
					AdminData.broadcastMessageToGMs("Warn: " + activeChar.getName() + " has removed donator status from player" + targetPlayer.getName());
					targetPlayer.broadcastUserInfo();
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Impossible to set a non player target as donator.");
				LOGGER.info("GM: " + activeChar.getName() + " is trying to set a non player target as donator.");
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
