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
package com.l2jmobius.gameserver.model;

import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;

public class TradeItem
{
	private L2ItemInstance _itemInstance;
	private int _objectId;
	private final L2Item _item;
	private final int _location;
	private int _enchant;
	private final int _type1;
	private final int _type2;
	private long _count;
	private long _storeCount;
	private long _price;
	private final byte _elemAtkType;
	private final int _elemAtkPower;
	private final int[] _elemDefAttr =
	{
		0,
		0,
		0,
		0,
		0,
		0
	};
	private final int[] _enchantOptions;
	private final boolean _isAugmented;
	private final L2Augmentation _augmentation;
	private final int _mana;
	private final boolean _isTimeLimited;
	private final int _time;
	private final int _visualId;
	private final long _visualExpiration;
	
	public TradeItem(L2ItemInstance item, long count, long price)
	{
		_itemInstance = item;
		_objectId = item.getObjectId();
		_item = item.getItem();
		_location = item.getLocationSlot();
		_enchant = item.getEnchantLevel();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		_count = count;
		_price = price;
		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchantOptions = item.getEnchantOptions();
		_isAugmented = item.isAugmented();
		_augmentation = item.getAugmentation();
		_mana = item.getMana();
		_isTimeLimited = item.isTimeLimitedItem();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999;
		_visualId = item.getVisualId();
		_visualExpiration = item.getTime();
	}
	
	public TradeItem(L2Item item, long count, long price, int enchantLevel, int attackAttribute, int attackAttributeValue, int defenseAttributes[], int appearanceId)
	{
		_itemInstance = null;
		_objectId = 0;
		_item = item;
		_location = 0;
		_enchant = 0;
		_type1 = 0;
		_type2 = 0;
		_count = count;
		_storeCount = count;
		_price = price;
		_elemAtkType = (byte) attackAttribute;
		_elemAtkPower = attackAttributeValue;
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = defenseAttributes[i];
		}
		_enchantOptions = L2ItemInstance.DEFAULT_ENCHANT_OPTIONS;
		_isAugmented = false;
		_augmentation = null;
		_mana = -1;
		_isTimeLimited = false;
		_time = -9999;
		_visualId = appearanceId;
		_visualExpiration = -1;
	}
	
	public TradeItem(TradeItem item, long count, long price, int enchantLevel, int attackAttribute, int attackAttributeValue, int defenseAttributes[], int appearanceId)
	{
		_itemInstance = item.getItemInstance();
		_objectId = item.getObjectId();
		_item = item.getItem();
		_location = item.getLocationSlot();
		_enchant = item.getEnchant();
		_type1 = item.getCustomType1();
		_type2 = item.getCustomType2();
		_count = count;
		_storeCount = count;
		_price = price;
		_elemAtkType = item.getAttackElementType();
		_elemAtkPower = item.getAttackElementPower();
		for (byte i = 0; i < 6; i++)
		{
			_elemDefAttr[i] = item.getElementDefAttr(i);
		}
		_enchantOptions = item.getEnchantOptions();
		_isAugmented = item.isAugmented();
		_augmentation = item.getAugmentation();
		_mana = item.getMana();
		_isTimeLimited = item.isTimeLimitedItem();
		_time = item.isTimeLimitedItem() ? (int) (item.getRemainingTime() / 1000) : -9999;
		_visualId = item.getVisualId();
		_visualExpiration = item.getVisualExpiration();
	}
	
	public L2ItemInstance getItemInstance()
	{
		return _itemInstance;
	}
	
	public void setItemInstance(L2ItemInstance it)
	{
		_itemInstance = it;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public L2Item getItem()
	{
		return _item;
	}
	
	public int getLocationSlot()
	{
		return _location;
	}
	
	public void setEnchant(int enchant)
	{
		_enchant = enchant;
	}
	
	public int getEnchant()
	{
		return _enchant;
	}
	
	public int getCustomType1()
	{
		return _type1;
	}
	
	public int getCustomType2()
	{
		return _type2;
	}
	
	public void setCount(long count)
	{
		_count = count;
	}
	
	public long getCount()
	{
		return _count;
	}
	
	public long getStoreCount()
	{
		return _storeCount;
	}
	
	public void setPrice(long price)
	{
		_price = price;
	}
	
	public long getPrice()
	{
		return _price;
	}
	
	public byte getAttackElementType()
	{
		return _elemAtkType;
	}
	
	public int getAttackElementPower()
	{
		return _elemAtkPower;
	}
	
	public int getElementDefAttr(byte i)
	{
		return _elemDefAttr[i];
	}
	
	public int[] getEnchantOptions()
	{
		return _enchantOptions;
	}
	
	public boolean isAugmented()
	{
		return _isAugmented;
	}
	
	public L2Augmentation getAugmentation()
	{
		return _augmentation;
	}
	
	public int getMana()
	{
		return _mana;
	}
	
	public boolean isTimeLimitedItem()
	{
		return _isTimeLimited;
	}
	
	public int getVisualId()
	{
		return _visualId;
	}
	
	public long getVisualExpiration()
	{
		return _visualExpiration;
	}
	
	public int getRemainingTime()
	{
		return _time;
	}
}
