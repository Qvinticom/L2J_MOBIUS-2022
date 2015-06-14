/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets.friend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Level;

import com.l2jserver.L2DatabaseFactory;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.friend.FriendAddRequestResult;
import com.l2jserver.gameserver.network.serverpackets.friend.FriendList;

public final class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private static final String _C__78_REQUESTANSWERFRIENDINVITE = "[C] 78 RequestAnswerFriendInvite";
	
	private int _response;
	
	@Override
	protected void readImpl()
	{
		_response = readC();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2PcInstance requestor = player.getActiveRequester();
		if (requestor == null)
		{
			return;
		}
		
		if (player.getFriendList().containsValue(requestor.getObjectId()) //
			|| requestor.getFriendList().containsValue(player.getObjectId()))
		{
			requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_PLAYER_IS_ALREADY_REGISTERED_ON_YOUR_FRIENDS_LIST));
			return;
		}
		
		if (_response == 1)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO character_friends (charId, friendId) VALUES (?, ?), (?, ?)"))
			{
				statement.setInt(1, requestor.getObjectId());
				statement.setInt(2, player.getObjectId());
				statement.setInt(3, player.getObjectId());
				statement.setInt(4, requestor.getObjectId());
				statement.execute();
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST);
				requestor.sendPacket(msg);
				
				// Player added to your friend list
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ADDED_TO_YOUR_FRIENDS_LIST);
				msg.addString(player.getName());
				requestor.sendPacket(msg);
				requestor.addFriend(player);
				
				// has joined as friend.
				msg = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_AS_A_FRIEND);
				msg.addString(requestor.getName());
				player.sendPacket(msg);
				player.addFriend(requestor);
				
				// Send notifications for both player in order to show them online
				player.sendPacket(new FriendAddRequestResult(requestor, 1));
				requestor.sendPacket(new FriendAddRequestResult(player, 1));
				player.sendPacket(new FriendList(player));
				requestor.sendPacket(new FriendList(requestor));
			}
			catch (Exception e)
			{
				_log.log(Level.WARNING, "Could not add friend objectid: " + e.getMessage(), e);
			}
		}
		else
		{
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_FAILED_TO_ADD_A_FRIEND_TO_YOUR_FRIENDS_LIST);
			requestor.sendPacket(msg);
			player.sendPacket(new FriendAddRequestResult(requestor, 0));
			requestor.sendPacket(new FriendAddRequestResult(player, 0));
		}
		
		player.setActiveRequester(null);
		requestor.onTransactionResponse();
	}
	
	@Override
	public String getType()
	{
		return _C__78_REQUESTANSWERFRIENDINVITE;
	}
}
