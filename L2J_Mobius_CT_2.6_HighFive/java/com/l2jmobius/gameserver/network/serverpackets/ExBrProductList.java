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

import com.l2jmobius.gameserver.data.xml.impl.ItemMallData;
import com.l2jmobius.gameserver.model.ItemMallProduct;

/**
 * @author Mobius
 */
public class ExBrProductList extends L2GameServerPacket
{
	private final Collection<ItemMallProduct> _itemList = ItemMallData.getInstance().getAllItems();
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xD6);
		writeD(_itemList.size());
		
		for (ItemMallProduct product : _itemList)
		{
			final int category = product.getCategory();
			
			writeD(product.getProductId()); // product id
			writeH(category); // category id
			writeD(product.getPrice()); // points
			
			switch (category)
			{
				case 6:
				{
					writeD(0x01); // event
					break;
				}
				case 7:
				{
					writeD(0x02); // best
					break;
				}
				case 8:
				{
					writeD(0x03); // event & best
					break;
				}
				default:
				{
					writeD(0x00); // normal
					break;
				}
			}
			
			writeD(0x00); // start sale
			writeD(0x00); // end sale
			writeC(0x00); // day week
			writeC(0x00); // start hour
			writeC(0x00); // start min
			writeC(0x00); // end hour
			writeC(0x00); // end min
			writeD(0x00); // current stock
			writeD(0x00); // max stock
		}
	}
}
