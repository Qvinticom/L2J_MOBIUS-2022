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
package org.l2jmobius.gameserver.model.conditions;

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skills.Skill;

/**
 * The Class ConditionSlotItemId.
 * @author mkizub
 */
public class ConditionSlotItemId extends ConditionInventory
{
	private final int _itemId;
	private final int _enchantLevel;
	
	/**
	 * Instantiates a new condition slot item id.
	 * @param slot the slot
	 * @param itemId the item id
	 * @param enchantLevel the enchant level
	 */
	public ConditionSlotItemId(int slot, int itemId, int enchantLevel)
	{
		super(slot);
		_itemId = itemId;
		_enchantLevel = enchantLevel;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if ((effector == null) || !effector.isPlayer())
		{
			return false;
		}
		
		final Item itemSlot = effector.getInventory().getPaperdollItem(_slot);
		if (itemSlot != null)
		{
			return (itemSlot.getId() == _itemId) && (itemSlot.getEnchantLevel() >= _enchantLevel);
		}
		return _itemId == 0;
	}
}
