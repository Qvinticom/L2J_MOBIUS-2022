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
package com.l2jmobius.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.model.L2ExtractableProduct;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.items.type.EtcItemType;

/**
 * This class is dedicated to the management of EtcItem.
 */
public final class L2EtcItem extends L2Item
{
	private String _handler;
	private EtcItemType _type;
	private boolean _isBlessed;
	private List<L2ExtractableProduct> _extractableItems;
	private int _extractableCountMin;
	private int _extractableCountMax;
	private boolean _isInfinite;
	
	/**
	 * Constructor for EtcItem.
	 * @param set StatsSet designating the set of couples (key,value) for description of the Etc
	 */
	public L2EtcItem(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public void set(StatsSet set)
	{
		super.set(set);
		_type = set.getEnum("etcitem_type", EtcItemType.class, EtcItemType.NONE);
		
		// l2j custom - L2EtcItemType.SHOT
		switch (getDefaultAction())
		{
			case SOULSHOT:
			case SUMMON_SOULSHOT:
			case SUMMON_SPIRITSHOT:
			case SPIRITSHOT:
			{
				_type = EtcItemType.SHOT;
				break;
			}
		}
		
		_type1 = L2Item.TYPE1_ITEM_QUESTITEM_ADENA;
		_type2 = L2Item.TYPE2_OTHER; // default is other
		
		if (isQuestItem())
		{
			_type2 = L2Item.TYPE2_QUEST;
		}
		else if ((getId() == Inventory.ADENA_ID) || (getId() == Inventory.ANCIENT_ADENA_ID))
		{
			_type2 = L2Item.TYPE2_MONEY;
		}
		
		_handler = set.getString("handler", null); // ! null !
		_isBlessed = set.getBoolean("blessed", false);
		
		// Extractable
		final String capsuled_items = set.getString("capsuled_items", null);
		if (capsuled_items != null)
		{
			final String[] split = capsuled_items.split(";");
			_extractableItems = new ArrayList<>(split.length);
			for (String part : split)
			{
				if (part.trim().isEmpty())
				{
					continue;
				}
				final String[] data = part.split(",");
				if ((data.length != 4) && (data.length != 6))
				{
					_log.info("> Couldnt parse " + part + " in capsuled_items! item " + this);
					continue;
				}
				final int itemId = Integer.parseInt(data[0]);
				final int min = Integer.parseInt(data[1]);
				final int max = Integer.parseInt(data[2]);
				if (max < min)
				{
					_log.info("> Max amount < Min amount in " + part + ", item " + this);
					continue;
				}
				final double chance = Double.parseDouble(data[3]);
				int minEnchant = 0;
				int maxEnchant = 0;
				if (data.length == 6)
				{
					minEnchant = Integer.parseInt(data[4]);
					maxEnchant = Integer.parseInt(data[5]);
					if (maxEnchant < minEnchant)
					{
						_log.info("> Max enchant < Min enchant in " + part + ", item " + this);
						continue;
					}
				}
				_extractableItems.add(new L2ExtractableProduct(itemId, min, max, chance, minEnchant, maxEnchant));
			}
			((ArrayList<?>) _extractableItems).trimToSize();
			
			// check for handler
			if (_handler == null)
			{
				_log.warning("Item " + this + " define capsuled_items but missing handler.");
				_handler = "ExtractableItems";
			}
		}
		else
		{
			_extractableItems = null;
		}
		
		_extractableCountMin = set.getInt("extractableCountMin", 0);
		_extractableCountMax = set.getInt("extractableCountMax", 0);
		if (_extractableCountMin > _extractableCountMax)
		{
			_log.warning("Item " + this + " extractableCountMin is bigger than extractableCountMax!");
		}
		
		_isInfinite = set.getBoolean("is_infinite", false);
	}
	
	/**
	 * @return the type of Etc Item.
	 */
	@Override
	public EtcItemType getItemType()
	{
		return _type;
	}
	
	/**
	 * @return the ID of the Etc item after applying the mask.
	 */
	@Override
	public int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * @return the handler name, null if no handler for item.
	 */
	public String getHandlerName()
	{
		return _handler;
	}
	
	/**
	 * @return {@code true} if the item is blessed, {@code false} otherwise.
	 */
	public final boolean isBlessed()
	{
		return _isBlessed;
	}
	
	/**
	 * @return the extractable items list.
	 */
	public List<L2ExtractableProduct> getExtractableItems()
	{
		return _extractableItems;
	}
	
	/**
	 * @return the minimum count of extractable items
	 */
	public int getExtractableCountMin()
	{
		return _extractableCountMin;
	}
	
	/**
	 * @return the maximum count of extractable items
	 */
	public int getExtractableCountMax()
	{
		return _extractableCountMax;
	}
	
	/**
	 * @return true if item is infinite
	 */
	public boolean isInfinite()
	{
		return _isInfinite;
	}
}
