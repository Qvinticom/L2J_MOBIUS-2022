/*
 * Copyright (C) 2004-2014 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.listeners.ExperienceGain;

import com.l2jserver.gameserver.handler.IVoicedCommandHandler;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.impl.character.playable.OnPlayableExpChanged;
import com.l2jserver.gameserver.model.events.listeners.FunctionEventListener;
import com.l2jserver.gameserver.model.events.returns.TerminateReturn;

/**
 * @author xban1x
 */
public final class ExperienceGain implements IVoicedCommandHandler
{
	private final String[] COMMANDS = new String[]
	{
		"xpoff",
		"xpon",
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (command.equals("xpoff"))
		{
			if (!activeChar.getVariables().getBoolean("XPOFF", false))
			{
				activeChar.addListener(new FunctionEventListener(activeChar, EventType.ON_PLAYABLE_EXP_CHANGED, (OnPlayableExpChanged event) -> onExperienceReceived(event.getActiveChar(), event.getNewExp() - event.getOldExp()), this));
				activeChar.getVariables().set("XPOFF", true);
				activeChar.sendMessage("Experience gain is disabled.");
			}
		}
		else if (command.equals("xpon"))
		{
			if (activeChar.getVariables().getBoolean("XPOFF", false))
			{
				activeChar.removeListenerIf(EventType.ON_PLAYABLE_EXP_CHANGED, listener -> listener.getOwner() == this);
				activeChar.getVariables().set("XPOFF", false);
				activeChar.sendMessage("Experience gain is enabled.");
			}
		}
		return true;
	}
	
	public TerminateReturn onExperienceReceived(L2Playable playable, long exp)
	{
		return new TerminateReturn(true, true, true);
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return COMMANDS;
	}
}
