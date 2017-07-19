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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.ItemRequest;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.TradeList;
import com.l2jmobius.gameserver.model.TradeList.TradeItem;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private static final String _C__79_REQUESTPRIVATESTOREBUY = "[C] 79 RequestPrivateStoreBuy";
	private static Logger _log = Logger.getLogger(RequestPrivateStoreBuy.class.getName());
	
	private int _storePlayerId;
	private int _count;
	private ItemRequest[] _items;
	
	@Override
	protected void readImpl()
	{
		_storePlayerId = readD();
		_count = readD();
		if ((_count < 0) || ((_count * 12) > _buf.remaining()) || (_count > Config.MAX_ITEM_IN_PACKET))
		{
			_count = 0;
		}
		_items = new ItemRequest[_count];
		
		for (int i = 0; i < _count; i++)
		{
			final int objectId = readD();
			long count = readD();
			if (count > Integer.MAX_VALUE)
			{
				count = Integer.MAX_VALUE;
			}
			final int price = readD();
			
			_items[i] = new ItemRequest(objectId, (int) count, price);
		}
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("privatestorebuy"))
		{
			player.sendMessage("You are buying items too fast.");
			return;
		}
		
		final L2Object object = L2World.getInstance().findObject(_storePlayerId);
		if ((object == null) || !(object instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance storePlayer = (L2PcInstance) object;
		if (!((storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_SELL) || (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)))
		{
			return;
		}
		
		final TradeList storeList = storePlayer.getSellList();
		
		if (storeList == null)
		{
			return;
		}
		
		if (Config.GM_DISABLE_TRANSACTION && (player.getAccessLevel() >= Config.GM_TRANSACTION_MIN) && (player.getAccessLevel() <= Config.GM_TRANSACTION_MAX))
		{
			player.sendMessage("Transactions are disabled for your Access Level.");
			sendPacket(new ActionFailed());
			return;
		}
		
		// FIXME: this check should be (and most probably is) done in the TradeList mechanics
		long priceTotal = 0;
		for (final ItemRequest ir : _items)
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
			priceTotal += ir.getPrice() * ir.getCount();
		}
		
		// FIXME: this check should be (and most probably is) done in the TradeList mechanics
		if ((priceTotal < 0) || (priceTotal > Integer.MAX_VALUE))
		{
			final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried an overflow exploit, ban this player!";
			Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
			return;
		}
		
		if (player.getAdena() < priceTotal)
		{
			sendPacket(new SystemMessage(SystemMessage.YOU_NOT_ENOUGH_ADENA));
			sendPacket(new ActionFailed());
			return;
		}
		
		if (storePlayer.getPrivateStoreType() == L2PcInstance.STORE_PRIVATE_PACKAGE_SELL)
		{
			if (storeList.getItemCount() > _count)
			{
				final String msgErr = "[RequestPrivateStoreBuy] player " + player.getName() + " tried to buy less items then sold by package-sell, ban this player for bot-usage!";
				Util.handleIllegalPlayerAction(player, msgErr, Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		if (!storeList.PrivateStoreBuy(player, _items, (int) priceTotal))
		{
			sendPacket(new ActionFailed());
			_log.warning("PrivateStore buy has failed due to invalid list or request. Player: " + player.getName() + ", Private store of: " + storePlayer.getName());
			return;
		}
		
		if (storeList.getItemCount() == 0)
		{
			storePlayer.setPrivateStoreType(L2PcInstance.STORE_PRIVATE_NONE);
			storePlayer.broadcastUserInfo();
		}
	}
	
	@Override
	public String getType()
	{
		return _C__79_REQUESTPRIVATESTOREBUY;
	}
}