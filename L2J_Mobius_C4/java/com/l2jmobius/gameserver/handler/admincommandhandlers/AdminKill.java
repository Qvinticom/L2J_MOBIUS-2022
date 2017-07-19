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

import java.util.StringTokenizer;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.GMAudit;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2ControllableMobInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class handles following admin commands: - kill = kills target L2Character - kill_monster = kills target non-player - kill <radius> = If radius is specified, then ALL players only in that radius will be killed. - kill_monster <radius> = If radius is specified, then ALL non-players only in
 * that radius will be killed.
 * @version $Revision: 1.2.4.5 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminKill implements IAdminCommandHandler
{
	private static Logger _log = Logger.getLogger(AdminKill.class.getName());
	private static String[] _adminCommands =
	{
		"admin_kill",
		"admin_kill_monster"
	};
	private static final int REQUIRED_LEVEL = Config.GM_NPC_EDIT;
	
	private boolean checkLevel(int level)
	{
		return (level >= REQUIRED_LEVEL);
	}
	
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
		
		final String target = (activeChar.getTarget() != null) ? activeChar.getTarget().getName() : "no-target";
		GMAudit.auditGMAction(activeChar.getName(), command, target, "");
		
		if (command.startsWith("admin_kill"))
		{
			final StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken(); // skip command
			
			if (st.hasMoreTokens())
			{
				final String firstParam = st.nextToken();
				final L2PcInstance plyr = L2World.getInstance().getPlayer(firstParam);
				if (plyr != null)
				{
					if (st.hasMoreTokens())
					{
						try
						{
							final int radius = Integer.parseInt(st.nextToken());
							
							for (final L2Character knownChar : plyr.getKnownList().getKnownCharactersInRadius(radius))
							{
								if ((knownChar == null) || (knownChar instanceof L2ControllableMobInstance) || knownChar.equals(activeChar))
								{
									continue;
								}
								
								kill(activeChar, knownChar);
							}
							
							activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
							return true;
						}
						catch (final NumberFormatException e)
						{
							activeChar.sendMessage("Invalid radius.");
							return false;
						}
					}
					kill(activeChar, plyr);
				}
				else
				{
					try
					{
						final int radius = Integer.parseInt(firstParam);
						
						for (final L2Character knownChar : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if ((knownChar == null) || (knownChar instanceof L2ControllableMobInstance) || knownChar.equals(activeChar))
							{
								continue;
							}
							
							kill(activeChar, knownChar);
						}
						
						activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
						return true;
					}
					catch (final NumberFormatException e)
					{
						activeChar.sendMessage("Enter a valid player name or radius.");
						return false;
					}
				}
			}
			else
			{
				final L2Object obj = activeChar.getTarget();
				
				if ((obj == null) || (obj instanceof L2ControllableMobInstance) || !(obj instanceof L2Character))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
				}
				else
				{
					kill(activeChar, (L2Character) obj);
				}
			}
		}
		
		return true;
	}
	
	private void kill(L2PcInstance activeChar, L2Character target)
	{
		if (target instanceof L2PcInstance)
		{
			target.reduceCurrentHp(target.getMaxHp() + target.getMaxCp() + 1, activeChar);
		}
		else if (Config.CHAMPION_ENABLE && target.isChampion())
		{
			target.reduceCurrentHp((target.getMaxHp() * Config.CHAMPION_HP) + 1, activeChar);
		}
		else
		{
			target.reduceCurrentHp(target.getMaxHp() + 1, activeChar);
		}
		
		if (Config.DEBUG)
		{
			_log.fine("GM: " + activeChar.getName() + "(" + activeChar.getObjectId() + ")" + " killed character " + target.getObjectId());
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}