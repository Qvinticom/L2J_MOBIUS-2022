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
package com.l2jserver.gameserver.model.drops;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.util.Util;
import com.l2jserver.util.Rnd;

/**
 * @author NosBit
 */
public class GroupedGeneralDropItem implements IDropItem
{
	private final double _chance;
	private List<GeneralDropItem> _items;
	
	/**
	 * @param chance the chance of this drop item.
	 */
	public GroupedGeneralDropItem(double chance)
	{
		_chance = chance;
	}
	
	protected double getGlobalChanceMultiplier()
	{
		return 1.;
	}
	
	/**
	 * Gets the chance of this drop item.
	 * @return the chance
	 */
	public double getChance()
	{
		return _chance;
	}
	
	/**
	 * Gets the chance of this drop item.
	 * @param victim the victim
	 * @param killer the killer
	 * @return the chance modified by any rates.
	 */
	public double getChance(L2Character victim, L2Character killer)
	{
		for (final GeneralDropItem gdi : getItems())
		{
			final L2Item item = ItemTable.getInstance().getTemplate(gdi.getItemId());
			if ((item == null) || !item.hasExImmediateEffect())
			{
				return getChance() * getGlobalChanceMultiplier();
			}
		}
		
		return getChance() * Config.RATE_HERB_DROP_CHANCE_MULTIPLIER;
	}
	
	/**
	 * Gets the items.
	 * @return the items
	 */
	public List<GeneralDropItem> getItems()
	{
		return _items;
	}
	
	/**
	 * Sets an item list to this drop item.
	 * @param items the item list
	 */
	public void setItems(List<GeneralDropItem> items)
	{
		_items = Collections.unmodifiableList(items);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jserver.gameserver.model.drop.IDropItem#calculateDrops(com.l2jserver.gameserver.model.actor.L2Character, com.l2jserver.gameserver.model.actor.L2Character)
	 */
	@Override
	public List<ItemHolder> calculateDrops(L2Character victim, L2Character killer)
	{
		int levelDifference = victim.getLevel() - killer.getLevel();
		double chanceModifier;
		if (victim instanceof L2RaidBossInstance)
		{
			chanceModifier = Math.max(0, Math.min(1, (levelDifference * 0.15) + 1));
		}
		else
		{
			chanceModifier = 1;
			
			double levelGapChanceToDrop = Util.map(levelDifference, -Config.DROP_ITEM_MAX_LEVEL_DIFFERENCE, -Config.DROP_ITEM_MIN_LEVEL_DIFFERENCE, Config.DROP_ITEM_MIN_LEVEL_GAP_CHANCE, 100.0);
			// There is a chance of level gap that it wont drop this item
			if (levelGapChanceToDrop < (Rnd.nextDouble() * 100))
			{
				return null;
			}
		}
		
		if ((getChance(victim, killer) * chanceModifier) > (Rnd.nextDouble() * 100))
		{
			final List<ItemHolder> items = new ArrayList<>(1);
			long amount = 0;
			double totalChance = 0;
			double random = (Rnd.nextDouble() * 100);
			double chance = 0;
			
			if (Config.L2JMOD_OLD_DROP_BEHAVIOR)
			{
				for (GeneralDropItem item : getItems())
				{
					// Grouped item chance rates should not be modified.
					totalChance += item.getChance();
					
					if (totalChance > 100)
					{
						int chanceOverflow = (int) (totalChance / 100);
						chance = totalChance % 100;
						while (chanceOverflow > 0)
						{
							amount += Rnd.get(item.getMin(victim, killer), item.getMax(victim, killer));
							chanceOverflow--;
						}
					}
					else
					{
						chance = totalChance;
					}
					
					if (chance > random)
					{
						amount += Rnd.get(item.getMin(victim, killer), item.getMax(victim, killer));
					}
					
					if (amount > 0)
					{
						items.add(new ItemHolder(item.getItemId(), amount));
						return items;
					}
				}
			}
			else
			{
				for (GeneralDropItem item : getItems())
				{
					// Grouped item chance rates should not be modified.
					totalChance += item.getChance();
					if (totalChance > random)
					{
						amount = Rnd.get(item.getMin(victim, killer), item.getMax(victim, killer));
						items.add(new ItemHolder(item.getItemId(), amount));
						return items;
					}
				}
			}
		}
		
		return null;
	}
}
