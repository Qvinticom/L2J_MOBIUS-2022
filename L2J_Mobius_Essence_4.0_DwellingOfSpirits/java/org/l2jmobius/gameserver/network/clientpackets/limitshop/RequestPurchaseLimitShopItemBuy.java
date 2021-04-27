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
package org.l2jmobius.gameserver.network.clientpackets.limitshop;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.xml.LCoinShopData;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.request.PrimeShopRequest;
import org.l2jmobius.gameserver.model.holders.LCoinShopProductHolder;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.variables.AccountVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.primeshop.ExBRBuyProduct;
import org.l2jmobius.gameserver.network.serverpackets.primeshop.ExBRBuyProduct.ExBrProductReplyType;

/**
 * @author Mobius
 */
public class RequestPurchaseLimitShopItemBuy implements IClientIncomingPacket
{
	private int _productId;
	private int _amount;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		packet.readC(); // category?
		_productId = packet.readD();
		_amount = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_amount < 1)
		{
			return;
		}
		
		final LCoinShopProductHolder product = LCoinShopData.getInstance().getProduct(_productId);
		if (product == null)
		{
			return;
		}
		
		if ((player.getLevel() < product.getMinLevel()) || (player.getLevel() > product.getMaxLevel()))
		{
			player.sendPacket(SystemMessageId.YOUR_LEVEL_CANNOT_PURCHASE_THIS_ITEM);
			return;
		}
		
		if (player.hasItemRequest() || player.hasRequest(PrimeShopRequest.class))
		{
			player.sendPacket(new ExBRBuyProduct(ExBrProductReplyType.INVALID_USER_STATE));
			return;
		}
		
		// Add request.
		player.addRequest(new PrimeShopRequest(player));
		
		// Check limits.
		if (product.getAccountDailyLimit() > 0) // Sale period.
		{
			if (player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0) >= product.getAccountDailyLimit())
			{
				if ((player.getAccountVariables().getLong(AccountVariables.LCOIN_SHOP_PRODUCT_TIME + product.getProductionId(), 0) + 86400000) > Chronos.currentTimeMillis())
				{
					player.sendMessage("You have reached your daily limit."); // TODO: Retail system message?
					player.removeRequest(PrimeShopRequest.class);
					return;
				}
				// Reset limit.
				player.getAccountVariables().set(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0);
			}
		}
		else if (product.getAccountBuyLimit() > 0) // Count limit.
		{
			if (player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0) >= product.getAccountBuyLimit())
			{
				player.sendMessage("You cannot buy any more of this item."); // TODO: Retail system message?
				player.removeRequest(PrimeShopRequest.class);
				return;
			}
		}
		
		// Check existing items.
		for (int i = 0; i < 3; i++)
		{
			if (product.getIngredientIds()[i] == 0)
			{
				continue;
			}
			if (product.getIngredientIds()[i] == Inventory.ADENA_ID)
			{
				if (player.getAdena() < (product.getIngredientQuantities()[i] * _amount))
				{
					player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
					player.removeRequest(PrimeShopRequest.class);
					return;
				}
			}
			else if (player.getInventory().getInventoryItemCount(product.getIngredientIds()[i], -1, true) < (product.getIngredientQuantities()[i] * _amount))
			{
				player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT_2);
				player.removeRequest(PrimeShopRequest.class);
				return;
			}
		}
		
		// Remove items.
		for (int i = 0; i < 3; i++)
		{
			if (product.getIngredientIds()[i] == 0)
			{
				continue;
			}
			if (product.getIngredientIds()[i] == Inventory.ADENA_ID)
			{
				player.reduceAdena("LCoinShop", product.getIngredientQuantities()[i] * _amount, player, true);
			}
			else
			{
				player.destroyItemByItemId("LCoinShop", product.getIngredientIds()[i], product.getIngredientQuantities()[i] * _amount, player, true);
			}
		}
		
		// Reward.
		if (product.getProductionId2() > 0)
		{
			if (Rnd.get(100) < product.getChance())
			{
				player.addItem("LCoinShop", product.getProductionId(), product.getCount(), player, true);
			}
			else if (Rnd.get(100) < product.getChance2())
			{
				player.addItem("LCoinShop", product.getProductionId2(), product.getCount2(), player, true);
			}
			else if (product.getProductionId3() > 0)
			{
				player.addItem("LCoinShop", product.getProductionId3(), product.getCount3(), player, true);
			}
		}
		else
		{
			player.addItem("LCoinShop", product.getProductionId(), _amount, player, true);
		}
		
		// Update account variables.
		if (product.getAccountDailyLimit() > 0)
		{
			player.getAccountVariables().set(AccountVariables.LCOIN_SHOP_PRODUCT_TIME + product.getProductionId(), Chronos.currentTimeMillis());
			player.getAccountVariables().set(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0) + 1);
		}
		else if (product.getAccountBuyLimit() > 0)
		{
			player.getAccountVariables().set(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), player.getAccountVariables().getInt(AccountVariables.LCOIN_SHOP_PRODUCT_COUNT + product.getProductionId(), 0) + 1);
		}
		
		// Remove request.
		player.removeRequest(PrimeShopRequest.class);
	}
}
