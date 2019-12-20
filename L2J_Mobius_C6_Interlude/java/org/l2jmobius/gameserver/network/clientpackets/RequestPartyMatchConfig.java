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
import org.l2jmobius.gameserver.model.PartyMatchWaitingList;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import org.l2jmobius.gameserver.network.serverpackets.PartyMatchList;

public class RequestPartyMatchConfig extends GameClientPacket
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
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.isInPartyMatchRoom() && (player.getParty() != null) && (player.getParty().getLeader() != player))
		{
			player.sendPacket(SystemMessageId.CANT_VIEW_PARTY_ROOMS);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
			if (list == null)
			{
				return;
			}
			
			final PartyMatchRoom room = list.getPlayerRoom(player);
			if (room == null)
			{
				return;
			}
			
			player.sendPacket(new PartyMatchDetail(room));
			player.sendPacket(new ExPartyRoomMember(room, 2));
			
			player.setPartyRoom(room.getId());
			player.broadcastUserInfo();
		}
		else
		{
			// Add to waiting list
			PartyMatchWaitingList.getInstance().addPlayer(player);
			
			// Send Room list
			player.sendPacket(new PartyMatchList(player, _auto, _loc, _lvl));
		}
	}
}