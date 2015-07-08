/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.stats.functions.FuncAdd;

public final class Elementals
{
	private static final Map<Integer, ElementalItems> TABLE = new HashMap<>();
	
	static
	{
		for (ElementalItems item : ElementalItems.values())
		{
			TABLE.put(item._itemId, item);
		}
	}
	
	public static final byte NONE = -1;
	public static final byte FIRE = 0;
	public static final byte WATER = 1;
	public static final byte WIND = 2;
	public static final byte EARTH = 3;
	public static final byte HOLY = 4;
	public static final byte DARK = 5;
	
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
	
	public static enum ElementalItemType
	{
		Stone(3),
		Roughore(3),
		Stone60(3),
		Stone150(3),
		Crystal(6),
		Crystal300(6),
		Jewel(9),
		Energy(12);
		
		public int _maxLevel;
		
		private ElementalItemType(int maxLvl)
		{
			_maxLevel = maxLvl;
		}
	}
	
	public static enum ElementalItems
	{
		fireStone1(FIRE, 9546, ElementalItemType.Stone),
		fireStone2(FIRE, 22635, ElementalItemType.Stone),
		fireStone3(FIRE, 34790, ElementalItemType.Stone),
		fireStone4(FIRE, 37499, ElementalItemType.Stone),
		fireStone5(FIRE, 22919, ElementalItemType.Stone),
		fireStone6(FIRE, 34661, ElementalItemType.Stone60),
		fireStone7(FIRE, 36960, ElementalItemType.Stone60),
		fireStone8(FIRE, 33863, ElementalItemType.Stone60),
		fireStone9(FIRE, 35729, ElementalItemType.Stone60),
		fireStone10(FIRE, 34667, ElementalItemType.Stone150),
		fireStone11(FIRE, 36966, ElementalItemType.Stone150),
		fireStone12(FIRE, 33869, ElementalItemType.Stone150),
		fireStone13(FIRE, 35735, ElementalItemType.Stone150),
		fireStone14(FIRE, 33481, ElementalItemType.Stone150),
		waterStone1(WATER, 9547, ElementalItemType.Stone),
		waterStone2(WATER, 34791, ElementalItemType.Stone),
		waterStone3(WATER, 22636, ElementalItemType.Stone),
		waterStone4(WATER, 37500, ElementalItemType.Stone),
		waterStone5(WATER, 22920, ElementalItemType.Stone),
		waterStone6(WATER, 34662, ElementalItemType.Stone60),
		waterStone7(WATER, 36961, ElementalItemType.Stone60),
		waterStone8(WATER, 33864, ElementalItemType.Stone60),
		waterStone9(WATER, 35730, ElementalItemType.Stone60),
		waterStone10(WATER, 34668, ElementalItemType.Stone150),
		waterStone11(WATER, 36967, ElementalItemType.Stone150),
		waterStone12(WATER, 33870, ElementalItemType.Stone150),
		waterStone13(WATER, 35736, ElementalItemType.Stone150),
		waterStone14(WATER, 33482, ElementalItemType.Stone150),
		windStone1(WIND, 9549, ElementalItemType.Stone),
		windStone2(WIND, 34793, ElementalItemType.Stone),
		windStone3(WIND, 22638, ElementalItemType.Stone),
		windStone4(WIND, 37502, ElementalItemType.Stone),
		windStone5(WIND, 22922, ElementalItemType.Stone),
		windStone6(WIND, 34664, ElementalItemType.Stone60),
		windStone7(WIND, 36963, ElementalItemType.Stone60),
		windStone8(WIND, 33866, ElementalItemType.Stone60),
		windStone9(WIND, 35732, ElementalItemType.Stone60),
		windStone10(WIND, 34670, ElementalItemType.Stone150),
		windStone11(WIND, 36969, ElementalItemType.Stone150),
		windStone12(WIND, 33872, ElementalItemType.Stone150),
		windStone13(WIND, 35738, ElementalItemType.Stone150),
		windStone14(WIND, 33484, ElementalItemType.Stone150),
		earthStone1(EARTH, 9548, ElementalItemType.Stone),
		earthStone2(EARTH, 34792, ElementalItemType.Stone),
		earthStone3(EARTH, 22637, ElementalItemType.Stone),
		earthStone4(EARTH, 37501, ElementalItemType.Stone),
		earthStone5(EARTH, 22921, ElementalItemType.Stone),
		earthStone6(EARTH, 34663, ElementalItemType.Stone60),
		earthStone7(EARTH, 36962, ElementalItemType.Stone60),
		earthStone8(EARTH, 33865, ElementalItemType.Stone60),
		earthStone9(EARTH, 35731, ElementalItemType.Stone60),
		earthStone10(EARTH, 34669, ElementalItemType.Stone150),
		earthStone11(EARTH, 36968, ElementalItemType.Stone150),
		earthStone12(EARTH, 33871, ElementalItemType.Stone150),
		earthStone13(EARTH, 35737, ElementalItemType.Stone150),
		earthStone14(EARTH, 33483, ElementalItemType.Stone150),
		divineStone1(HOLY, 9551, ElementalItemType.Stone),
		divineStone2(HOLY, 34795, ElementalItemType.Stone),
		divineStone3(HOLY, 22640, ElementalItemType.Stone),
		divineStone4(HOLY, 37504, ElementalItemType.Stone),
		divineStone5(HOLY, 22924, ElementalItemType.Stone),
		divineStone6(HOLY, 34666, ElementalItemType.Stone60),
		divineStone7(HOLY, 36965, ElementalItemType.Stone60),
		divineStone8(HOLY, 33868, ElementalItemType.Stone60),
		divineStone9(HOLY, 35734, ElementalItemType.Stone60),
		divineStone10(HOLY, 34672, ElementalItemType.Stone150),
		divineStone11(HOLY, 36971, ElementalItemType.Stone150),
		divineStone12(HOLY, 33874, ElementalItemType.Stone150),
		divineStone13(HOLY, 35740, ElementalItemType.Stone150),
		divineStone14(HOLY, 33486, ElementalItemType.Stone150),
		darkStone1(DARK, 9550, ElementalItemType.Stone),
		darkStone2(DARK, 34794, ElementalItemType.Stone),
		darkStone3(DARK, 22639, ElementalItemType.Stone),
		darkStone4(DARK, 37503, ElementalItemType.Stone),
		darkStone5(DARK, 22923, ElementalItemType.Stone),
		darkStone6(DARK, 34665, ElementalItemType.Stone60),
		darkStone7(DARK, 36964, ElementalItemType.Stone60),
		darkStone8(DARK, 33867, ElementalItemType.Stone60),
		darkStone9(DARK, 35733, ElementalItemType.Stone60),
		darkStone10(DARK, 34671, ElementalItemType.Stone150),
		darkStone11(DARK, 36970, ElementalItemType.Stone150),
		darkStone12(DARK, 33873, ElementalItemType.Stone150),
		darkStone13(DARK, 35739, ElementalItemType.Stone150),
		darkStone14(DARK, 33485, ElementalItemType.Stone150),
		
		fireRoughtore(FIRE, 10521, ElementalItemType.Roughore),
		waterRoughtore(WATER, 10522, ElementalItemType.Roughore),
		windRoughtore(WIND, 10524, ElementalItemType.Roughore),
		earthRoughtore(EARTH, 10523, ElementalItemType.Roughore),
		divineRoughtore(HOLY, 10526, ElementalItemType.Roughore),
		darkRoughtore(DARK, 10525, ElementalItemType.Roughore),
		
		fireCrystal1(FIRE, 9552, ElementalItemType.Crystal),
		fireCrystal2(FIRE, 34796, ElementalItemType.Crystal),
		fireCrystal3(FIRE, 22925, ElementalItemType.Crystal),
		fireCrystal4(FIRE, 22641, ElementalItemType.Crystal),
		fireCrystal5(FIRE, 36972, ElementalItemType.Crystal300),
		fireCrystal6(FIRE, 33487, ElementalItemType.Crystal300),
		waterCrystal1(WATER, 9553, ElementalItemType.Crystal),
		waterCrystal2(WATER, 34797, ElementalItemType.Crystal),
		waterCrystal3(WATER, 22926, ElementalItemType.Crystal),
		waterCrystal4(WATER, 22642, ElementalItemType.Crystal),
		waterCrystal5(WATER, 36973, ElementalItemType.Crystal300),
		waterCrystal6(WATER, 33488, ElementalItemType.Crystal300),
		windCrystal1(WIND, 9555, ElementalItemType.Crystal),
		windCrystal2(WIND, 34799, ElementalItemType.Crystal),
		windCrystal3(WIND, 22928, ElementalItemType.Crystal),
		windCrystal4(WIND, 22644, ElementalItemType.Crystal),
		windCrystal5(WIND, 36975, ElementalItemType.Crystal300),
		windCrystal6(WIND, 33490, ElementalItemType.Crystal300),
		earthCrystal1(EARTH, 9554, ElementalItemType.Crystal),
		earthCrystal2(EARTH, 34798, ElementalItemType.Crystal),
		earthCrystal3(EARTH, 22927, ElementalItemType.Crystal),
		earthCrystal4(EARTH, 22643, ElementalItemType.Crystal),
		earthCrystal5(EARTH, 36974, ElementalItemType.Crystal300),
		earthCrystal6(EARTH, 33489, ElementalItemType.Crystal300),
		divineCrystal1(HOLY, 9557, ElementalItemType.Crystal),
		divineCrystal2(HOLY, 34801, ElementalItemType.Crystal),
		divineCrystal3(HOLY, 22930, ElementalItemType.Crystal),
		divineCrystal4(HOLY, 22646, ElementalItemType.Crystal),
		divineCrystal5(HOLY, 36977, ElementalItemType.Crystal300),
		divineCrystal6(HOLY, 33492, ElementalItemType.Crystal300),
		darkCrystal1(DARK, 9556, ElementalItemType.Crystal),
		darkCrystal2(DARK, 34800, ElementalItemType.Crystal),
		darkCrystal3(DARK, 22929, ElementalItemType.Crystal),
		darkCrystal4(DARK, 22645, ElementalItemType.Crystal),
		darkCrystal5(DARK, 36796, ElementalItemType.Crystal300),
		darkCrystal6(DARK, 33491, ElementalItemType.Crystal300),
		
		fireJewel(FIRE, 9558, ElementalItemType.Jewel),
		waterJewel(WATER, 9559, ElementalItemType.Jewel),
		windJewel(WIND, 9561, ElementalItemType.Jewel),
		earthJewel(EARTH, 9560, ElementalItemType.Jewel),
		divineJewel(HOLY, 9563, ElementalItemType.Jewel),
		darkJewel(DARK, 9562, ElementalItemType.Jewel),
		
		// not yet supported by client (Freya pts)
		fireEnergy(FIRE, 9564, ElementalItemType.Energy),
		waterEnergy(WATER, 9565, ElementalItemType.Energy),
		windEnergy(WIND, 9567, ElementalItemType.Energy),
		earthEnergy(EARTH, 9566, ElementalItemType.Energy),
		divineEnergy(HOLY, 9569, ElementalItemType.Energy),
		darkEnergy(DARK, 9568, ElementalItemType.Energy);
		
		public byte _element;
		public int _itemId;
		public ElementalItemType _type;
		
		private ElementalItems(byte element, int itemId, ElementalItemType type)
		{
			_element = element;
			_itemId = itemId;
			_type = type;
		}
	}
	
	public static byte getItemElement(int itemId)
	{
		ElementalItems item = TABLE.get(itemId);
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
		ElementalItems item = TABLE.get(itemId);
		if (item != null)
		{
			return item._type._maxLevel;
		}
		return -1;
	}
	
	public static String getElementName(byte element)
	{
		switch (element)
		{
			case FIRE:
				return "Fire";
			case WATER:
				return "Water";
			case WIND:
				return "Wind";
			case EARTH:
				return "Earth";
			case DARK:
				return "Dark";
			case HOLY:
				return "Holy";
		}
		return "None";
	}
	
	public static byte getElementId(String name)
	{
		String tmp = name.toLowerCase();
		if (tmp.equals("fire"))
		{
			return FIRE;
		}
		if (tmp.equals("water"))
		{
			return WATER;
		}
		if (tmp.equals("wind"))
		{
			return WIND;
		}
		if (tmp.equals("earth"))
		{
			return EARTH;
		}
		if (tmp.equals("dark"))
		{
			return DARK;
		}
		if (tmp.equals("holy"))
		{
			return HOLY;
		}
		return NONE;
	}
	
	public static byte getOppositeElement(byte element)
	{
		return (byte) (((element % 2) == 0) ? (element + 1) : (element - 1));
	}
	
	public static class ElementalStatBoni
	{
		private byte _elementalType;
		private int _elementalValue;
		private boolean _active;
		
		public ElementalStatBoni(byte type, int value)
		{
			_elementalType = type;
			_elementalValue = value;
			_active = false;
		}
		
		public void applyBonus(L2PcInstance player, boolean isArmor)
		{
			// make sure the bonuses are not applied twice..
			if (_active)
			{
				return;
			}
			
			switch (_elementalType)
			{
				case FIRE:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.FIRE_RES : Stats.FIRE_POWER, 0x40, this, _elementalValue, null));
					break;
				case WATER:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.WATER_RES : Stats.WATER_POWER, 0x40, this, _elementalValue, null));
					break;
				case WIND:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.WIND_RES : Stats.WIND_POWER, 0x40, this, _elementalValue, null));
					break;
				case EARTH:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.EARTH_RES : Stats.EARTH_POWER, 0x40, this, _elementalValue, null));
					break;
				case DARK:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.DARK_RES : Stats.DARK_POWER, 0x40, this, _elementalValue, null));
					break;
				case HOLY:
					player.addStatFunc(new FuncAdd(isArmor ? Stats.HOLY_RES : Stats.HOLY_POWER, 0x40, this, _elementalValue, null));
					break;
			}
			
			_active = true;
		}
		
		public void removeBonus(L2PcInstance player)
		{
			// make sure the bonuses are not removed twice
			if (!_active)
			{
				return;
			}
			
			player.removeStatsOwner(this);
			
			_active = false;
		}
		
		public void setValue(int val)
		{
			_elementalValue = val;
		}
		
		public void setElement(byte type)
		{
			_elementalType = type;
		}
	}
	
	// non static:
	private ElementalStatBoni _boni = null;
	private byte _element = NONE;
	private int _value = 0;
	
	public byte getElement()
	{
		return _element;
	}
	
	public void setElement(byte type)
	{
		_element = type;
		_boni.setElement(type);
	}
	
	public int getValue()
	{
		return _value;
	}
	
	public void setValue(int val)
	{
		_value = val;
		_boni.setValue(val);
	}
	
	@Override
	public String toString()
	{
		return getElementName(_element) + " +" + _value;
	}
	
	public Elementals(byte type, int value)
	{
		_element = type;
		_value = value;
		_boni = new ElementalStatBoni(_element, _value);
	}
	
	public void applyBonus(L2PcInstance player, boolean isArmor)
	{
		_boni.applyBonus(player, isArmor);
	}
	
	public void removeBonus(L2PcInstance player)
	{
		_boni.removeBonus(player);
	}
	
	public void updateBonus(L2PcInstance player, boolean isArmor)
	{
		_boni.removeBonus(player);
		_boni.applyBonus(player, isArmor);
	}
}
