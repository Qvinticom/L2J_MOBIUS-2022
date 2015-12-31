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
package com.l2jmobius.gameserver.network.clientpackets.alchemy;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.alchemy.ExTryMixCube;

public class RequestAlchemyTryMixCube extends L2GameClientPacket
{
	private final static int AIR_STONE = 39461;
	private final static int ELCYUM_CRYSTAL = 36514;
	private final Map<Integer, Long> _items = new HashMap<>();
	
	public RequestAlchemyTryMixCube()
	{
		_items.clear();
	}
	
	@Override
	protected void readImpl()
	{
		final int count = readD();
		for (int i = 0; i < count; ++i)
		{
			final int itemObjectId = readD();
			final long itemCount = readQ();
			_items.put(itemObjectId, itemCount);
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		if ((_items == null) || _items.isEmpty())
		{
			activeChar.sendPacket(ExTryMixCube.FAIL);
			return;
		}
		if (activeChar.isInCombat())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_DURING_BATTLE);
			activeChar.sendPacket(ExTryMixCube.FAIL);
			return;
		}
		if (activeChar.isInStoreMode() || activeChar.isInStoreMode())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_TRADING_OR_USING_A_PRIVATE_STORE_OR_SHOP);
			activeChar.sendPacket(ExTryMixCube.FAIL);
			return;
		}
		if (activeChar.isDead())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_DEAD);
			activeChar.sendPacket(ExTryMixCube.FAIL);
			return;
		}
		if (activeChar.isMovementDisabled())
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_IMMOBILE);
			activeChar.sendPacket(ExTryMixCube.FAIL);
			return;
		}
		
		long totalPrice = 0;
		long count = 0;
		for (int itemId : _items.keySet())
		{
			final int itemObjectId = itemId;
			final long itemCount = _items.get(itemId);
			final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(itemObjectId);
			if (item != null)
			{
				if (item.getCount() < itemCount)
				{
					continue;
				}
				if (!item.isDestroyable())
				{
					continue;
				}
				if (item.getEnchantLevel() > 0)
				{
					continue;
				}
				if (item.isAugmented())
				{
					continue;
				}
				if (item.isShadowItem())
				{
					continue;
				}
				if (item.getId() == ELCYUM_CRYSTAL)
				{
					if (_items.size() <= 3)
					{
						continue;
					}
					count = itemCount;
				}
				else
				{
					final long price = item.getId() == Inventory.ADENA_ID ? itemCount : item.getReferencePrice();
					if (price <= 0)
					{
						continue;
					}
					totalPrice += price;
				}
				activeChar.destroyItem("AlchemyMixCube", itemObjectId, itemCount, activeChar, true);
			}
		}
		
		long stoneCount = 0;
		if (totalPrice > 0)
		{
			if (_items.size() >= 3)
			{
				stoneCount = totalPrice / 10000;
				stoneCount += count * 1000;
			}
			else if ((totalPrice >= 20000) && (totalPrice < 35000))
			{
				stoneCount = 1;
			}
			else if ((totalPrice >= 35000) && (totalPrice < 50000))
			{
				stoneCount = 2;
			}
			else if (totalPrice >= 50000)
			{
				stoneCount = (long) Math.floor(totalPrice / 16666.666666666668);
			}
		}
		if (stoneCount > 0)
		{
			activeChar.addItem("AlchemyMixCube", AIR_STONE, stoneCount, activeChar, true);
		}
		activeChar.sendPacket(new ExTryMixCube(AIR_STONE, stoneCount));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
