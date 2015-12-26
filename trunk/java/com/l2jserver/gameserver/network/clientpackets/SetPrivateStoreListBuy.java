/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets;

import static com.l2jserver.gameserver.model.itemcontainer.Inventory.MAX_ADENA;

import com.l2jserver.Config;
import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.model.TradeList;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.2.2.1.2.5 $ $Date: 2005/03/27 15:29:30 $ CPU Disasm Packets: ddhhQQ cddb
 */
public final class SetPrivateStoreListBuy extends L2GameClientPacket
{
	private static final String _C__9A_SETPRIVATESTORELISTBUY = "[C] 9A SetPrivateStoreListBuy";
	
	private static final int BATCH_LENGTH = 46; // length of the one item
	
	private Item[] _items = null;
	
	@Override
	protected void readImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		final int count = readD();
		if ((count < 1) || (count > Config.MAX_ITEM_IN_PACKET) || ((count * BATCH_LENGTH) != _buf.remaining()))
		{
			return;
		}
		
		_items = new Item[count];
		for (int i = 0; i < count; i++)
		{
			final int itemId = readD();
			int enchantLevel = readD();
			
			final long cnt = readQ();
			final long price = readQ();
			
			if ((itemId < 1) || (cnt < 1) || (price < 0))
			{
				_items = null;
				return;
			}
			int attackAttribute = readH(); // Attack Attribute Type
			int attackAttributeValue = readH(); // Attack Attribute Value
			final int defenseAttributes[] = new int[6];
			for (int h = 0; h < 6; h++)
			{
				defenseAttributes[i] = readH(); // Defense attributes
			}
			int appearanceId = readD(); // Appearance ID
			readH(); // ?
			boolean canUse = false;
			for (L2ItemInstance item : player.getInventory().getItemsByItemId(itemId))
			{
				if ((enchantLevel == item.getEnchantLevel()) && (attackAttribute == item.getAttackElementType()) && (attackAttributeValue == item.getAttackElementPower()) && (appearanceId == item.getVisualId()) && (item.getElementDefAttr((byte) 0) == defenseAttributes[0]) && (item.getElementDefAttr((byte) 1) == defenseAttributes[1]) && (item.getElementDefAttr((byte) 2) == defenseAttributes[2]) && (item.getElementDefAttr((byte) 3) == defenseAttributes[3]) && (item.getElementDefAttr((byte) 4) == defenseAttributes[4]) && (item.getElementDefAttr((byte) 5) == defenseAttributes[5]))
				{
					canUse = true;
					break;
				}
			}
			if (!canUse)
			{
				enchantLevel = 0;
				attackAttribute = -1;
				attackAttributeValue = 0;
				defenseAttributes[0] = 0;
				defenseAttributes[1] = 0;
				defenseAttributes[2] = 0;
				defenseAttributes[3] = 0;
				defenseAttributes[4] = 0;
				defenseAttributes[5] = 0;
				appearanceId = 0;
			}
			_items[i] = new Item(itemId, cnt, price, enchantLevel, attackAttribute, attackAttributeValue, defenseAttributes, appearanceId);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (_items == null)
		{
			player.setPrivateStoreType(PrivateStoreType.NONE);
			player.broadcastUserInfo();
			return;
		}
		
		if (!player.getAccessLevel().allowTransaction())
		{
			player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel())
		{
			player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ENGAGED_IN_COMBAT_YOU_CANNOT_OPERATE_A_PRIVATE_STORE_OR_PRIVATE_WORKSHOP);
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (player.isInsideZone(ZoneId.NO_STORE))
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_CANNOT_OPEN_A_PRIVATE_STORE_HERE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!player.canOpenPrivateStore())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final TradeList tradeList = player.getBuyList();
		tradeList.clear();
		
		// Check maximum number of allowed slots for pvt shops
		if (_items.length > player.getPrivateBuyStoreLimit())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_QUANTITY_THAT_CAN_BE_INPUTTED);
			return;
		}
		
		long totalCost = 0;
		for (Item i : _items)
		{
			if (!i.addToTradeList(tradeList))
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set price more than " + MAX_ADENA + " adena in Private Store - Buy.", Config.DEFAULT_PUNISH);
				return;
			}
			
			totalCost += i.getCost();
			if (totalCost > MAX_ADENA)
			{
				Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " tried to set total price more than " + MAX_ADENA + " adena in Private Store - Buy.", Config.DEFAULT_PUNISH);
				return;
			}
		}
		
		// Check for available funds
		if (totalCost > player.getAdena())
		{
			player.sendPacket(new PrivateStoreManageListBuy(player));
			player.sendPacket(SystemMessageId.THE_PURCHASE_PRICE_IS_HIGHER_THAN_THE_AMOUNT_OF_MONEY_THAT_YOU_HAVE_AND_SO_YOU_CANNOT_OPEN_A_PERSONAL_STORE);
			return;
		}
		
		player.sitDown();
		player.setPrivateStoreType(PrivateStoreType.BUY);
		player.broadcastUserInfo();
		player.broadcastPacket(new PrivateStoreMsgBuy(player));
	}
	
	private static class Item
	{
		private final int _itemId;
		private final long _count;
		private final long _price;
		private final int _enchantLevel;
		private final int _attackAttribute;
		private final int _attackAttributeValue;
		private final int _defenseAttributes[];
		private final int _appearanceId;
		
		public Item(int itemId, long cnt, long price, int enchantLevel, int attackAttribute, int attackAttributeValue, int defenseAttributes[], int appearanceId)
		{
			_itemId = itemId;
			_count = cnt;
			_price = price;
			_enchantLevel = enchantLevel;
			_attackAttribute = attackAttribute;
			_attackAttributeValue = attackAttributeValue;
			_defenseAttributes = defenseAttributes;
			_appearanceId = appearanceId;
		}
		
		public boolean addToTradeList(TradeList list)
		{
			if ((MAX_ADENA / _count) < _price)
			{
				return false;
			}
			
			list.addItemByItemId(_itemId, _count, _price, _enchantLevel, _attackAttribute, _attackAttributeValue, _defenseAttributes, _appearanceId);
			return true;
		}
		
		public long getCost()
		{
			return _count * _price;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__9A_SETPRIVATESTORELISTBUY;
	}
}
