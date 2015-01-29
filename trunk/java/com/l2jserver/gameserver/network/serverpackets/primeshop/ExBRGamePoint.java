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
package com.l2jserver.gameserver.network.serverpackets.primeshop;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Gnacik, UnAfraid
 */
public class ExBRGamePoint extends L2GameServerPacket
{
	private final int _charId;
	private final int _charPoints;
	
	public ExBRGamePoint(final L2PcInstance player)
	{
		_charId = player.getObjectId();
		_charPoints = player.getPrimePoints();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD6);
		writeD(_charId);
		writeQ(_charPoints);
		writeD(0x00);
	}
}
