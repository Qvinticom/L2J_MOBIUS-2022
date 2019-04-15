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
package org.l2jmobius.gameserver.enums;

import org.l2jmobius.gameserver.model.items.Armor;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.Weapon;

/**
 * @author Nik
 */
public enum CrystallizationType
{
	NONE,
	WEAPON,
	ARMOR,
	ACCESORY;
	
	public static CrystallizationType getByItem(Item item)
	{
		if (item instanceof Weapon)
		{
			return WEAPON;
		}
		if (item instanceof Armor)
		{
			return ARMOR;
		}
		switch (item.getBodyPart())
		{
			case Item.SLOT_R_EAR:
			case Item.SLOT_L_EAR:
			case Item.SLOT_R_FINGER:
			case Item.SLOT_L_FINGER:
			case Item.SLOT_NECK:
			case Item.SLOT_HAIR:
			case Item.SLOT_HAIR2:
			case Item.SLOT_HAIRALL:
			{
				return ACCESORY;
			}
		}
		
		return NONE;
	}
}
