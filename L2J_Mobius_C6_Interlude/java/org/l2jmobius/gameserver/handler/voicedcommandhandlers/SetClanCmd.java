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
package org.l2jmobius.gameserver.handler.voicedcommandhandlers;

import org.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SetClanCmd implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"set name",
		"set home",
		"set group"
	};
	
	@Override
	public boolean useVoicedCommand(String command, PlayerInstance activeChar, String target)
	{
		if (command.startsWith("set privileges"))
		{
			final int n = Integer.parseInt(command.substring(15));
			PlayerInstance pc = (PlayerInstance) activeChar.getTarget();
			if ((pc != null) && (((activeChar.getClan().getClanId() == pc.getClan().getClanId()) && (activeChar.getClanPrivileges() > n)) || activeChar.isClanLeader()))
			{
				pc.setClanPrivileges(n);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Your clan privileges have been set to " + n + " by " + activeChar.getName());
				activeChar.sendPacket(sm);
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}
