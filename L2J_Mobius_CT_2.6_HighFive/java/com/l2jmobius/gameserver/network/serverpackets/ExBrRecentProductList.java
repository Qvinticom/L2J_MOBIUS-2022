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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.data.xml.impl.ItemMallData;
import com.l2jmobius.gameserver.model.ItemMallProduct;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Mobius
 */
public class ExBrRecentProductList extends L2GameServerPacket
{
	private final List<ItemMallProduct> _itemList = new ArrayList<>();
	
	public ExBrRecentProductList(L2PcInstance player)
	{
		final int playerObj = player.getObjectId();
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT productId FROM item_mall_transactions WHERE charId=? ORDER BY transactionTime DESC"))
		{
			statement.setInt(1, playerObj);
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final ItemMallProduct product = ItemMallData.getInstance().getProduct(rset.getInt("productId"));
					if ((product != null) && !_itemList.contains(product))
					{
						_itemList.add(product);
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Could not restore Item Mall transaction: " + e.getMessage(), e);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		if ((_itemList == null) || _itemList.isEmpty())
		{
			return;
		}
		
		writeC(0xFE);
		writeH(0xDC);
		writeD(_itemList.size());
		
		for (ItemMallProduct product : _itemList)
		{
			writeD(product.getProductId());
			writeH(product.getCategory());
			writeD(product.getPrice());
			writeD(0x00); // category
			
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
