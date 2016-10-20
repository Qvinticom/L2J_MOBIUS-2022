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

import java.util.Calendar;

import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Friend;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Sdw
 */
public class ExFriendDetailInfo extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final String _name;
	
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
		final Friend friend = _player.getFriend(CharNameTable.getInstance().getIdByName(_name));
		
		final L2PcInstance player = friend.getFriend();
		if (player == null)
		{
			writeS(_name);
			writeD(0x00);
			writeD(0x00);
			writeH(friend.getLevel());
			writeH(friend.getClassId());
			writeD(friend.getClanId());
			writeD(friend.getClanCrestId());
			writeS(friend.getClanName());
			writeD(friend.getAllyId());
			writeD(friend.getAllyCrestId());
			writeS(friend.getAllyName());
			final Calendar createDate = Calendar.getInstance();
			createDate.setTimeInMillis(friend.getCreateDate());
			writeC(createDate.get(Calendar.MONTH) + 1);
			writeC(createDate.get(Calendar.DAY_OF_MONTH));
			writeD((int) ((System.currentTimeMillis() - friend.getLastLogin()) / 1000));
		}
		else
		{
			writeS(player.getName());
			writeD(player.isOnlineInt());
			writeD(player.getObjectId());
			writeH(player.getLevel());
			writeH(player.getClassId().getId());
			writeD(player.getClanId());
			writeD(player.getClanCrestId());
			writeS(player.getClan() != null ? player.getClan().getName() : "");
			writeD(player.getAllyId());
			writeD(player.getAllyCrestId());
			writeS(player.getClan() != null ? player.getClan().getAllyName() : "");
			final Calendar createDate = player.getCreateDate();
			writeC(createDate.get(Calendar.MONTH) + 1);
			writeC(createDate.get(Calendar.DAY_OF_MONTH));
			writeD(player.isOnline() ? -1 : (int) ((System.currentTimeMillis() - player.getLastAccess()) / 1000));
		}
		writeS(friend.getMemo()); // memo
	}
}
