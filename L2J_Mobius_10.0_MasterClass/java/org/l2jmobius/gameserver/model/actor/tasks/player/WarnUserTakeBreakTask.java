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
package org.l2jmobius.gameserver.model.actor.tasks.player;

import java.util.concurrent.TimeUnit;

import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Task dedicated to warn user to take a break.
 * @author UnAfraid
 */
public class WarnUserTakeBreakTask implements Runnable
{
	private final Player _player;
	
	public WarnUserTakeBreakTask(Player player)
	{
		_player = player;
	}
	
	@Override
	public void run()
	{
		if (_player != null)
		{
			if (_player.isOnline())
			{
				final long hours = TimeUnit.MILLISECONDS.toHours(_player.getUptime());
				_player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_PLAYED_FOR_S1_H_TAKE_A_BREAK_PLEASE).addLong(hours));
			}
			else
			{
				_player.stopWarnUserTakeBreak();
			}
		}
	}
}
