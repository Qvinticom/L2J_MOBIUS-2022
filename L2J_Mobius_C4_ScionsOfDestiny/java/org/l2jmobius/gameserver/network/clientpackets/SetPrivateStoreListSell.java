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
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreManageListSell;
import org.l2jmobius.gameserver.network.serverpackets.PrivateStoreMsgSell;
import org.l2jmobius.gameserver.util.Util;

public class SetPrivateStoreListSell implements IClientIncomingPacket
{
	private int _count;
	private boolean _packageSale;
	private int[] _items; // count * 3
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_packageSale = packet.readD() == 1;
		_count = packet.readD();
		if ((_count <= 0) || ((_count * 12) > packet.getReadableBytes()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
			return false;
		}
		
		_items = new int[_count * 3];
		for (int x = 0; x < _count; x++)
		{
			final int objectId = packet.readD();
			_items[(x * 3) + 0] = objectId;
			final long cnt = packet.readD();
			if ((cnt > Integer.MAX_VALUE) || (cnt < 0))
			{
				_count = 0;
				return false;
			}
			
			_items[(x * 3) + 1] = (int) cnt;
			final int price = packet.readD();
			_items[(x * 3) + 2] = price;
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
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isCastingNow() || player.isCastingPotionNow() || player.isMovementDisabled() || player.inObserverMode() || (player.getActiveEnchantItem() != null))
		{
			player.sendMessage("You cannot start store now..");
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendMessage("Trade is disable here. Try another place.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList tradeList = player.getSellList();
		tradeList.clear();
		tradeList.setPackaged(_packageSale);
		
		long totalCost = player.getAdena();
		for (int i = 0; i < _count; i++)
		{
			final int objectId = _items[(i * 3) + 0];
			final int count = _items[(i * 3) + 1];
			final int price = _items[(i * 3) + 2];
			if (price <= 0)
			{
				final String msgErr = "[SetPrivateStoreListSell] player " + player.getName() + " tried an overflow exploit (use PHX), ban this player!";
				Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
				_count = 0;
				return;
			}
			
			totalCost += price;
			if (totalCost > Integer.MAX_VALUE)
			{
				player.sendPacket(new PrivateStoreManageListSell(player));
				player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
				return;
			}
			
			tradeList.addItem(objectId, count, price);
		}
		
		if (_count <= 0)
		{
			player.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (player.isProcessingTransaction())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendMessage("Store mode are disable while trading.");
			return;
		}
		
		// Check maximum number of allowed slots for pvt shops
		if (_count > player.getPrivateSellStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListSell(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		player.sitDown();
		if (_packageSale)
		{
			player.setPrivateStoreType(Player.STORE_PRIVATE_PACKAGE_SELL);
		}
		else
		{
			player.setPrivateStoreType(Player.STORE_PRIVATE_SELL);
		}
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgSell(player));
	}
}