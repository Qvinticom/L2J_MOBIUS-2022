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
import org.l2jmobius.gameserver.model.ItemRequest;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.model.TradeList.TradeItem;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

public class RequestPrivateStoreBuy implements IClientIncomingPacket
{
	private int _storePlayerId;
	private int _count;
	private ItemRequest[] _items;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_storePlayerId = packet.readD();
		_count = packet.readD();
		
		// count*12 is the size of a for iteration of each item
		if ((_count < 0) || ((_count * 12) > packet.getReadableBytes()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
		}
		
		_items = new ItemRequest[_count];
		for (int i = 0; i < _count; i++)
		{
			final int objectId = packet.readD();
			long count = packet.readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			final int price = packet.readD();
			_items[i] = new ItemRequest(objectId, (int) count, price);
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
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendMessage("You buying items too fast.");
			return;
		}
		
		final WorldObject object = World.getInstance().findObject(_storePlayerId);
		if (!(object instanceof Player))
		{
			return;
		}
		
		final Player storePlayer = (Player) object;
		if (((storePlayer.getPrivateStoreType() != Player.STORE_PRIVATE_SELL) && (storePlayer.getPrivateStoreType() != Player.STORE_PRIVATE_PACKAGE_SELL)))
		{
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Unsufficient privileges.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList storeList = storePlayer.getSellList();
		if (storeList == null)
		{
			return;
		}
		
		// Check if player didn't choose any items
		if ((_items == null) || (_items.length == 0))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// FIXME: this check should be (and most probabliy is) done in the TradeList mechanics
		long priceTotal = 0;
		for (ItemRequest ir : _items)
		{
			if ((ir.getCount() > Integer.MAX_VALUE) || (ir.getCount() < 0))
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried an overflow exploit, ban this player!";
				Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			final TradeItem sellersItem = storeList.getItem(ir.getObjectId());
			if (sellersItem == null)
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried to buy an item not sold in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			if (ir.getPrice() != sellersItem.getPrice())
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried to change the seller's price in a private store (buy), ban this player!";
				Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
				return;
			}
			
			final Item iEnchant = storePlayer.getInventory().getItemByObjectId(ir.getObjectId());
			int enchant = 0;
			if (iEnchant == null)
			{
				enchant = 0;
			}
			else
			{
				enchant = iEnchant.getEnchantLevel();
			}
			ir.setEnchant(enchant);
			
			priceTotal += ir.getPrice() * ir.getCount();
		}
		
		// FIXME: this check should be (and most probably is) done in the TradeList mechanics
		if ((priceTotal < 0) || (priceTotal > Integer.MAX_VALUE))
		{
			final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (Config.SELL_BY_ITEM)
		{
			if (player.getItemCount(Config.SELL_ITEM, -1) < priceTotal)
			{
				player.sendPacket(SystemMessage.sendString("You do not have needed items to buy"));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else if (player.getAdena() < priceTotal)
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((storePlayer.getPrivateStoreType() == Player.STORE_PRIVATE_PACKAGE_SELL) && (storeList.getItemCount() > _count))
		{
			final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
			Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (!storeList.PrivateStoreBuy(player, _items, (int) priceTotal))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			// Punishment e LOGGER in audit
			Util.handleIllegalPlayerAction(storePlayer, "PrivateStore buy has failed due to invalid list or request. Player: " + player.getName(), Config.DEFAULT_PUNISH);
			LOGGER.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}
}