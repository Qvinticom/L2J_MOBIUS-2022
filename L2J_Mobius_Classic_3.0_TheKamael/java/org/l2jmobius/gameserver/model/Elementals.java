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
package org.l2jmobius.gameserver.model;

import java.util.HashMap;
import java.util.Map;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;

public class Elementals
{
	private static final Map<Integer, ElementalItems> TABLE = new HashMap<>();
	
	static
	{
		for (ElementalItems item : ElementalItems.values())
		{
			TABLE.put(item._itemId, item);
		}
	}
	
	protected static final byte NONE = -1;
	protected static final byte FIRE = 0;
	protected static final byte WATER = 1;
	protected static final byte WIND = 2;
	protected static final byte EARTH = 3;
	protected static final byte HOLY = 4;
	protected static final byte DARK = 5;
	
	public static final int FIRST_WEAPON_BONUS = 20;
	public static final int NEXT_WEAPON_BONUS = 5;
	public static final int ARMOR_BONUS = 6;
	
	public static final int[] WEAPON_VALUES =
	{
		0, // Level 1
		25, // Level 2
		75, // Level 3
		150, // Level 4
		175, // Level 5
		225, // Level 6
		300, // Level 7
		325, // Level 8
		375, // Level 9
		450, // Level 10
		475, // Level 11
		525, // Level 12
		600, // Level 13
		Integer.MAX_VALUE
		// TODO: Higher stones
	};
	
	public static final int[] ARMOR_VALUES =
	{
		0, // Level 1
		12, // Level 2
		30, // Level 3
		60, // Level 4
		72, // Level 5
		90, // Level 6
		120, // Level 7
		132, // Level 8
		150, // Level 9
		180, // Level 10
		192, // Level 11
		210, // Level 12
		240, // Level 13
		Integer.MAX_VALUE
		// TODO: Higher stones
	};
	
	/* @formatter:off */
	private static final int[][] CHANCE_TABLE =
	{
		{Config.S_WEAPON_STONE,		Config.S_ARMOR_STONE,		Config.S_WEAPON_CRYSTAL,	Config.S_ARMOR_CRYSTAL,		Config.S_WEAPON_STONE_SUPER,	Config.S_ARMOR_STONE_SUPER,		Config.S_WEAPON_CRYSTAL_SUPER,		Config.S_ARMOR_CRYSTAL_SUPER,		Config.S_WEAPON_JEWEL,		Config.S_ARMOR_JEWEL},
		{Config.S80_WEAPON_STONE,	Config.S80_ARMOR_STONE,		Config.S80_WEAPON_CRYSTAL,	Config.S80_ARMOR_CRYSTAL,	Config.S80_WEAPON_STONE_SUPER,	Config.S80_ARMOR_STONE_SUPER,	Config.S80_WEAPON_CRYSTAL_SUPER,	Config.S80_ARMOR_CRYSTAL_SUPER,		Config.S80_WEAPON_JEWEL,	Config.S80_ARMOR_JEWEL},
		{Config.S84_WEAPON_STONE,	Config.S84_ARMOR_STONE,		Config.S84_WEAPON_CRYSTAL,	Config.S84_ARMOR_CRYSTAL,	Config.S84_WEAPON_STONE_SUPER,	Config.S84_ARMOR_STONE_SUPER,	Config.S84_WEAPON_CRYSTAL_SUPER,	Config.S84_ARMOR_CRYSTAL_SUPER,		Config.S84_WEAPON_JEWEL,	Config.S84_ARMOR_JEWEL},
		{Config.R_WEAPON_STONE,		Config.R_ARMOR_STONE,		Config.R_WEAPON_CRYSTAL,	Config.R_ARMOR_CRYSTAL,		Config.R_WEAPON_STONE_SUPER,	Config.R_ARMOR_STONE_SUPER,		Config.R_WEAPON_CRYSTAL_SUPER,		Config.R_ARMOR_CRYSTAL_SUPER,		Config.R_WEAPON_JEWEL,		Config.R_ARMOR_JEWEL},
		{Config.R95_WEAPON_STONE,	Config.R95_ARMOR_STONE,		Config.R95_WEAPON_CRYSTAL,	Config.R95_ARMOR_CRYSTAL,	Config.R95_WEAPON_STONE_SUPER,	Config.R95_ARMOR_STONE_SUPER,	Config.R95_WEAPON_CRYSTAL_SUPER,	Config.R95_ARMOR_CRYSTAL_SUPER,		Config.R95_WEAPON_JEWEL,	Config.R95_ARMOR_JEWEL},
		{Config.R99_WEAPON_STONE,	Config.R99_ARMOR_STONE,		Config.R99_WEAPON_CRYSTAL,	Config.R99_ARMOR_CRYSTAL,	Config.R99_WEAPON_STONE_SUPER,	Config.R99_ARMOR_STONE_SUPER,	Config.R99_WEAPON_CRYSTAL_SUPER,	Config.R99_ARMOR_CRYSTAL_SUPER,		Config.R99_WEAPON_JEWEL,	Config.R99_ARMOR_JEWEL},
	};	
	/* @formatter:on */
	
	public enum ElementalItemType
	{
		Stone(3),
		StoneSuper(3),
		Crystal(6),
		CrystalSuper(6),
		Jewel(9),
		Energy(12);
		
		public int _maxLevel;
		
		ElementalItemType(int maxLvl)
		{
			_maxLevel = maxLvl;
		}
	}
	
	public enum ElementalItems
	{
		fireStone(FIRE, 9546, ElementalItemType.Stone, 0),
		waterStone(WATER, 9547, ElementalItemType.Stone, 0),
		windStone(WIND, 9549, ElementalItemType.Stone, 0),
		earthStone(EARTH, 9548, ElementalItemType.Stone, 0),
		divineStone(HOLY, 9551, ElementalItemType.Stone, 0),
		darkStone(DARK, 9550, ElementalItemType.Stone, 0),
		
		fireRoughtore(FIRE, 10521, ElementalItemType.Stone, 0),
		waterRoughtore(WATER, 10522, ElementalItemType.Stone, 0),
		windRoughtore(WIND, 10524, ElementalItemType.Stone, 0),
		earthRoughtore(EARTH, 10523, ElementalItemType.Stone, 0),
		divineRoughtore(HOLY, 10526, ElementalItemType.Stone, 0),
		darkRoughtore(DARK, 10525, ElementalItemType.Stone, 0),
		
		fireCrystal(FIRE, 9552, ElementalItemType.Crystal, 0),
		waterCrystal(WATER, 9553, ElementalItemType.Crystal, 0),
		windCrystal(WIND, 9555, ElementalItemType.Crystal, 0),
		earthCrystal(EARTH, 9554, ElementalItemType.Crystal, 0),
		divineCrystal(HOLY, 9557, ElementalItemType.Crystal, 0),
		darkCrystal(DARK, 9556, ElementalItemType.Crystal, 0),
		
		fireJewel(FIRE, 9558, ElementalItemType.Jewel, 0),
		waterJewel(WATER, 9559, ElementalItemType.Jewel, 0),
		windJewel(WIND, 9561, ElementalItemType.Jewel, 0),
		earthJewel(EARTH, 9560, ElementalItemType.Jewel, 0),
		divineJewel(HOLY, 9563, ElementalItemType.Jewel, 0),
		darkJewel(DARK, 9562, ElementalItemType.Jewel, 0),
		
		fireEnergy(FIRE, 9564, ElementalItemType.Energy, 0),
		waterEnergy(WATER, 9565, ElementalItemType.Energy, 0),
		windEnergy(WIND, 9567, ElementalItemType.Energy, 0),
		earthEnergy(EARTH, 9566, ElementalItemType.Energy, 0),
		divineEnergy(HOLY, 9569, ElementalItemType.Energy, 0),
		darkEnergy(DARK, 9568, ElementalItemType.Energy, 0);
		
		public byte _element;
		public int _itemId;
		public ElementalItemType _type;
		public int _fixedPower;
		
		ElementalItems(byte element, int itemId, ElementalItemType type, int fixedPower)
		{
			_element = element;
			_itemId = itemId;
			_type = type;
			_fixedPower = fixedPower;
		}
	}
	
	public static byte getItemElement(int itemId)
	{
		final ElementalItems item = TABLE.get(itemId);
		if (item != null)
		{
			return item._element;
		}
		return NONE;
	}
	
	public static ElementalItems getItemElemental(int itemId)
	{
		return TABLE.get(itemId);
	}
	
	public static int getMaxElementLevel(int itemId)
	{
		final ElementalItems item = TABLE.get(itemId);
		if (item != null)
		{
			return item._type._maxLevel;
		}
		return -1;
	}
	
	public static boolean isSuccess(ItemInstance item, int stoneId)
	{
		int row = -1;
		int column = -1;
		switch (item.getItem().getCrystalType())
		{
			case S:
			{
				row = 0;
				break;
			}
			case S80:
			{
				row = 1;
				break;
			}
			case S84:
			{
				row = 2;
				break;
			}
			case R:
			{
				row = 3;
				break;
			}
			case R95:
			{
				row = 4;
				break;
			}
			case R99:
			{
				row = 5;
				break;
			}
		}
		
		switch (TABLE.get(stoneId)._type)
		{
			case Stone:
			{
				column = item.isWeapon() ? 0 : 1;
				break;
			}
			case Crystal:
			{
				column = item.isWeapon() ? 2 : 3;
				break;
			}
			case StoneSuper:
			{
				column = item.isWeapon() ? 4 : 5;
				break;
			}
			case CrystalSuper:
			{
				column = item.isWeapon() ? 6 : 7;
				break;
			}
			case Jewel:
			{
				column = item.isWeapon() ? 8 : 9;
				break;
			}
		}
		if ((row != -1) && (column != -1))
		{
			return Rnd.get(100) < CHANCE_TABLE[row][column];
		}
		return true;
	}
}
