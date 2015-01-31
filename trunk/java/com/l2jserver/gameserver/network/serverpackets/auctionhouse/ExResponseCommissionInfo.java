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
package com.l2jserver.gameserver.network.serverpackets.auctionhouse;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Erlandys
 */
public class ExResponseCommissionInfo extends L2GameServerPacket
{
	L2PcInstance player;
	L2ItemInstance item;
	boolean success;
	
	public ExResponseCommissionInfo(L2PcInstance _player, int _itemOID, boolean _success)
	{
		player = _player;
		item = player.getInventory().getItemByObjectId(_itemOID);
		success = _success;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF4);
		writeD(success ? 0x01 : 0x00); // TODO: Success
		writeD(0x00); // ItemID
		writeD(0x00); // TODO: Price
		writeQ(0x00); // TODO: Count
		writeD(0x00); // TODO: Duration
		writeD(-0x01); // TODO: Unknown
		writeD(0x00); // TODO: Unknown
	}
}
