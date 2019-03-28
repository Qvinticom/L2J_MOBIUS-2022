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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.PartyMatchWaitingList;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.ListPartyWating;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public final class RequestPartyMatchConfig implements IClientIncomingPacket
{
	private int _auto;
	private int _loc;
	private int _lvl;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_auto = packet.readD(); //
		_loc = packet.readD(); // Location
		_lvl = packet.readD(); // my level
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance _player = client.getPlayer();
		
		if (_player == null)
		{
			return;
		}
		
		if (!_player.isInPartyMatchRoom() && (_player.getParty() != null) && (_player.getParty().getLeader() != _player))
		{
			_player.sendPacket(SystemMessageId.THE_LIST_OF_PARTY_ROOMS_CAN_ONLY_BE_VIEWED_BY_A_PERSON_WHO_IS_NOT_PART_OF_A_PARTY);
			_player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_player.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			final PartyMatchRoomList _list = PartyMatchRoomList.getInstance();
			if (_list == null)
			{
				return;
			}
			
			final PartyMatchRoom _room = _list.getPlayerRoom(_player);
			if (_room == null)
			{
				return;
			}
			
			_player.sendPacket(new PartyMatchDetail(_player, _room));
			_player.sendPacket(new ExPartyRoomMember(_player, _room, 2));
			
			_player.setPartyRoom(_room.getId());
			// _activeChar.setPartyMatching(1);
			_player.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(_player);
			
			// Send Room list
			final ListPartyWating matchList = new ListPartyWating(_player, _auto, _loc, _lvl);
			
			_player.sendPacket(matchList);
		}
	}
}
