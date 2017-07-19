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
import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jmobius.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.JoinParty;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * sample 2a 01 00 00 00 format cdd
 * @version $Revision: 1.7.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestAnswerJoinParty extends L2GameClientPacket
{
	private static final String _C__2A_REQUESTANSWERPARTY = "[C] 2A RequestAnswerJoinParty";
	// private static Logger _log = Logger.getLogger(RequestAnswerJoinParty.class.getName());
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (_response == 1)
		{
			// summary of ppl already in party and ppl that get invitation
			if (requestor.isInParty() && !requestor.getParty().isLeader(requestor))
			{
				requestor.sendPacket(new SystemMessage(SystemMessage.ONLY_LEADER_CAN_INVITE));
			}
			else if (requestor.isInParty() && (requestor.getParty().getMemberCount() >= 9))
			{
				requestor.sendPacket(new SystemMessage(SystemMessage.PARTY_FULL));
				player.sendPacket(new SystemMessage(SystemMessage.PARTY_FULL));
			}
			else if (requestor.isInParty() && requestor.getParty().isInDimensionalRift())
			{
				requestor.sendMessage("You cannot invite characters from another dimension.");
			}
			else if (player.isInJail() || requestor.isInJail())
			{
				requestor.sendMessage("Player is jailed.");
			}
			else if (requestor.inOfflineMode())
			{
				player.sendMessage("Requestor is in Offline mode.");
			}
			else if (player.inObserverMode() || requestor.inObserverMode())
			{
				player.sendMessage("A Party request cannot be done while one of the partners is in Observer mode.");
			}
			else if (player.isInOlympiadMode() || requestor.isInOlympiadMode())
			{
				player.sendMessage("A Party request cannot be done while one of the partners is in Olympiad mode.");
			}
			else if ((player.getEventTeam() > 0) && (player.getEventTeam() != requestor.getEventTeam()))
			{
				player.sendMessage("Player in TvT Event.");
			}
			else if ((requestor.getEventTeam() > 0) && (requestor.getEventTeam() != player.getEventTeam()))
			{
				requestor.sendMessage("Player in TvT Event.");
			}
			else if (player.isInParty())
			{
				SystemMessage msg = new SystemMessage(SystemMessage.S1_IS_ALREADY_IN_PARTY);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				msg = null;
			}
			else
			{
				if (!requestor.isInParty())
				{
					requestor.setParty(new L2Party(requestor));
				}
				
				player.joinParty(requestor.getParty());
				
				// Check everything in detail
				checkPartyMatchingConditions(requestor, player);
			}
		}
		else
		{
			requestor.sendPacket(new SystemMessage(SystemMessage.PLAYER_DECLINED));
		}
		
		requestor.sendPacket(new JoinParty(_response));
		
		requestor.setLootInvitation(-1);
		// just in case somebody manages to invite a requestor
		player.setLootInvitation(-1);
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	private void checkPartyMatchingConditions(L2PcInstance requestor, L2PcInstance player)
	{
		if (requestor.isInPartyMatchRoom())
		{
			final PartyMatchRoomList list = PartyMatchRoomList.getInstance();
			if (list != null)
			{
				final PartyMatchRoom room = list.getPlayerRoom(requestor);
				final PartyMatchRoom targetRoom = list.getPlayerRoom(player);
				if (player.isInPartyMatchRoom())
				{
					if (room.getId() != targetRoom.getId())
					{
						requestor.sendPacket(new ExClosePartyRoom());
						room.deleteMember(requestor);
						requestor.setPartyRoom(0);
						requestor.broadcastUserInfo();
						
						player.sendPacket(new ExClosePartyRoom());
						targetRoom.deleteMember(player);
						player.setPartyRoom(0);
					}
					else if (requestor != room.getOwner())
					{
						requestor.sendPacket(new ExClosePartyRoom());
						room.deleteMember(requestor);
						requestor.setPartyRoom(0);
						requestor.broadcastUserInfo();
						
						player.sendPacket(new ExClosePartyRoom());
						room.deleteMember(player);
						player.setPartyRoom(0);
					}
					else
					{
						for (final L2PcInstance member : room.getPartyMembers())
						{
							member.sendPacket(new ExManagePartyRoomMember(player, room, 1));
						}
					}
					player.broadcastUserInfo();
				}
				else
				{
					if (requestor != room.getOwner())
					{
						requestor.sendPacket(new ExClosePartyRoom());
						room.deleteMember(requestor);
						requestor.setPartyRoom(0);
						requestor.broadcastUserInfo();
					}
					else
					{
						room.addMember(player);
						player.setPartyRoom(room.getId());
						
						player.sendPacket(new PartyMatchDetail(room));
						player.sendPacket(new ExPartyRoomMember(player, room, 0));
						
						player.broadcastUserInfo();
						
						for (final L2PcInstance member : room.getPartyMembers())
						{
							member.sendPacket(new ExManagePartyRoomMember(player, room, 0));
						}
					}
				}
			}
		}
		else
		{
			final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(player);
			if (_room != null)
			{
				player.sendPacket(new ExClosePartyRoom());
				_room.deleteMember(player);
				player.setPartyRoom(0);
				player.broadcastUserInfo();
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__2A_REQUESTANSWERPARTY;
	}
}