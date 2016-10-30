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

import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2Party.MessageType;
import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.request.PartyRequest;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.JoinParty;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public final class RequestAnswerJoinParty extends L2GameClientPacket
{
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final PartyRequest request = player.getRequest(PartyRequest.class);
		if ((request == null) || request.isProcessing() || !player.removeRequest(request.getClass()))
		{
			return;
		}
		request.setProcessing(true);
		
		final L2PcInstance requestor = request.getActiveChar();
		if (requestor == null)
		{
			return;
		}
		
		final L2Party party = request.getParty();
		final L2Party requestorParty = requestor.getParty();
		if ((requestorParty != null) && (requestorParty != party))
		{
			return;
		}
		
		requestor.sendPacket(new JoinParty(_response));
		
		if (_response == 1)
		{
			if (party.getMemberCount() >= 7)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THE_PARTY_IS_FULL);
				player.sendPacket(sm);
				requestor.sendPacket(sm);
				return;
			}
			
			// Assign the party to the leader upon accept of his partner
			if (requestorParty == null)
			{
				requestor.setParty(party);
			}
			
			player.joinParty(party);
			
			if (requestor.isInPartyMatchRoom())
			{
				final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
				if (player.isInPartyMatchRoom())
				{
					if (list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player))
					{
						final PartyMatchRoom room = list.getPlayerRoom(requestor);
						if (room != null)
						{
							final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
							room.getPartyMembers().forEach(packet::sendTo);
						}
					}
				}
				else
				{
					final PartyMatchRoom room = list.getPlayerRoom(requestor);
					if (room != null)
					{
						room.addMember(player);
						final ExManagePartyRoomMember packet = new ExManagePartyRoomMember(player, room, 1);
						room.getPartyMembers().forEach(packet::sendTo);
						player.setPartyRoom(room.getId());
						player.broadcastUserInfo();
					}
				}
			}
		}
		else if (_response == -1)
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_SET_TO_REFUSE_PARTY_REQUESTS_AND_CANNOT_RECEIVE_A_PARTY_REQUEST);
			sm.addPcName(player);
			requestor.sendPacket(sm);
			
			if (party.getMemberCount() == 1)
			{
				party.removePartyMember(requestor, MessageType.NONE);
			}
		}
		else
		{
			if (party.getMemberCount() == 1)
			{
				party.removePartyMember(requestor, MessageType.NONE);
			}
		}
		
		party.setPendingInvitation(false);
		request.setProcessing(false);
	}
}
