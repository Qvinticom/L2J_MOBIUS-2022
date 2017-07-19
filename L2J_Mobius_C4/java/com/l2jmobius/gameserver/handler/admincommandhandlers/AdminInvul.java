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
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 * @version $Revision: 1.2.4.4 $ $Date: 2005/04/11 10:06:02 $
 */
public class AdminInvul implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminInvul.class.getName());
	private static String[] _adminCommands =
	{
		"admin_invul",
		"admin_setinvul"
	};
	private static final int REQUIRED_LEVEL = Config.GM_GODMODE;
	
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
		
		GMAudit.auditGMAction(activeChar.getName(), command, (activeChar.getTarget() != null ? activeChar.getTarget().getName() : "no-target"), "");
		
		if (command.equals("admin_invul"))
		{
			handleInvul(activeChar);
		}
		else if (command.equals("admin_setinvul"))
		{
			final L2Object target = activeChar.getTarget();
			if (target instanceof L2PcInstance)
			{
				handleInvul((L2PcInstance) target);
			}
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
	
	private void handleInvul(L2PcInstance activeChar)
	{
		if (activeChar.isInvul())
		{
			activeChar.setIsInvul(false);
			final String text = activeChar.getName() + " is now mortal.";
			final SystemMessage sm = new SystemMessage(614);
			sm.addString(text);
			activeChar.sendPacket(sm);
			if (Config.DEBUG)
			{
				_log.fine("GM: Gm removed invul mode from character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
			}
		}
		else
		{
			activeChar.setIsInvul(true);
			final String text = activeChar.getName() + " is now invulnerable.";
			final SystemMessage sm = new SystemMessage(614);
			sm.addString(text);
			activeChar.sendPacket(sm);
			if (Config.DEBUG)
			{
				_log.fine("GM: Gm activated invul mode for character " + activeChar.getName() + "(" + activeChar.getObjectId() + ")");
			}
		}
	}
}