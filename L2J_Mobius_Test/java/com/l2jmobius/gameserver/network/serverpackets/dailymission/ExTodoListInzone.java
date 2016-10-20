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
package com.l2jmobius.gameserver.network.serverpackets.dailymission;

import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Mobius
 */
public class ExTodoListInzone extends L2GameServerPacket
{
	public ExTodoListInzone()
	{
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x18B);
		writeH(0x00); // zone list size: if not select show all levels, this size=0, maybe I am LV3.
		writeS(""); // HTML name
		writeS(""); // Zone name
		writeH(0x00); // Min level
		writeH(0x00); // Max level
		writeH(0x00); // Min players
		writeH(0x00); // Max players
		writeC(0x00); // Entry info 1=on 2=off 3=always
	}
}
