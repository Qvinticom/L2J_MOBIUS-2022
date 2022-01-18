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
package org.l2jmobius.gameserver.model.stats;

import java.util.OptionalDouble;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.enums.PlayerCondOverride;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.transform.TransformType;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.CrystalType;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

/**
 * @author UnAfraid
 */
@FunctionalInterface
public interface IStatFunction
{
	default void throwIfPresent(OptionalDouble base)
	{
		if (base.isPresent())
		{
			throw new IllegalArgumentException("base should not be set for " + getClass().getSimpleName());
		}
	}
	
	default double calcEnchantBodyPart(Creature creature, int... slots)
	{
		double value = 0;
		for (int slot : slots)
		{
			final Item item = creature.getInventory().getPaperdollItemByItemId(slot);
			if ((item != null) && (item.getEnchantLevel() >= 4) && (item.getItem().getCrystalTypePlus() == CrystalType.R))
			{
				value += calcEnchantBodyPartBonus(item.getEnchantLevel(), item.getItem().isBlessed());
			}
		}
		return value;
	}
	
	default double calcEnchantBodyPartBonus(int enchantLevel, boolean isBlessed)
	{
		return 0;
	}
	
	default double calcWeaponBaseValue(Creature creature, Stat stat)
	{
		final double baseTemplateValue = creature.getTemplate().getBaseValue(stat, 0);
		double baseValue = creature.getTransformation().map(transform -> transform.getStats(creature, stat, baseTemplateValue)).orElse(baseTemplateValue);
		if (creature.isPet())
		{
			final Pet pet = (Pet) creature;
			final Item weapon = pet.getActiveWeaponInstance();
			final double baseVal = stat == Stat.PHYSICAL_ATTACK ? pet.getPetLevelData().getPetPAtk() : stat == Stat.MAGIC_ATTACK ? pet.getPetLevelData().getPetMAtk() : baseTemplateValue;
			baseValue = baseVal + (weapon != null ? weapon.getItem().getStats(stat, baseVal) : 0);
		}
		else if (creature.isPlayer() && (!creature.isTransformed() || (creature.getTransformation().get().getType() == TransformType.COMBAT) || (creature.getTransformation().get().getType() == TransformType.MODE_CHANGE)))
		{
			final Item weapon = creature.getActiveWeaponInstance();
			baseValue = (weapon != null ? weapon.getItem().getStats(stat, baseTemplateValue) : baseTemplateValue);
		}
		
		return baseValue;
	}
	
	default double calcWeaponPlusBaseValue(Creature creature, Stat stat)
	{
		final double baseTemplateValue = creature.getTemplate().getBaseValue(stat, 0);
		double baseValue = creature.getTransformation().filter(transform -> !transform.isStance()).map(transform -> transform.getStats(creature, stat, baseTemplateValue)).orElse(baseTemplateValue);
		
		if (creature.isPlayable())
		{
			final Inventory inv = creature.getInventory();
			if (inv != null)
			{
				baseValue += inv.getPaperdollCache().getStats(stat);
			}
		}
		
		return baseValue;
	}
	
	default double calcEnchantedItemBonus(Creature creature, Stat stat)
	{
		if (!creature.isPlayer())
		{
			return 0;
		}
		
		double value = 0;
		for (Item equippedItem : creature.getInventory().getPaperdollItems(Item::isEnchanted))
		{
			final ItemTemplate item = equippedItem.getItem();
			final long bodypart = item.getBodyPart();
			if ((bodypart == ItemTemplate.SLOT_HAIR) || //
				(bodypart == ItemTemplate.SLOT_HAIR2) || //
				(bodypart == ItemTemplate.SLOT_HAIRALL))
			{
				// TODO: Item after enchant shows pDef, but scroll says mDef increase.
				if ((stat != Stat.PHYSICAL_DEFENCE) && (stat != Stat.MAGICAL_DEFENCE))
				{
					continue;
				}
			}
			else if (item.getStats(stat, 0) <= 0)
			{
				continue;
			}
			
			final double blessedBonus = item.isBlessed() ? 1.5 : 1;
			int enchant = equippedItem.getEnchantLevel();
			
			if (creature.getActingPlayer().isInOlympiadMode())
			{
				if (item.isWeapon())
				{
					if ((Config.ALT_OLY_WEAPON_ENCHANT_LIMIT >= 0) && (enchant > Config.ALT_OLY_WEAPON_ENCHANT_LIMIT))
					{
						enchant = Config.ALT_OLY_WEAPON_ENCHANT_LIMIT;
					}
				}
				else
				{
					if ((Config.ALT_OLY_ARMOR_ENCHANT_LIMIT >= 0) && (enchant > Config.ALT_OLY_ARMOR_ENCHANT_LIMIT))
					{
						enchant = Config.ALT_OLY_ARMOR_ENCHANT_LIMIT;
					}
				}
			}
			
			if ((stat == Stat.MAGICAL_DEFENCE) || (stat == Stat.PHYSICAL_DEFENCE))
			{
				value += calcEnchantDefBonus(equippedItem, blessedBonus, enchant);
			}
			else if (stat == Stat.MAGIC_ATTACK)
			{
				value += calcEnchantMatkBonus(equippedItem, blessedBonus, enchant);
			}
			else if ((stat == Stat.PHYSICAL_ATTACK) && equippedItem.isWeapon())
			{
				value += calcEnchantedPAtkBonus(equippedItem, blessedBonus, enchant);
			}
		}
		return value;
	}
	
	/**
	 * @param item
	 * @param blessedBonus
	 * @param enchant
	 * @return
	 */
	static double calcEnchantDefBonus(Item item, double blessedBonus, int enchant)
	{
		return enchant + (3 * Math.max(0, enchant - 3)); // Fixed formula for Classic.
	}
	
	/**
	 * @param item
	 * @param blessedBonus
	 * @param enchant
	 * @return
	 */
	static double calcEnchantMatkBonus(Item item, double blessedBonus, int enchant)
	{
		// M. Atk. increases by 3 for all weapons. Starting at +4, M. Atk. bonus double.
		return (3 * enchant) + (3 * Math.max(0, enchant - 3)); // Fixed formula for Classic.
	}
	
	/**
	 * @param item
	 * @param blessedBonus
	 * @param enchant
	 * @return
	 */
	static double calcEnchantedPAtkBonus(Item item, double blessedBonus, int enchant)
	{
		switch (item.getItem().getCrystalTypePlus()) // Fixed formula for Classic.
		{
			case S:
			{
				if (item.getWeaponItem().getItemType().isRanged())
				{
					// P. Atk. is increased by 10 for Bows. Starting at +4, P. Atk. bonus double.
					return (10 * enchant) + (10 * Math.max(0, enchant - 3));
				}
				if ((item.getWeaponItem().getBodyPart() == ItemTemplate.SLOT_R_HAND) || (item.getWeaponItem().getItemType() == WeaponType.POLE))
				{
					// P. Atk. increases by 5 for One-Handed Weapons and Poles. Starting at +4, P. Atk. bonus double.
					return (5 * enchant) + (5 * Math.max(0, enchant - 3));
				}
				// P. Atk. increases by 6 for Two-Handed Weapons, except Spears. Starting at +4, P. Atk. bonus double.
				return (6 * enchant) + (6 * Math.max(0, enchant - 3));
			}
			default: // From A-Grade and below, all formula are the same.
			{
				if (item.getWeaponItem().getItemType().isRanged())
				{
					// P. Atk. is increased by 8 for Bows. Starting at +4, P. Atk. bonus double.
					return (8 * enchant) + (8 * Math.max(0, enchant - 3));
				}
				if ((item.getWeaponItem().getBodyPart() == ItemTemplate.SLOT_R_HAND) || (item.getWeaponItem().getItemType() == WeaponType.POLE))
				{
					// P. Atk. increases by 4 for One-Handed Weapons and Poles. Starting at +4, P. Atk. bonus double.
					return (4 * enchant) + (4 * Math.max(0, enchant - 3));
				}
				// P. Atk. increases by 5 for Two-Handed Weapons, except Spears. Starting at +4, P. Atk. bonus double.
				return (5 * enchant) + (5 * Math.max(0, enchant - 3));
			}
		}
	}
	
	default double validateValue(Creature creature, double value, double minValue, double maxValue)
	{
		if ((value > maxValue) && !creature.canOverrideCond(PlayerCondOverride.MAX_STATS_VALUE))
		{
			return maxValue;
		}
		
		return Math.max(minValue, value);
	}
	
	double calc(Creature creature, OptionalDouble base, Stat stat);
}
