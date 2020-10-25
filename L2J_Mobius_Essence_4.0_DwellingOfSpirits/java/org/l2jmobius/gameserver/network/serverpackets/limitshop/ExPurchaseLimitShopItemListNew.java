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
package org.l2jmobius.gameserver.network.serverpackets.limitshop;

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.impl.LCoinShopData;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.LCoinShopProductHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExPurchaseLimitShopItemListNew implements IClientOutgoingPacket
{
	private final int _category;
	private final PlayerInstance _player;
	
	public ExPurchaseLimitShopItemListNew(int category, PlayerInstance player)
	{
		_category = category;
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PURCHASE_LIMIT_SHOP_ITEM_LIST_NEW.writeId(packet);
		
		final Collection<LCoinShopProductHolder> products = LCoinShopData.getInstance().getProducts();
		
		packet.writeC(_category); // _category? Main shop?
		packet.writeD(products.size());
		
		for (LCoinShopProductHolder product : products)
		{
			packet.writeD(product.getId());
			packet.writeD(product.getProductionId());
			packet.writeD(product.getIngredientIds()[0]);
			packet.writeD(product.getIngredientIds()[1]);
			packet.writeD(product.getIngredientIds()[2]);
			packet.writeQ(product.getIngredientQuantities()[0]);
			packet.writeQ(product.getIngredientQuantities()[1]);
			packet.writeQ(product.getIngredientQuantities()[2]);
			
			packet.writeH(0x00); // ?
			
			packet.writeC(-1); // remaining amount?
			packet.writeC(-1); // remaining time?
			
			packet.writeC(-1); // ?
			packet.writeC(-1); // ?
			
			// Sale period.
			if (product.getAccountDailyLimit() > 0)
			{
				if (_player.getAccountVariables().getInt("LCSCount" + product.getProductionId(), 0) >= product.getAccountDailyLimit())
				{
					if ((_player.getAccountVariables().getLong("LCSTime" + product.getProductionId(), 0) + 86400000) > System.currentTimeMillis())
					{
						packet.writeD(0x00);
					}
					else // Reset limit.
					{
						_player.getAccountVariables().remove("LCSCount" + product.getProductionId());
						packet.writeD(product.getAccountDailyLimit());
					}
				}
				else
				{
					packet.writeD(product.getAccountDailyLimit() - _player.getAccountVariables().getInt("LCSCount" + product.getProductionId(), 0));
				}
			}
			else // No account daily limit.
			{
				packet.writeD(0x01);
			}
			packet.writeD(0x00);
			packet.writeD(0x00);
		}
		
		return true;
	}
}
