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
package com.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ArmorsetSkillHolder;
import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.itemcontainer.Inventory;
import com.l2jmobius.gameserver.model.itemcontainer.PcInventory;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Luno
 */
public final class L2ArmorSet
{
	private boolean _isVisual;
	private int _minimumPieces;
	private final List<Integer> _chests;
	private final List<Integer> _legs;
	private final List<Integer> _head;
	private final List<Integer> _gloves;
	private final List<Integer> _feet;
	private final List<Integer> _shield;
	
	private final List<ArmorsetSkillHolder> _skills;
	private final List<SkillHolder> _shieldSkills;
	private final List<ArmorsetSkillHolder> _enchantSkills;
	
	private int _con;
	private int _dex;
	private int _str;
	private int _men;
	private int _wit;
	private int _int;
	private int _luc;
	private int _cha;
	
	private static final int[] ARMORSET_SLOTS = new int[]
	{
		Inventory.PAPERDOLL_CHEST,
		Inventory.PAPERDOLL_LEGS,
		Inventory.PAPERDOLL_HEAD,
		Inventory.PAPERDOLL_GLOVES,
		Inventory.PAPERDOLL_FEET
	};
	
	public L2ArmorSet()
	{
		_chests = new ArrayList<>();
		_legs = new ArrayList<>();
		_head = new ArrayList<>();
		_gloves = new ArrayList<>();
		_feet = new ArrayList<>();
		_shield = new ArrayList<>();
		
		_skills = new ArrayList<>();
		_shieldSkills = new ArrayList<>();
		_enchantSkills = new ArrayList<>();
	}
	
	public boolean isVisual()
	{
		return _isVisual;
	}
	
	public void setIsVisual(boolean val)
	{
		_isVisual = val;
	}
	
	/**
	 * @return the minimum amount of pieces equipped to form a set.
	 */
	public int getMinimumPieces()
	{
		return _minimumPieces;
	}
	
	public void setMinimumPieces(int pieces)
	{
		_minimumPieces = pieces;
	}
	
	public void addChest(int id)
	{
		_chests.add(id);
	}
	
	public void addLegs(int id)
	{
		_legs.add(id);
	}
	
	public void addHead(int id)
	{
		_head.add(id);
	}
	
	public void addGloves(int id)
	{
		_gloves.add(id);
	}
	
	public void addFeet(int id)
	{
		_feet.add(id);
	}
	
	public void addShield(int id)
	{
		_shield.add(id);
	}
	
	public void addSkill(ArmorsetSkillHolder holder)
	{
		_skills.add(holder);
	}
	
	public void addShieldSkill(SkillHolder holder)
	{
		_shieldSkills.add(holder);
	}
	
	public void addEnchantSkill(ArmorsetSkillHolder holder)
	{
		_enchantSkills.add(holder);
	}
	
	public void addCon(int val)
	{
		_con = val;
	}
	
	public void addDex(int val)
	{
		_dex = val;
	}
	
	public void addStr(int val)
	{
		_str = val;
	}
	
	public void addMen(int val)
	{
		_men = val;
	}
	
	public void addWit(int val)
	{
		_wit = val;
	}
	
	public void addInt(int val)
	{
		_int = val;
	}
	
	public void addLuc(int val)
	{
		_luc = val;
	}
	
	public void addCha(int val)
	{
		_cha = val;
	}
	
	public int getPiecesCount(L2PcInstance player)
	{
		final Inventory inv = player.getInventory();
		
		final L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		final L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		final L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		final L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		
		if (legsItem != null)
		{
			legs = legsItem.getId();
		}
		if (headItem != null)
		{
			head = headItem.getId();
		}
		if (glovesItem != null)
		{
			gloves = glovesItem.getId();
		}
		if (feetItem != null)
		{
			feet = feetItem.getId();
		}
		
		return getPiecesCount(legs, head, gloves, feet);
	}
	
	public int getPiecesCount(int legs, int head, int gloves, int feet)
	{
		int pieces = 1;
		if (_legs.contains(legs))
		{
			pieces++;
		}
		if (_head.contains(head))
		{
			pieces++;
		}
		if (_gloves.contains(gloves))
		{
			pieces++;
		}
		if (_feet.contains(feet))
		{
			pieces++;
		}
		
		return pieces;
	}
	
	public boolean containItem(int slot, int itemId)
	{
		switch (slot)
		{
			case Inventory.PAPERDOLL_CHEST:
			{
				return _chests.contains(itemId);
			}
			case Inventory.PAPERDOLL_LEGS:
			{
				return _legs.contains(itemId);
			}
			case Inventory.PAPERDOLL_HEAD:
			{
				return _head.contains(itemId);
			}
			case Inventory.PAPERDOLL_GLOVES:
			{
				return _gloves.contains(itemId);
			}
			case Inventory.PAPERDOLL_FEET:
			{
				return _feet.contains(itemId);
			}
			default:
			{
				return false;
			}
		}
	}
	
	public List<Integer> getChests()
	{
		return _chests;
	}
	
	public List<ArmorsetSkillHolder> getSkills()
	{
		return _skills;
	}
	
	public boolean containShield(L2PcInstance player)
	{
		final Inventory inv = player.getInventory();
		
		final L2ItemInstance shieldItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		return ((shieldItem != null) && _shield.contains(Integer.valueOf(shieldItem.getId())));
	}
	
	public boolean containShield(int shield_id)
	{
		if (_shield.isEmpty())
		{
			return false;
		}
		
		return _shield.contains(Integer.valueOf(shield_id));
	}
	
	public List<SkillHolder> getShieldSkills()
	{
		return _shieldSkills;
	}
	
	public List<ArmorsetSkillHolder> getEnchantSkills()
	{
		return _enchantSkills;
	}
	
	/**
	 * @param player
	 * @return true if all parts of set are enchanted to +6 or more
	 */
	public int getLowestSetEnchant(L2PcInstance player)
	{
		// Player don't have full set
		if (getPiecesCount(player) < getMinimumPieces())
		{
			return 0;
		}
		
		final PcInventory inv = player.getInventory();
		
		// No Chest - No Bonus
		if (inv.getPaperdollItem(Inventory.PAPERDOLL_CHEST) == null)
		{
			return 0;
		}
		
		int enchantLevel = Byte.MAX_VALUE;
		for (int armorSlot : ARMORSET_SLOTS)
		{
			final L2ItemInstance itemPart = inv.getPaperdollItem(armorSlot);
			if (itemPart != null)
			{
				if (enchantLevel > itemPart.getEnchantLevel())
				{
					enchantLevel = itemPart.getEnchantLevel();
				}
			}
		}
		if (enchantLevel == Byte.MAX_VALUE)
		{
			enchantLevel = 0;
		}
		return enchantLevel;
	}
	
	public int getVisualPiecesCount(L2PcInstance player)
	{
		final Inventory inv = player.getInventory();
		
		final L2ItemInstance legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS);
		final L2ItemInstance headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD);
		final L2ItemInstance glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES);
		final L2ItemInstance feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET);
		
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		
		if (legsItem != null)
		{
			legs = legsItem.getVisualId();
		}
		if (headItem != null)
		{
			head = headItem.getVisualId();
		}
		if (glovesItem != null)
		{
			gloves = glovesItem.getVisualId();
		}
		if (feetItem != null)
		{
			feet = feetItem.getVisualId();
		}
		return getPiecesCount(legs, head, gloves, feet);
	}
	
	public int getCON()
	{
		return _con;
	}
	
	public int getDEX()
	{
		return _dex;
	}
	
	public int getSTR()
	{
		return _str;
	}
	
	public int getMEN()
	{
		return _men;
	}
	
	public int getWIT()
	{
		return _wit;
	}
	
	public int getINT()
	{
		return _int;
	}
	
	public int getLUC()
	{
		return _luc;
	}
	
	public int getCHA()
	{
		return _cha;
	}
}
