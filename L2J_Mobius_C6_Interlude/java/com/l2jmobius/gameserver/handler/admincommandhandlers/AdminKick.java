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

import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.LeaveWorld;

public class AdminKick implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_kick",
		"admin_kick_non_gm"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		/*
		 * if(!AdminCommandAccessRights.getInstance().hasAccess(command, activeChar.getAccessLevel())){ return false; } if(Config.GMAUDIT) { Logger _logAudit = Logger.getLogger("gmaudit"); LogRecord record = new LogRecord(Level.INFO, command); record.setParameters(new Object[] { "GM: " +
		 * activeChar.getName(), " to target [" + activeChar.getTarget() + "] " }); _logAudit.LOGGER(record); }
		 */
		
		if (command.startsWith("admin_kick"))
		{
			StringTokenizer st = new StringTokenizer(command);
			
			if (activeChar.getTarget() != null)
			{
				activeChar.sendMessage("Type //kick name");
			}
			
			if (st.countTokens() > 1)
			{
				st.nextToken();
				
				final String player = st.nextToken();
				final L2PcInstance plyr = L2World.getInstance().getPlayer(player);
				
				if (plyr != null)
				{
					plyr.logout(true);
					activeChar.sendMessage("You kicked " + plyr.getName() + " from the game.");
				}
				
				if ((plyr != null) && plyr.isInOfflineMode())
				{
					plyr.deleteMe();
					activeChar.sendMessage("You kicked Offline Player " + plyr.getName() + " from the game.");
				}
			}
		}
		
		if (command.startsWith("admin_kick_non_gm"))
		{
			int counter = 0;
			
			for (L2PcInstance player : L2World.getInstance().getAllPlayers())
			{
				if (!player.isGM())
				{
					counter++;
					player.sendPacket(new LeaveWorld());
					player.logout(true);
				}
			}
			activeChar.sendMessage("Kicked " + counter + " players");
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
}
