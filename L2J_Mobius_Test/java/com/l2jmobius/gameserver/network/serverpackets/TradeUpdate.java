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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.model.TradeItem;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author daemon
 */
public class TradeUpdate extends AbstractItemPacket
{
	private final TradeItem _item;
	private final long _newCount;
	
	public TradeUpdate(L2PcInstance player, TradeItem item)
	{
		_item = item;
		_newCount = player.getInventory().getItemByObjectId(item.getObjectId()).getCount() - item.getCount();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0x81);
		writeH(1);
		writeH((_newCount > 0) && _item.getItem().isStackable() ? 3 : 2);
		writeTradeItem(_item);
	}
}
