/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets.friend;

import java.util.Map;

import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Erlandys
 */
public class BlockListPacket extends L2GameServerPacket
{
	private final L2PcInstance player;
	
	public BlockListPacket(L2PcInstance activeChar)
	{
		player = activeChar;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xD5);
		writeD(player.getBlockList().getBlockList().size());
		for (Map.Entry<Integer, String> entry : player.getBlockList().getBlockList().entrySet())
		{
			writeS(CharNameTable.getInstance().getNameById(entry.getKey()));
			writeS(entry.getValue());
		}
		
	}
}
