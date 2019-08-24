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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;

/**
 * @author Beetle
 */
public class TradeUpdate extends GameServerPacket
{
	private final ItemInstance[] _items;
	private final TradeItem[] _trade_items;
	
	public TradeUpdate(TradeList trade, PlayerInstance player)
	{
		_items = player.getInventory().getItems();
		_trade_items = trade.getItems();
	}
	
	private int getItemCount(int objectId)
	{
		for (ItemInstance item : _items)
		{
			if (item.getObjectId() == objectId)
			{
				return item.getCount();
			}
		}
		return 0;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x74);
		
		writeH(_trade_items.length);
		for (TradeItem _item : _trade_items)
		{
			int _aveable_count = getItemCount(_item.getObjectId()) - _item.getCount();
			boolean _stackable = _item.getItem().isStackable();
			if (_aveable_count == 0)
			{
				_aveable_count = 1;
				_stackable = false;
			}
			writeH(_stackable ? 3 : 2);
			writeH(_item.getItem().getType1()); // item type1
			writeD(_item.getObjectId());
			writeD(_item.getItem().getItemId());
			writeD(_aveable_count);
			writeH(_item.getItem().getType2()); // item type2
			writeH(0x00); // ?
			writeD(_item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
			writeH(_item.getEnchant()); // enchant level
			writeH(0x00); // ?
			writeH(0x00);
		}
	}
}
