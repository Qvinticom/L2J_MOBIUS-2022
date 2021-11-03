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
package org.l2jmobius.gameserver.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.data.xml.ArmorSetData;
import org.l2jmobius.gameserver.model.ArmorSet;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Sahar
 */
public final class PaperdollCache
{
	private final Set<ItemInstance> _paperdollItems = ConcurrentHashMap.newKeySet();
	
	private final Map<BaseStat, Double> _baseStatValues = new ConcurrentHashMap<>();
	private final Map<Stat, Double> _statValues = new ConcurrentHashMap<>();
	private int _maxSetEnchant = -1;
	
	public Set<ItemInstance> getPaperdollItems()
	{
		return _paperdollItems;
	}
	
	public void clearCachedStats()
	{
		_baseStatValues.clear();
		_statValues.clear();
		
		clearMaxSetEnchant();
	}
	
	public void clearMaxSetEnchant()
	{
		_maxSetEnchant = -1;
	}
	
	public double getBaseStatValue(PlayerInstance player, BaseStat stat)
	{
		final Double baseStatValue = _baseStatValues.get(stat);
		if (baseStatValue != null)
		{
			return baseStatValue.doubleValue();
		}
		
		final Set<ArmorSet> appliedSets = new HashSet<>(2);
		double value = 0;
		for (ItemInstance item : _paperdollItems)
		{
			for (ArmorSet set : ArmorSetData.getInstance().getSets(item.getId()))
			{
				if ((set.getPiecesCountById(player) >= set.getMinimumPieces()) && appliedSets.add(set))
				{
					value += set.getStatsBonus(stat);
				}
			}
		}
		
		_baseStatValues.put(stat, value);
		return value;
	}
	
	public int getMaxSetEnchant(PlayerInstance player)
	{
		if (_maxSetEnchant >= 0)
		{
			return _maxSetEnchant;
		}
		
		int maxSetEnchant = 0;
		for (ItemInstance item : _paperdollItems)
		{
			for (ArmorSet set : ArmorSetData.getInstance().getSets(item.getId()))
			{
				final int enchantEffect = set.getLowestSetEnchant(player);
				if (enchantEffect > maxSetEnchant)
				{
					maxSetEnchant = enchantEffect;
				}
			}
		}
		
		_maxSetEnchant = maxSetEnchant;
		return maxSetEnchant;
	}
	
	public double getStats(Stat stat)
	{
		final Double statValue = _statValues.get(stat);
		if (statValue != null)
		{
			return statValue.doubleValue();
		}
		
		double value = 0;
		for (ItemInstance item : _paperdollItems)
		{
			value += item.getItem().getStats(stat, 0);
		}
		
		_statValues.put(stat, value);
		return value;
	}
}