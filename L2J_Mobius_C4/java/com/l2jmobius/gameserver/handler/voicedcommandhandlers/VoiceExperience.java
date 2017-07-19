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
package com.l2jmobius.gameserver.handler.voicedcommandhandlers;

import com.l2jmobius.gameserver.handler.IVoicedCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

public class VoiceExperience implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"expon",
		"expoff"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String target)
	{
		if (command.equalsIgnoreCase("expon"))
		{
			activeChar.setGainXpSp(true);
			activeChar.sendMessage("Experience Gain: Enabled.");
			activeChar.sendMessage("Skill Point Gain: Enabled.");
		}
		else if (command.equalsIgnoreCase("expoff"))
		{
			activeChar.setGainXpSp(false);
			activeChar.sendMessage("Experience Gain: Disabled.");
			activeChar.sendMessage("Skill Point Gain: Disabled.");
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}