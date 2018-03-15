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

import com.l2jmobius.gameserver.data.xml.impl.ItemMallData;
import com.l2jmobius.gameserver.model.ItemMallProduct;

/**
 * @author Mobius
 */
public class ExBrProductInfo extends L2GameServerPacket
{
	private final ItemMallProduct _product;
	
	public ExBrProductInfo(int id)
	{
		_product = ItemMallData.getInstance().getProduct(id);
	}
	
	@Override
	protected void writeImpl()
	{
		if (_product == null)
		{
			return;
		}
		
		writeC(0xFE);
		writeH(0xD7);
		
		writeD(_product.getProductId()); // product id
		writeD(_product.getPrice()); // points
		writeD(1); // components size
		writeD(_product.getItemId()); // item id
		writeD(_product.getItemCount()); // quality
		writeD(_product.getItemWeight()); // weight
		writeD(_product.isTradable() ? 1 : 0); // 0 - dont drop/trade
	}
}
