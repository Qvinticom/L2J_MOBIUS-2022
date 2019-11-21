/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.templates;

import java.io.Serializable;

public abstract class Item implements Serializable
{
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int SLOT_NONE = 0;
	public static final int SLOT_UNDERWEAR = 1;
	public static final int SLOT_R_EAR = 2;
	public static final int SLOT_L_EAR = 4;
	public static final int SLOT_NECK = 8;
	public static final int SLOT_R_FINGER = 16;
	public static final int SLOT_L_FINGER = 32;
	public static final int SLOT_HEAD = 64;
	public static final int SLOT_R_HAND = 128;
	public static final int SLOT_L_HAND = 256;
	public static final int SLOT_GLOVES = 512;
	public static final int SLOT_CHEST = 1024;
	public static final int SLOT_LEGS = 2048;
	public static final int SLOT_FEET = 4096;
	public static final int SLOT_BACK = 8192;
	public static final int SLOT_LR_HAND = 16384;
	public static final int SLOT_FULL_ARMOR = 32768;
	public static final int MATERIAL_STEEL = 0;
	public static final int MATERIAL_FINE_STEEL = 1;
	public static final int MATERIAL_BLOOD_STEEL = 2;
	public static final int MATERIAL_BRONZE = 3;
	public static final int MATERIAL_SILVER = 4;
	public static final int MATERIAL_GOLD = 5;
	public static final int MATERIAL_MITHRIL = 6;
	public static final int MATERIAL_ORIHARUKON = 7;
	public static final int MATERIAL_PAPER = 8;
	public static final int MATERIAL_WOOD = 9;
	public static final int MATERIAL_CLOTH = 10;
	public static final int MATERIAL_LEATHER = 11;
	public static final int MATERIAL_BONE = 12;
	public static final int MATERIAL_HORN = 13;
	public static final int MATERIAL_DAMASCUS = 14;
	public static final int MATERIAL_ADAMANTAITE = 15;
	public static final int MATERIAL_CHRYSOLITE = 16;
	public static final int MATERIAL_CRYSTAL = 17;
	public static final int MATERIAL_LIQUID = 18;
	public static final int MATERIAL_SCALE_OF_DRAGON = 19;
	public static final int MATERIAL_DYESTUFF = 20;
	public static final int MATERIAL_COBWEB = 21;
	public static final int CRYSTAL_NONE = 1;
	public static final int CRYSTAL_D = 2;
	public static final int CRYSTAL_C = 3;
	public static final int CRYSTAL_B = 4;
	public static final int CRYSTAL_A = 5;
	public static final int CRYSTAL_S = 6;
	private int _itemId;
	private String _name;
	private int _type1;
	private int _type2;
	private int _weight;
	private boolean _crystallizable;
	private boolean _stackable;
	private int _materialType;
	private int _crystalType = 1;
	private int _durability;
	private int _bodyPart;
	private int _referencePrice;
	
	public int getDurability()
	{
		return _durability;
	}
	
	public void setDurability(int durability)
	{
		_durability = durability;
	}
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public int getMaterialType()
	{
		return _materialType;
	}
	
	public void setMaterialType(int materialType)
	{
		_materialType = materialType;
	}
	
	public int getType2()
	{
		return _type2;
	}
	
	public void setType2(int type)
	{
		_type2 = type;
	}
	
	public int getWeight()
	{
		return _weight;
	}
	
	public void setWeight(int weight)
	{
		_weight = weight;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public boolean isCrystallizable()
	{
		return _crystallizable;
	}
	
	public void setCrystallizable(boolean crystallizable)
	{
		_crystallizable = crystallizable;
	}
	
	public int getCrystalType()
	{
		return _crystalType;
	}
	
	public void setCrystalType(int crystalType)
	{
		_crystalType = crystalType;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public int getBodyPart()
	{
		return _bodyPart;
	}
	
	public void setBodyPart(int bodyPart)
	{
		_bodyPart = bodyPart;
	}
	
	public int getType1()
	{
		return _type1;
	}
	
	public void setType1(int type1)
	{
		_type1 = type1;
	}
	
	public boolean isStackable()
	{
		return _stackable;
	}
	
	public void setStackable(boolean stackable)
	{
		_stackable = stackable;
	}
	
	public void setReferencePrice(int price)
	{
		_referencePrice = price;
	}
	
	public int getReferencePrice()
	{
		return _referencePrice;
	}
}
