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

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.Ride;
import org.l2jmobius.gameserver.util.BuilderUtil;

public class AdminRideWyvern implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_ride_wyvern",
		"admin_ride_strider",
		"admin_unride_wyvern",
		"admin_unride_strider",
		"admin_unride",
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (command.startsWith("admin_ride"))
		{
			if (activeChar.isMounted() || (activeChar.getPet() != null))
			{
				BuilderUtil.sendSysMessage(activeChar, "Already Have a Pet or Mounted.");
				return false;
			}
			
			int petRideId;
			if (command.startsWith("admin_ride_wyvern"))
			{
				petRideId = 12621;
				
				// Add skill Wyvern Breath
				activeChar.addSkill(SkillTable.getInstance().getSkill(4289, 1));
				activeChar.sendSkillList();
			}
			else if (command.startsWith("admin_ride_strider"))
			{
				petRideId = 12526;
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "Command '" + command + "' not recognized");
				return false;
			}
			
			if (!activeChar.disarmWeapons())
			{
				return false;
			}
			
			final Ride mount = new Ride(activeChar.getObjectId(), Ride.ACTION_MOUNT, petRideId);
			activeChar.sendPacket(mount);
			activeChar.broadcastPacket(mount);
			activeChar.setMountType(mount.getMountType());
		}
		else if (command.startsWith("admin_unride"))
		{
			activeChar.dismount();
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}