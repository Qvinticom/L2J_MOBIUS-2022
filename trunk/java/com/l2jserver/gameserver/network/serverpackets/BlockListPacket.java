/*
 * Copyright (C) 2004-2014 L2J Server
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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.List;

import com.l2jserver.gameserver.datatables.CharNameTable;

/**
 * @author Sdw
 */
public class BlockListPacket extends L2GameServerPacket
{
	private final List<Integer> _playersId;
	
	public BlockListPacket(List<Integer> playersId)
	{
		_playersId = playersId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xD5);
		writeD(_playersId.size());
		for (int playerId : _playersId)
		{
			writeS(CharNameTable.getInstance().getNameById(playerId));
			writeS(""); // memo ?
		}
	}
}
