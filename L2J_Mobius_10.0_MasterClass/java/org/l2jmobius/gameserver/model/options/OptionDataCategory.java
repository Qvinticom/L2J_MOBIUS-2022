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
package org.l2jmobius.gameserver.model.options;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.util.Rnd;

/**
 * @author Pere, Mobius
 */
public class OptionDataCategory
{
	private final Map<Options, Double> _options;
	private final List<Integer> _itemIds;
	private final double _chance;
	
	public OptionDataCategory(Map<Options, Double> options, List<Integer> itemIds, double chance)
	{
		_options = options;
		_itemIds = itemIds;
		_chance = chance;
	}
	
	Options getRandomOptions()
	{
		Options result = null;
		do
		{
			double random = Rnd.nextDouble() * 100.0;
			for (Entry<Options, Double> entry : _options.entrySet())
			{
				if (entry.getValue() >= random)
				{
					result = entry.getKey();
					break;
				}
				
				random -= entry.getValue();
			}
		}
		while (result == null);
		return result;
	}
	
	public List<Integer> getItemIds()
	{
		return _itemIds;
	}
	
	public double getChance()
	{
		return _chance;
	}
}