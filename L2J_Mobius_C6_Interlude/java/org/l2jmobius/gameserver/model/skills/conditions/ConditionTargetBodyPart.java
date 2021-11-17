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
package org.l2jmobius.gameserver.model.skills.conditions;

import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.model.items.Armor;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.skills.Env;

/**
 * @author mkizub
 */
public class ConditionTargetBodyPart extends Condition
{
	private final Armor _armor;
	
	public ConditionTargetBodyPart(Armor armor)
	{
		_armor = armor;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		// target is attacker
		if (env.target == null)
		{
			return true;
		}
		final int bodypart = env.target.getAttackingBodyPart();
		final int armor_part = _armor.getBodyPart();
		switch (bodypart)
		{
			case Inventory.PAPERDOLL_CHEST:
			{
				return (armor_part & (ItemTemplate.SLOT_CHEST | ItemTemplate.SLOT_FULL_ARMOR | ItemTemplate.SLOT_UNDERWEAR)) != 0;
			}
			case Inventory.PAPERDOLL_LEGS:
			{
				return (armor_part & (ItemTemplate.SLOT_LEGS | ItemTemplate.SLOT_FULL_ARMOR)) != 0;
			}
			case Inventory.PAPERDOLL_HEAD:
			{
				return (armor_part & ItemTemplate.SLOT_HEAD) != 0;
			}
			case Inventory.PAPERDOLL_FEET:
			{
				return (armor_part & ItemTemplate.SLOT_FEET) != 0;
			}
			case Inventory.PAPERDOLL_GLOVES:
			{
				return (armor_part & ItemTemplate.SLOT_GLOVES) != 0;
			}
			default:
			{
				return true;
			}
		}
	}
}
