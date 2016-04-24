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
package com.l2jmobius.gameserver.model.items.enchant;

import java.util.logging.Logger;

import com.l2jmobius.gameserver.datatables.ItemTable;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.CrystalType;
import com.l2jmobius.gameserver.model.items.type.EtcItemType;
import com.l2jmobius.gameserver.model.items.type.ItemType;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public abstract class AbstractEnchantItem
{
	protected static final Logger _log = Logger.getLogger(AbstractEnchantItem.class.getName());
	
	private static final ItemType[] ENCHANT_TYPES = new ItemType[]
	{
		EtcItemType.ANCIENT_CRYSTAL_ENCHANT_AM,
		EtcItemType.ANCIENT_CRYSTAL_ENCHANT_WP,
		EtcItemType.BLESS_SCRL_ENCHANT_AM,
		EtcItemType.BLESS_SCRL_ENCHANT_WP,
		EtcItemType.SCRL_ENCHANT_AM,
		EtcItemType.SCRL_ENCHANT_WP,
		EtcItemType.SCRL_INC_ENCHANT_PROP_AM,
		EtcItemType.SCRL_INC_ENCHANT_PROP_WP,
		EtcItemType.BLESS_SCRL_INC_ENCHANT_PROP_AM,
		EtcItemType.BLESS_SCRL_INC_ENCHANT_PROP_WP,
		EtcItemType.GIANT_SCRL_ENCHANT_AM,
		EtcItemType.GIANT_SCRL_ENCHANT_WP,
		EtcItemType.GIANT_SCRL_INC_ENCHANT_PROP_AM,
		EtcItemType.GIANT_SCRL_INC_ENCHANT_PROP_WP,
		EtcItemType.GIANT_SCRL_BLESS_INC_ENCHANT_PROP_AM,
		EtcItemType.GIANT_SCRL_BLESS_INC_ENCHANT_PROP_WP,
		EtcItemType.SCRL_BLESS_INC_ENCHANT_PROP_AM,
		EtcItemType.SCRL_BLESS_INC_ENCHANT_PROP_WP,
		EtcItemType.BLESS_DROP_SCRL_INC_ENCHANT_PROP_AM,
		EtcItemType.BLESS_DROP_SCRL_INC_ENCHANT_PROP_WP,
		EtcItemType.GIANT2_SCRL_BLESS_INC_ENCHANT_PROP_AM,
		EtcItemType.GIANT2_SCRL_BLESS_INC_ENCHANT_PROP_WP,
		EtcItemType.SCRL_ENCHANT_HR
	};
	
	private final int _id;
	private final CrystalType _grade;
	private final int _minEnchantLevel;
	private final int _maxEnchantLevel;
	private final int _maxEnchantLevelFighter;
	private final int _maxEnchantLevelMagic;
	private final double _bonusRate;
	
	public AbstractEnchantItem(StatsSet set)
	{
		_id = set.getInt("id");
		if (getItem() == null)
		{
			throw new NullPointerException();
		}
		if (!Util.contains(ENCHANT_TYPES, getItem().getItemType()))
		{
			throw new IllegalAccessError();
		}
		_grade = set.getEnum("targetGrade", CrystalType.class, CrystalType.NONE);
		_minEnchantLevel = set.getInt("minEnchant", 0);
		_maxEnchantLevel = set.getInt("maxEnchant", 127);
		_maxEnchantLevelFighter = set.getInt("maxEnchantFighter", 127);
		_maxEnchantLevelMagic = set.getInt("maxEnchantMagic", 127);
		_bonusRate = set.getDouble("bonusRate", 0);
	}
	
	/**
	 * @return id of current item
	 */
	public final int getId()
	{
		return _id;
	}
	
	/**
	 * @return bonus chance that would be added
	 */
	public final double getBonusRate()
	{
		return _bonusRate;
	}
	
	/**
	 * @return {@link L2Item} current item/scroll
	 */
	public final L2Item getItem()
	{
		return ItemTable.getInstance().getTemplate(_id);
	}
	
	/**
	 * @return grade of the item/scroll.
	 */
	public final CrystalType getGrade()
	{
		return _grade;
	}
	
	/**
	 * @return {@code true} if scroll is for weapon, {@code false} for armor
	 */
	public abstract boolean isWeapon();
	
	/**
	 * @return the minimum enchant level that this scroll/item can be used with
	 */
	public int getMinEnchantLevel()
	{
		return _minEnchantLevel;
	}
	
	/**
	 * @return the maximum enchant level that this scroll/item can be used with
	 */
	public int getMaxEnchantLevel()
	{
		return _maxEnchantLevel;
	}
	
	/**
	 * @return the maximum enchant level that fighter weapon can be enchanted with this scroll
	 */
	public int getMaxEnchantLevelFighter()
	{
		return _maxEnchantLevelFighter;
	}
	
	/**
	 * @return the maximum enchant level that magic weapon can be enchanted with this scroll
	 */
	public int getMaxEnchantLevelMagic()
	{
		return _maxEnchantLevelMagic;
	}
	
	/**
	 * @param itemToEnchant the item to be enchanted
	 * @param supportItem
	 * @return {@code true} if this support item can be used with the item to be enchanted, {@code false} otherwise
	 */
	public boolean isValid(L2ItemInstance itemToEnchant, EnchantSupportItem supportItem)
	{
		if (itemToEnchant == null)
		{
			return false;
		}
		else if (itemToEnchant.isEnchantable() == 0)
		{
			return false;
		}
		else if (!isValidItemType(itemToEnchant.getItem().getType2()))
		{
			return false;
		}
		else if ((_minEnchantLevel != 0) && (itemToEnchant.getEnchantLevel() < _minEnchantLevel))
		{
			return false;
		}
		else if ((_maxEnchantLevel != 0) && (itemToEnchant.getEnchantLevel() >= _maxEnchantLevel))
		{
			return false;
		}
		else if ((_maxEnchantLevelFighter != 0) && !itemToEnchant.getItem().isMagicWeapon() && (itemToEnchant.getEnchantLevel() >= _maxEnchantLevelFighter))
		{
			return false;
		}
		else if ((_maxEnchantLevelMagic != 0) && itemToEnchant.getItem().isMagicWeapon() && (itemToEnchant.getEnchantLevel() >= _maxEnchantLevelMagic))
		{
			return false;
		}
		else if (_grade != itemToEnchant.getItem().getCrystalTypePlus())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * @param type2
	 * @return {@code true} if current type2 is valid to be enchanted, {@code false} otherwise
	 */
	private final boolean isValidItemType(int type2)
	{
		return type2 == L2Item.TYPE2_WEAPON ? isWeapon() : ((type2 == L2Item.TYPE2_SHIELD_ARMOR) || (type2 == L2Item.TYPE2_ACCESSORY)) && !isWeapon();
	}
}
