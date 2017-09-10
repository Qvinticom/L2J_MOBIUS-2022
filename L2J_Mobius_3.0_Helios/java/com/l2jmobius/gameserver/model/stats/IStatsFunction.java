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
package com.l2jmobius.gameserver.model.stats;

import java.util.Optional;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.PcCondOverride;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.CrystalType;

/**
 * @author UnAfraid
 */
@FunctionalInterface
public interface IStatsFunction
{
	default void throwIfPresent(Optional<Double> base)
	{
		if (base.isPresent())
		{
			throw new IllegalArgumentException("base should not be set for " + getClass().getSimpleName());
		}
	}
	
	default double calcEnchantBodyPart(L2Character creature, int... slots)
	{
		double value = 0;
		for (int slot : slots)
		{
			final L2ItemInstance item = creature.getInventory().getPaperdollItemByL2ItemId(slot);
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
	
	default double calcWeaponBaseValue(L2Character creature, Stats stat)
	{
		final double baseTemplateBalue = creature.getTemplate().getBaseValue(stat, 0);
		final double baseValue = creature.getTransformation().filter(transform -> !transform.isStance()).map(transform -> transform.getStats(creature, stat, baseTemplateBalue)).orElseGet(() ->
		{
			if (creature.isPet())
			{
				final L2PetInstance pet = (L2PetInstance) creature;
				final L2ItemInstance weapon = pet.getActiveWeaponInstance();
				final double baseVal = stat == Stats.PHYSICAL_ATTACK ? pet.getPetLevelData().getPetPAtk() : stat == Stats.MAGIC_ATTACK ? pet.getPetLevelData().getPetMAtk() : baseTemplateBalue;
				return baseVal + (weapon != null ? weapon.getItem().getStats(stat, baseVal) : 0);
			}
			else if (creature.isPlayer())
			{
				final L2ItemInstance weapon = creature.getActiveWeaponInstance();
				return (weapon != null ? weapon.getItem().getStats(stat, baseTemplateBalue) : baseTemplateBalue);
			}
			
			return baseTemplateBalue;
		});
		
		return baseValue;
	}
	
	default double calcWeaponPlusBaseValue(L2Character creature, Stats stat)
	{
		final double baseTemplateBalue = creature.getTemplate().getBaseValue(stat, 0);
		double baseValue = creature.getTransformation().filter(transform -> !transform.isStance()).map(transform -> transform.getStats(creature, stat, baseTemplateBalue)).orElse(baseTemplateBalue);
		
		if (creature.isPlayable())
		{
			final Inventory inv = creature.getInventory();
			if (inv != null)
			{
				for (L2ItemInstance item : inv.getPaperdollItems(L2ItemInstance::isEquipped))
				{
					baseValue += item.getItem().getStats(stat, 0);
				}
			}
		}
		
		return baseValue;
	}
	
	default double calcEnchantedItemBonus(L2Character creature, Stats stat)
	{
		if (!creature.isPlayer())
		{
			return 0;
		}
		
		double value = 0;
		for (L2ItemInstance item : creature.getInventory().getPaperdollItems(L2ItemInstance::isEquipped, L2ItemInstance::isEnchanted))
		{
			if (item.getItem().getStats(stat, 0) <= 0)
			{
				continue;
			}
			
			final double blessedBonus = item.getItem().isBlessed() ? 1.5 : 1;
			int overEnchant = 0;
			int enchant = item.getEnchantLevel();
			if (enchant > 3)
			{
				overEnchant = enchant - 3;
				enchant = 3;
			}
			
			if (creature.getActingPlayer().isInOlympiadMode() && (Config.ALT_OLY_ENCHANT_LIMIT >= 0) && ((enchant + overEnchant) > Config.ALT_OLY_ENCHANT_LIMIT))
			{
				if (Config.ALT_OLY_ENCHANT_LIMIT > 3)
				{
					overEnchant = Config.ALT_OLY_ENCHANT_LIMIT - 3;
				}
				else
				{
					overEnchant = 0;
					enchant = Config.ALT_OLY_ENCHANT_LIMIT;
				}
			}
			
			if ((stat == Stats.MAGICAL_DEFENCE) || (stat == Stats.PHYSICAL_DEFENCE))
			{
				value += calcEnchantDefBonus(item, blessedBonus, enchant, overEnchant);
			}
			else if (stat == Stats.MAGIC_ATTACK)
			{
				value += calcEnchantMatkBonus(item, blessedBonus, enchant, overEnchant);
			}
			else if ((stat == Stats.PHYSICAL_ATTACK) && item.isWeapon())
			{
				value += calcEnchantedPAtkBonus(item, blessedBonus, enchant, overEnchant);
			}
		}
		return value;
	}
	
	/**
	 * @param item
	 * @param blessedBonus
	 * @param enchant
	 * @param overEnchant
	 * @return
	 */
	static double calcEnchantDefBonus(L2ItemInstance item, double blessedBonus, int enchant, int overEnchant)
	{
		double value = 0;
		switch (item.getItem().getCrystalTypePlus())
		{
			case R:
			{
				// Enchant 0-3 adding +2
				// Enchant 3-6 adding +4
				// Enchant 6-127 adding +6
				switch (overEnchant)
				{
					case 1:
					case 2:
					case 3:
					{
						value += ((2 * blessedBonus * enchant) + (4 * blessedBonus * overEnchant));
						break;
					}
					default:
					{
						value += (6 * blessedBonus * overEnchant);
						break;
					}
				}
				if (value == 0) // 1-3
				{
					value += ((2 * blessedBonus * enchant) + (4 * blessedBonus * overEnchant));
				}
				break;
			}
			case S:
			case A:
			case B:
			case C:
			case D:
			case NONE:
			{
				value += enchant + (3 * overEnchant);
				break;
			}
		}
		return value;
	}
	
	/**
	 * @param item
	 * @param blessedBonus
	 * @param enchant
	 * @param overEnchant
	 * @return
	 */
	static double calcEnchantMatkBonus(L2ItemInstance item, double blessedBonus, int enchant, int overEnchant)
	{
		double value = 0;
		switch (item.getItem().getCrystalTypePlus())
		{
			case R:
			{
				//@formatter:off
				/* M. Atk. increases by 5 for all weapons.
				 * Starting at +4, M. Atk. bonus double.
				 * 0-3 adding +5
				 * 3-6 adding +10
				 * 7-9 adding +15
				 * 10-12 adding +20
				 * 13-127 adding +25
				 */
				//@formatter:on
				switch (overEnchant)
				{
					case 0:
					{
						break;
					}
					case 1:
					case 2:
					case 3:
					{
						value += ((5 * blessedBonus * enchant) + (10 * blessedBonus * overEnchant));
						break;
					}
					case 4:
					case 5:
					case 6:
					{
						value += (15 * blessedBonus * overEnchant);
						break;
					}
					case 7:
					case 8:
					case 9:
					case 10:
					case 11:
					case 12:
					case 13:
					{
						value += (20 * blessedBonus * (overEnchant - 1.5));
						break;
					}
					default:
					{
						value += (25 * blessedBonus * (overEnchant - 3));
						break;
					}
				}
				if (value == 0) // 1-3
				{
					value += ((5 * blessedBonus * enchant) + (10 * blessedBonus * overEnchant));
				}
				break;
			}
			case S:
			{
				// M. Atk. increases by 4 for all weapons.
				// Starting at +4, M. Atk. bonus double.
				value += (4 * enchant) + (8 * overEnchant);
				break;
			}
			case A:
			case B:
			case C:
			{
				// M. Atk. increases by 3 for all weapons.
				// Starting at +4, M. Atk. bonus double.
				value += (3 * enchant) + (6 * overEnchant);
				break;
			}
			case D:
			case NONE:
			{
				// M. Atk. increases by 2 for all weapons. Starting at +4, M. Atk. bonus double.
				// Starting at +4, M. Atk. bonus double.
				value += (2 * enchant) + (4 * overEnchant);
				break;
			}
		}
		return value;
	}
	
	/**
	 * @param item
	 * @param blessedBonus
	 * @param enchant
	 * @param overEnchant
	 * @return
	 */
	static double calcEnchantedPAtkBonus(L2ItemInstance item, double blessedBonus, int enchant, int overEnchant)
	{
		double value = 0;
		switch (item.getItem().getCrystalTypePlus())
		{
			case R:
			{
				if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
				{
					if (item.getWeaponItem().getItemType().isRanged())
					{
						//@formatter:off
						/* P. Atk. increases by 12 for bows.
						 * Starting at +4, P. Atk. bonus double.
						 * 0-3 adding +12 
						 * 4-6 adding +24 
						 * 7-9 adding +36 
						 * 10-12 adding +48 
						 * 13-127 adding +60
						 */
						//@formatter:on
						switch (overEnchant)
						{
							case 0:
							{
								break;
							}
							case 1:
							case 2:
							case 3:
							{
								value += ((12 * blessedBonus * enchant) + (24 * blessedBonus * overEnchant));
								break;
							}
							case 4:
							case 5:
							case 6:
							{
								value += (36 * blessedBonus * overEnchant);
								break;
							}
							case 7:
							case 8:
							case 9:
							case 10:
							case 11:
							case 12:
							{
								value += (48 * blessedBonus * (overEnchant - 1.5));
								break;
							}
							default:
							{
								value += (60 * blessedBonus * (overEnchant - 3));
								break;
							}
						}
						if (value == 0) // 1-3
						{
							value += ((12 * blessedBonus * enchant) + (24 * blessedBonus * overEnchant));
						}
					}
					else
					{
						//@formatter:off
						/* P. Atk. increases by 7 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
						 * Starting at +4, P. Atk. bonus double.
						 * 0-3 adding +7 
						 * 4-6 adding +14 
						 * 7-9 adding +21 
						 * 10-12 adding +28 
						 * 13-127 adding +35
						 */
						//@formatter:on
						switch (overEnchant)
						{
							case 0:
							{
								break;
							}
							case 1:
							case 2:
							case 3:
							{
								value += ((7 * blessedBonus * enchant) + (14 * blessedBonus * overEnchant));
								break;
							}
							case 4:
							case 5:
							case 6:
							{
								value += (21 * blessedBonus * overEnchant);
								break;
							}
							case 7:
							case 8:
							case 9:
							case 10:
							case 11:
							case 12:
							{
								value += (28 * blessedBonus * (overEnchant - 1.5));
								break;
							}
							default:
							{
								value += (35 * blessedBonus * (overEnchant - 3));
								break;
							}
						}
						if (value == 0) // 1-3
						{
							value += ((7 * blessedBonus * enchant) + (14 * blessedBonus * overEnchant));
						}
					}
				}
				else
				{
					//@formatter:off
					/* P. Atk. increases by 6 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
					 * Starting at +4, P. Atk. bonus double.
					 * 0-3 adding +6 
					 * 4-6 adding +12 
					 * 7-9 adding +18 
					 * 10-12 adding +24 
					 * 13-127 adding +30
					 */
					//@formatter:on
					switch (overEnchant)
					{
						case 0:
						{
							break;
						}
						case 1:
						case 2:
						case 3:
						{
							value += ((6 * blessedBonus * enchant) + (12 * blessedBonus * overEnchant));
							break;
						}
						case 4:
						case 5:
						case 6:
						{
							value += (18 * blessedBonus * overEnchant);
							break;
						}
						case 7:
						case 8:
						case 9:
						case 10:
						case 11:
						case 12:
						{
							value += (24 * blessedBonus * (overEnchant - 1.5));
							break;
						}
						default:
						{
							value += (30 * blessedBonus * (overEnchant - 3));
							break;
						}
					}
					if (value == 0) // 1-3
					{
						value += ((6 * blessedBonus * enchant) + (12 * blessedBonus * overEnchant));
					}
				}
				break;
			}
			case S:
			{
				if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
				{
					if (item.getWeaponItem().getItemType().isRanged())
					{
						// P. Atk. increases by 10 for bows.
						// Starting at +4, P. Atk. bonus double.
						value += (10 * enchant) + (20 * overEnchant);
					}
					else
					{
						// P. Atk. increases by 6 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (6 * enchant) + (12 * overEnchant);
					}
				}
				else
				{
					// P. Atk. increases by 5 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
					// Starting at +4, P. Atk. bonus double.
					value += (5 * enchant) + (10 * overEnchant);
				}
				break;
			}
			case A:
			{
				if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
				{
					if (item.getWeaponItem().getItemType().isRanged())
					{
						// P. Atk. increases by 8 for bows.
						// Starting at +4, P. Atk. bonus double.
						value += (8 * enchant) + (16 * overEnchant);
					}
					else
					{
						// P. Atk. increases by 5 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (5 * enchant) + (10 * overEnchant);
					}
				}
				else
				{
					// P. Atk. increases by 4 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
					// Starting at +4, P. Atk. bonus double.
					value += (4 * enchant) + (8 * overEnchant);
				}
				break;
			}
			case B:
			case C:
			{
				if (item.getWeaponItem().getBodyPart() == L2Item.SLOT_LR_HAND)
				{
					if (item.getWeaponItem().getItemType().isRanged())
					{
						// P. Atk. increases by 6 for bows.
						// Starting at +4, P. Atk. bonus double.
						value += (6 * enchant) + (12 * overEnchant);
					}
					else
					{
						// P. Atk. increases by 4 for two-handed swords, two-handed blunts, dualswords, and two-handed combat weapons.
						// Starting at +4, P. Atk. bonus double.
						value += (4 * enchant) + (8 * overEnchant);
					}
				}
				else
				{
					// P. Atk. increases by 3 for one-handed swords, one-handed blunts, daggers, spears, and other weapons.
					// Starting at +4, P. Atk. bonus double.
					value += (3 * enchant) + (6 * overEnchant);
				}
				break;
			}
			case D:
			case NONE:
			{
				if (item.getWeaponItem().getItemType().isRanged())
				{
					// Bows increase by 4.
					// Starting at +4, P. Atk. bonus double.
					value += (4 * enchant) + (8 * overEnchant);
				}
				else
				{
					// P. Atk. increases by 2 for all weapons with the exception of bows.
					// Starting at +4, P. Atk. bonus double.
					value += (2 * enchant) + (4 * overEnchant);
				}
				break;
			}
		}
		return value;
	}
	
	default double validateValue(L2Character creature, double value, double minValue, double maxValue)
	{
		if ((value > maxValue) && !creature.canOverrideCond(PcCondOverride.MAX_STATS_VALUE))
		{
			return maxValue;
		}
		
		return Math.max(minValue, value);
	}
	
	double calc(L2Character creature, Optional<Double> base, Stats stat);
}
