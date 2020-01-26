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
package org.l2jmobius.gameserver.model.holders;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.l2jmobius.gameserver.enums.SkillEnchantType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

/**
 * @author Sdw
 */
public class EnchantSkillHolder
{
	private final int _level;
	private final int _enchantFailLevel;
	private final Map<SkillEnchantType, Long> _sp = new EnumMap<>(SkillEnchantType.class);
	private final Map<SkillEnchantType, Integer> _chance = new EnumMap<>(SkillEnchantType.class);
	private final Map<SkillEnchantType, Set<ItemHolder>> _requiredItems = new EnumMap<>(SkillEnchantType.class);
	
	public EnchantSkillHolder(StatSet set)
	{
		_level = set.getInt("level");
		_enchantFailLevel = set.getInt("enchantFailLevel");
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getEnchantFailLevel()
	{
		return _enchantFailLevel;
	}
	
	public void addSp(SkillEnchantType type, long sp)
	{
		_sp.put(type, sp);
	}
	
	public long getSp(SkillEnchantType type)
	{
		return _sp.getOrDefault(type, 0L);
	}
	
	public void addChance(SkillEnchantType type, int chance)
	{
		_chance.put(type, chance);
	}
	
	public int getChance(SkillEnchantType type)
	{
		return _chance.getOrDefault(type, 100);
	}
	
	public void addRequiredItem(SkillEnchantType type, ItemHolder item)
	{
		_requiredItems.computeIfAbsent(type, k -> new HashSet<>()).add(item);
	}
	
	public Set<ItemHolder> getRequiredItems(SkillEnchantType type)
	{
		return _requiredItems.getOrDefault(type, Collections.emptySet());
	}
	
	public ItemHolder getRequiredBook(SkillEnchantType type)
	{
		for (ItemHolder item : _requiredItems.getOrDefault(type, Collections.emptySet()))
		{
			if (item.getId() != Inventory.ADENA_ID)
			{
				return item;
			}
		}
		return null;
	}
	
	public ItemHolder getRequiredAdena(SkillEnchantType type)
	{
		for (ItemHolder item : _requiredItems.getOrDefault(type, Collections.emptySet()))
		{
			if (item.getId() == Inventory.ADENA_ID)
			{
				return item;
			}
		}
		return new ItemHolder(Inventory.ADENA_ID, 0);
	}
}
