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
package com.l2jserver.gameserver.network.serverpackets.itemappearance;

import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Erlandys
 */
public class ExShape_Shifting_Result extends L2GameServerPacket
{
	private final int _success;
	private int _itemId = 0, _targetItemId = 0, _time = -1;
	
	public ExShape_Shifting_Result(int success)
	{
		_success = success;
	}
	
	public ExShape_Shifting_Result(int success, int itemId, int targetItemId)
	{
		_success = success;
		_itemId = itemId;
		_targetItemId = targetItemId;
	}
	
	public ExShape_Shifting_Result(int success, int itemId, int targetItemId, int time)
	{
		_success = success;
		_itemId = itemId;
		_targetItemId = targetItemId;
		_time = time;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x12C);
		writeD(_success); // Success - 1, Fail - 0
		writeD(_itemId); // targetItemId
		writeD(_targetItemId); // extractItemId
		writeD(_time); // time
	}
}
