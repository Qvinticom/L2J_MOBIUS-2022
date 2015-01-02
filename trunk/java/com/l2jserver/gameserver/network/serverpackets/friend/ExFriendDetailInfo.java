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
package com.l2jserver.gameserver.network.serverpackets.friend;

import java.util.Calendar;

import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Sdw
 */
public class ExFriendDetailInfo extends L2GameServerPacket
{
	final L2PcInstance _player;
	final String _name;
	
	public ExFriendDetailInfo(L2PcInstance player, String name)
	{
		_player = player;
		_name = name;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xEC);
		
		writeD(_player.getObjectId());
		
		L2PcInstance friend = L2World.getInstance().getPlayer(_name);
		if (friend == null)
		{
			writeS(_name);
			writeD(0);
			writeD(0);
			writeH(0);
			writeH(0);
			writeD(0);
			writeD(0);
			writeS("");
			writeD(0);
			writeD(0);
			writeS("");
			writeD(0);
			writeS(""); // memo
		}
		else
		{
			writeS(friend.getName());
			writeD(friend.isOnlineInt());
			writeD(friend.getObjectId());
			writeH(friend.getLevel());
			writeH(friend.getClassIndex());
			writeD(friend.getClanId());
			writeD(friend.getClanCrestId());
			writeS(friend.getClan() != null ? friend.getClan().getName() : "");
			writeD(friend.getAllyId());
			writeD(friend.getAllyCrestId());
			writeS(friend.getClan() != null ? friend.getClan().getAllyName() : "");
			Calendar createDate = friend.getCreateDate();
			writeC(createDate.get(Calendar.MONTH));
			writeC(createDate.get(Calendar.DAY_OF_MONTH));
			writeD(friend.isOnline() ? (int) System.currentTimeMillis() : (int) (System.currentTimeMillis() - friend.getLastAccess()) / 1000);
			writeS(""); // memo
		}
	}
}
