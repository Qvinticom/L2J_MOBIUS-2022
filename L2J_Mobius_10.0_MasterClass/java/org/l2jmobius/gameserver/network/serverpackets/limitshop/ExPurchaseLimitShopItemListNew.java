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
import java.util.Collections;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.xml.LimitShopCraftData;
import org.l2jmobius.gameserver.data.xml.LimitShopData;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.LimitShopProductHolder;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExPurchaseLimitShopItemListNew implements IClientOutgoingPacket
{
	private final int _shopType; // 3 Lcoin Store, 4 Special Craft, 100 Clan Shop
	private final PlayerInstance _player;
	private final Collection<LimitShopProductHolder> _products;
	
	public ExPurchaseLimitShopItemListNew(int shopType, PlayerInstance player)
	{
		_shopType = shopType;
		_player = player;
		
		switch (shopType)
		{
			case 4: // Lcoin Special Craft
			{
				_products = LimitShopCraftData.getInstance().getProducts();
				break;
			}
			case 7: // Normal Lcoin Shop
			{
				_products = LimitShopData.getInstance().getProducts();
				break;
			}
			default:
			{
				_products = Collections.emptyList();
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PURCHASE_LIMIT_SHOP_ITEM_LIST_NEW.writeId(packet);
		
		packet.writeC(_shopType);
		packet.writeC(0x01); // Page. (311)
		packet.writeC(0x01); // MaxPage. (311)
		packet.writeD(_products.size());
		for (LimitShopProductHolder product : _products)
		{
			packet.writeD(product.getId());
			packet.writeD(product.getProductionId());
			packet.writeD(product.getIngredientIds()[0]);
			packet.writeD(product.getIngredientIds()[1]);
			packet.writeD(product.getIngredientIds()[2]);
			packet.writeD(product.getIngredientIds()[3]); // 306
			packet.writeD(product.getIngredientIds()[4]); // 306
			packet.writeQ(product.getIngredientQuantities()[0]);
			packet.writeQ(product.getIngredientQuantities()[1]);
			packet.writeQ(product.getIngredientQuantities()[2]);
			packet.writeQ(product.getIngredientQuantities()[3]); // 306
			packet.writeQ(product.getIngredientQuantities()[4]); // 306
			packet.writeH(product.getIngredientEnchants()[0]);
			packet.writeH(product.getIngredientEnchants()[1]);
			packet.writeH(product.getIngredientEnchants()[2]);
			packet.writeH(product.getIngredientEnchants()[3]); // 306
			packet.writeH(product.getIngredientEnchants()[4]); // 306
			
			// Check limits.
			if (product.getAccountDailyLimit() > 0) // Sale period.
			{
				if (_player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0) >= product.getAccountDailyLimit())
				{
					if ((_player.getAccountVariables().getLong(AccountVariables.LCOIN_SHOP_PRODUCT_TIME + product.getProductionId(), 0) + 86400000) > Chronos.currentTimeMillis())
					{
						packet.writeD(0x00);
					}
					else // Reset limit.
					{
						_player.getAccountVariables().remove(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId());
						packet.writeD(product.getAccountDailyLimit());
					}
				}
				else
				{
					packet.writeD(product.getAccountDailyLimit() - _player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0));
				}
			}
			else if (product.getAccountBuyLimit() > 0) // Count limit.
			{
				if (_player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0) >= product.getAccountBuyLimit())
				{
					packet.writeD(0x00);
				}
				else
				{
					packet.writeD(product.getAccountBuyLimit() - _player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0));
				}
			}
			else // No account limits.
			{
				packet.writeD(0x01);
			}
			packet.writeD(0x00); // nRemainSec
			packet.writeD(0x00); // nRemainServerItemAmount
			packet.writeH(0x00); // sCircleNum (311)
		}
		
		return true;
	}
}
