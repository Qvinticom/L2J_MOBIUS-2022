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

import org.l2jmobius.gameserver.model.PartyMatchRoom;
import org.l2jmobius.gameserver.model.PartyMatchRoomList;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExClosePartyRoom;

/**
 * Format (ch) dd
 * @author -Wooden-
 */
public class RequestWithdrawPartyRoom extends GameClientPacket
{
	private int _roomid;
	@SuppressWarnings("unused")
	private int _unk1;
	
	@Override
	protected void readImpl()
	{
		_roomid = readD();
		_unk1 = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance _player = getClient().getPlayer();
		
		if (_player == null)
		{
			return;
		}
		
		final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (_room == null)
		{
			return;
		}
		
		if ((_player.isInParty() && _room.getOwner().isInParty()) && (_player.getParty().getPartyLeaderOID() == _room.getOwner().getParty().getPartyLeaderOID()))
		{
			// If user is in party with Room Owner is not removed from Room
		}
		else
		{
			_room.deleteMember(_player);
			_player.setPartyRoom(0);
			_player.broadcastUserInfo();
			
			_player.sendPacket(new ExClosePartyRoom());
			_player.sendPacket(SystemMessageId.PARTY_ROOM_EXITED);
		}
	}
}