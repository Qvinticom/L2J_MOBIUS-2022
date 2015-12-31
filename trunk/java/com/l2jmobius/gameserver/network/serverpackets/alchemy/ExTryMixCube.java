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
package com.l2jmobius.gameserver.network.serverpackets.alchemy;

import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

public class ExTryMixCube extends L2GameServerPacket
{
	public static final L2GameServerPacket FAIL = new ExTryMixCube(6);
	private final int _result;
	private final int _itemId;
	private final long _itemCount;
	
	public ExTryMixCube(int result)
	{
		_result = result;
		_itemId = 0;
		_itemCount = 0;
	}
	
	public ExTryMixCube(int itemId, long itemCount)
	{
		_result = 0;
		_itemId = itemId;
		_itemCount = itemCount;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x175);
		writeC(_result);
		writeC(0x01);
		writeD(0x00); // 1=show bonus card, but cant't understand bonus count
		writeD(_itemId);
		writeQ(_itemCount);
	}
}
