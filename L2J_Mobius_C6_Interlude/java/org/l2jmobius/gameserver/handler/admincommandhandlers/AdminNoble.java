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

import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.util.BuilderUtil;

public class AdminNoble implements IAdminCommandHandler
{
	protected static final Logger LOGGER = Logger.getLogger(AdminNoble.class.getName());
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_setnoble"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.startsWith("admin_setnoble"))
		{
			final WorldObject target = activeChar.getTarget();
			if (target instanceof Player)
			{
				final Player targetPlayer = (Player) target;
				final boolean newNoble = !targetPlayer.isNoble();
				if (newNoble)
				{
					targetPlayer.setNoble(true);
					targetPlayer.sendMessage("You are now a noblesse.");
					targetPlayer.getVariables().set("CustomNoble", true);
					targetPlayer.sendMessage(activeChar.getName() + " has granted noble status from you!");
					activeChar.sendMessage("You've granted noble status from " + targetPlayer.getName());
					AdminData.broadcastMessageToGMs("Warn: " + activeChar.getName() + " has set " + targetPlayer.getName() + " as noble !");
					targetPlayer.broadcastPacket(new SocialAction(targetPlayer.getObjectId(), 16));
				}
				else
				{
					targetPlayer.setNoble(false);
					targetPlayer.sendMessage("You are no longer a noblesse.");
					targetPlayer.getVariables().set("CustomNoble", false);
					targetPlayer.sendMessage(activeChar.getName() + " has revoked noble status for you!");
					activeChar.sendMessage("You've revoked noble status for " + targetPlayer.getName());
					AdminData.broadcastMessageToGMs("Warn: " + activeChar.getName() + " has removed noble status of player" + targetPlayer.getName());
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Impossible to set a non player target as noble.");
				LOGGER.info("GM: " + activeChar.getName() + " is trying to set a non player target as noble.");
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
