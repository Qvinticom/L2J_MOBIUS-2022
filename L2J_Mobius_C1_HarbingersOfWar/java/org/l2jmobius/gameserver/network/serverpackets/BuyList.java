/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.List;

import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;

public class BuyList extends ServerBasePacket
{
	private final TradeList _list;
	private final int _money;
	
	public BuyList(TradeList list, int currentMoney)
	{
		_list = list;
		_money = currentMoney;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x1D);
		writeD(_money);
		writeD(_list.getListId());
		final int count = _list.getItems().size();
		writeH(count);
		final List<ItemInstance> items = _list.getItems();
		for (int i = 0; i < count; ++i)
		{
			final ItemInstance temp = items.get(i);
			final int type = temp.getItem().getType1();
			writeH(type);
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(0);
			if (type < 4)
			{
				writeD(temp.getItem().getBodyPart());
				writeH(temp.getEnchantLevel());
				writeH(0);
				writeH(0);
			}
			writeD(temp.getPrice());
		}
	}
}
