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
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.xml.LCoinShopData;
import org.l2jmobius.gameserver.data.xml.LCoinShopSpecialCraftData;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.LCoinShopProductHolder;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExPurchaseLimitShopItemListNew implements IClientOutgoingPacket
{
	private final int _shopType; // 3 = Lcoin Shop - 4 = Special Craft
	private final PlayerInstance _player;
	private Collection<LCoinShopProductHolder> _products;
	
	public ExPurchaseLimitShopItemListNew(int shopType, PlayerInstance player)
	{
		_shopType = shopType;
		_player = player;
		_products = null;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PURCHASE_LIMIT_SHOP_ITEM_LIST_NEW.writeId(packet);
		
		switch (_shopType)
		{
			case 3: // Normal Lcoin Shop
			{
				_products = LCoinShopData.getInstance().getProducts();
				break;
			}
			case 4: // Lcoin Special Craft
			{
				_products = LCoinShopSpecialCraftData.getInstance().getProducts();
				break;
			}
			default:
			{
				_products = LCoinShopData.getInstance().getProducts();
			}
		}
		
		packet.writeC(_shopType); //
		packet.writeD(_products.size());
		
		for (LCoinShopProductHolder product : _products)
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
			
			packet.writeH(0x00); // ?
			packet.writeH(0x00); // ? 306
			packet.writeH(0x00); // ? 306
			
			packet.writeC(-1); // remaining amount?
			packet.writeC(-1); // remaining time?
			
			packet.writeC(-1); // ?
			packet.writeC(-1); // ?
			
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
			packet.writeD(0x00);
			packet.writeD(0x00);
		}
		
		return true;
	}
}
