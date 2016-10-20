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
package com.l2jmobius.gameserver.network.serverpackets.friend;

import java.util.Collection;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Friend;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Erlandys
 */
public class FriendList extends L2GameServerPacket
{
	Collection<Friend> _friends;
	
	public FriendList(L2PcInstance player)
	{
		_friends = player.getFriendList().values();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x58);
		writeD(_friends.size());
		for (Friend friend : _friends)
		{
			final L2PcInstance player = friend.getFriend();
			if (player != null)
			{
				writeD(player.getObjectId());
				writeS(player.getName());
				writeD(0x01);
				writeD(player.getObjectId());
				writeD(player.getLevel());
				writeD(player.getClassId().getId());
			}
			else
			{
				writeD(friend.getFriendOID());
				writeS(friend.getName());
				writeD(0x00);
				writeD(0x00);
				writeD(friend.getLevel());
				writeD(friend.getClassId());
			}
			writeS("testing");
		}
	}
}
