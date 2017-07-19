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

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.Universe;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */

public class AdminTest implements IAdminCommandHandler
{
	private static final int REQUIRED_LEVEL = Config.GM_TEST;
	public static final String[] ADMIN_TEST_COMMANDS =
	{
		"admin_test",
		"admin_stats",
		"admin_skill_test",
		"admin_st",
		"admin_known"
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IAdminCommandHandler#useAdminCommand(java.lang.String, com.l2jmobius.gameserver.model.L2PcInstance)
	 */
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (!Config.ALT_PRIVILEGES_ADMIN)
		{
			if (activeChar.getAccessLevel() < REQUIRED_LEVEL)
			{
				return false;
			}
		}
		
		if (command.equals("admin_stats"))
		{
			for (final String line : ThreadPoolManager.getInstance().getStats())
			{
				activeChar.sendMessage(line);
			}
		}
		else if (command.startsWith("admin_skill_test") || command.startsWith("admin_st"))
		{
			try
			{
				final StringTokenizer st = new StringTokenizer(command);
				st.nextToken();
				final int id = Integer.parseInt(st.nextToken());
				adminTestSkill(activeChar, id);
			}
			catch (final NumberFormatException e)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
			catch (final NoSuchElementException nsee)
			{
				activeChar.sendMessage("Command format is //skill_test <ID>");
			}
		}
		
		else if (command.startsWith("admin_test uni flush"))
		{
			Universe.getInstance().flush();
			activeChar.sendMessage("Universe Map Saved.");
		}
		else if (command.startsWith("admin_test uni"))
		{
			activeChar.sendMessage("Universe Map Size is: " + Universe.getInstance().size());
		}
		else if (command.equals("admin_known on"))
		{
			Config.CHECK_KNOWN = true;
		}
		else if (command.equals("admin_known off"))
		{
			Config.CHECK_KNOWN = false;
		}
		return true;
	}
	
	/**
	 * @param activeChar
	 * @param id
	 */
	private void adminTestSkill(L2PcInstance activeChar, int id)
	{
		L2Character player;
		final L2Object target = activeChar.getTarget();
		if ((target == null) || !(target instanceof L2Character))
		{
			player = activeChar;
		}
		else
		{
			player = (L2Character) target;
		}
		player.broadcastPacket(new MagicSkillUse(activeChar, player, id, 1, 1, 1));
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IAdminCommandHandler#getAdminCommandList()
	 */
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_TEST_COMMANDS;
	}
}