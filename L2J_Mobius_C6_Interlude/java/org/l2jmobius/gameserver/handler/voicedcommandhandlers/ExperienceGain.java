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
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * This class allows user to turn XP-gain off and on.
 * @author Notorious
 */
public class ExperienceGain implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"xpoff",
		"xpon"
	};
	
	@Override
	public boolean useVoicedCommand(String command, Player activeChar, String params)
	{
		if (command.equalsIgnoreCase("xpoff"))
		{
			activeChar.setExpGain(false);
			activeChar.sendMessage("Experience gain is disabled.");
		}
		else if (command.equalsIgnoreCase("xpon"))
		{
			activeChar.setExpGain(true);
			activeChar.sendMessage("Experience gain is enabled.");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}