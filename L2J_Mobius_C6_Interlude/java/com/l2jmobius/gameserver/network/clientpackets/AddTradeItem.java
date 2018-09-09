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

import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.TradeList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.TradeOtherAdd;
import com.l2jmobius.gameserver.network.serverpackets.TradeOwnAdd;
import com.l2jmobius.gameserver.network.serverpackets.TradeUpdate;

public final class AddTradeItem extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(AddTradeItem.class.getName());
	private int _tradeId;
	private int _objectId;
	private int _count;
	
	public AddTradeItem()
	{
	}
	
	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_count = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final TradeList trade = player.getActiveTradeList();
		if (trade == null) // Trade null
		{
			LOGGER.warning("Character: " + player.getName() + " requested item:" + _objectId + " add without active tradelist:" + _tradeId);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Check Partner and ocbjectId
		if ((trade.getPartner() == null) || (L2World.getInstance().findObject(trade.getPartner().getObjectId()) == null))
		{
			// Trade partner not found, cancel trade
			if (trade.getPartner() != null)
			{
				LOGGER.warning("Character:" + player.getName() + " requested invalid trade object: " + _objectId);
			}
			
			player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}
		
		// Check if player has Access level for Transaction
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendMessage("Transactions are disable for your Access Level.");
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			player.cancelActiveTrade();
			return;
		}
		
		// Check validateItemManipulation
		if (!player.validateItemManipulation(_objectId, "trade"))
		{
			player.sendPacket(SystemMessageId.NOTHING_HAPPENED);
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Java Emulator Security
		if ((player.getInventory().getItemByObjectId(_objectId) == null) || (_count <= 0))
		{
			LOGGER.info("Character:" + player.getName() + " requested invalid trade object");
			player.getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList.TradeItem item = trade.addItem(_objectId, _count);
		if (item == null)
		{
			return;
		}
		
		if (item.isAugmented())
		{
			return;
		}
		
		player.sendPacket(new TradeOwnAdd(item));
		player.sendPacket(new TradeUpdate(trade, player));
		trade.getPartner().sendPacket(new TradeOtherAdd(item));
	}
}