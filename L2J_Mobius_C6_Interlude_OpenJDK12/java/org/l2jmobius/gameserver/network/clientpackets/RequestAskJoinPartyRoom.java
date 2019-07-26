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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExAskJoinPartyRoom;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) S
 * @author -Wooden-
 */
public class RequestAskJoinPartyRoom extends GameClientPacket
{
	private static String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance _player = getClient().getPlayer();
		if (_player == null)
		{
			return;
		}
		
		// Send PartyRoom invite request (with activeChar) name to the target
		final PlayerInstance _target = World.getInstance().getPlayer(_name);
		if (_target != null)
		{
			if (!_target.isProcessingRequest())
			{
				_player.onTransactionRequest(_target);
				_target.sendPacket(new ExAskJoinPartyRoom(_player.getName()));
			}
			else
			{
				_player.sendPacket(new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(_target.getName()));
			}
		}
		else
		{
			_player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
		}
	}
}
