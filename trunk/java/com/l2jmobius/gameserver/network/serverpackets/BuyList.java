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

import java.util.Collection;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.buylist.L2BuyList;
import com.l2jmobius.gameserver.model.buylist.Product;

public final class BuyList extends AbstractItemPacket
{
	private final int _listId;
	private final Collection<Product> _list;
	private final long _money;
	private double _taxRate = 0;
	
	public BuyList(L2BuyList list, long currentMoney, double taxRate)
	{
		_listId = list.getListId();
		_list = list.getProducts();
		_money = currentMoney;
		_taxRate = taxRate;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xB8);
		writeD(0x00);
		writeQ(_money); // current money
		writeD(_listId);
		writeD(0x00); // TODO: Find me
		writeH(_list.size());
		
		for (Product product : _list)
		{
			if ((product.getCount() > 0) || !product.hasLimitedStock())
			{
				writeItem(product);
				
				if ((product.getItemId() >= 3960) && (product.getItemId() <= 4026))
				{
					writeQ((long) (product.getPrice() * Config.RATE_SIEGE_GUARDS_PRICE * (1 + _taxRate)));
				}
				else
				{
					writeQ((long) (product.getPrice() * (1 + _taxRate)));
				}
			}
		}
	}
}