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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreMsgBuy;

public class SetPrivateStoreListBuy implements IClientIncomingPacket
{
	private int _count;
	private int[] _items; // count * 3
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_count = packet.readD();
		if ((_count <= 0) || ((_count * 12) > packet.getReadableBytes()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
			return false;
		}
		
		_items = new int[_count * 4];
		for (int x = 0; x < _count; x++)
		{
			final int itemId = packet.readD();
			_items[(x * 4) + 0] = itemId;
			_items[((x * 4) + 3)] = packet.readH();
			// packet.readH(); // it's the enchant value, but the interlude client has a bug, so it did not send back the correct enchant value
			packet.readH(); // TODO analyse this
			final long cnt = packet.readD();
			if ((cnt > Integer.MAX_VALUE) || (cnt < 0))
			{
				_count = 0;
				return false;
			}
			
			_items[(x * 4) + 1] = (int) cnt;
			final int price = packet.readD();
			_items[(x * 4) + 2] = price;
		}
		
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isTradeDisabled())
		{
			player.sendMessage("Trade are disable here. Try in another place.");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow() || player.isMovementDisabled() || player.inObserverMode() || (player.getActiveEnchantItem() != null))
		{
			player.sendMessage("You cannot start store now..");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendMessage("Trade is disable here. Try another place.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		int cost = 0;
		for (int i = 0; i < _count; i++)
		{
			final int itemId = _items[(i * 4) + 0];
			final int count = _items[(i * 4) + 1];
			final int price = _items[(i * 4) + 2];
			final int enchant = _items[(i * 4) + 3];
			tradeList.addItemByItemId(itemId, count, price, enchant);
			cost += count * price;
			if (cost > Integer.MAX_VALUE)
			{
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				player.sendPacket(new PrivateStoreManageListBuy(player));
				return;
			}
		}
		
		if (_count <= 0)
		{
			player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendMessage("Store mode are disable while trading.");
			player.sendPacket(new PrivateStoreManageListBuy(player));
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_count > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		// Check for available funds
		if (Config.SELL_BY_ITEM)
		{
			if ((cost > player.getItemCount(Config.SELL_ITEM, -1)) || (cost <= 0))
			{
				player.sendPacket(new PrivateStoreManageListBuy(player));
				player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
				return;
			}
		}
		else if ((cost > player.getAdena()) || (cost <= 0))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			return;
		}
		
		player.sitDown();
		player.setPrivateStoreType(Player.STORE_PRIVATE_BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
}