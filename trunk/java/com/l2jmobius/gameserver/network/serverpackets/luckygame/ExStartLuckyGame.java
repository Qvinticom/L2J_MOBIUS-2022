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
package com.l2jmobius.gameserver.network.serverpackets.luckygame;

import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Mobius
 */
public class ExStartLuckyGame extends L2GameServerPacket
{
	private static final int FORTUNE_READING_TICKET = 23767;
	private static final int LUXURY_FORTUNE_READING_TICKET = 23768;
	private int _type = 0;
	
	public ExStartLuckyGame(int type)
	{
		_type = type;
	}
	
	@Override
	protected void writeImpl()
	{
		final long count = getClient().getActiveChar().getInventory().getInventoryItemCount(_type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, -1);
		
		writeC(0xFE);
		writeH(0x160);
		writeD(_type);
		writeD((int) count);
	}
}
