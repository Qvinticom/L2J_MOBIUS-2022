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

import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.PartyMatchWaitingList;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchList;

public final class RequestPartyMatchConfig extends GameClientPacket
{
	private int _auto;
	private int _loc;
	private int _lvl;
	
	@Override
	protected void readImpl()
	{
		_auto = readD();
		_loc = readD();
		_lvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance _player = getClient().getPlayer();
		if (_player == null)
		{
			return;
		}
		
		if (!_player.isInPartyMatchRoom() && (_player.getParty() != null) && (_player.getParty().getLeader() != _player))
		{
			_player.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
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
			_player.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(_player);
			
			// Send Room list
			_player.sendPacket(new PartyMatchList(_player, _auto, _loc, _lvl));
		}
	}
}