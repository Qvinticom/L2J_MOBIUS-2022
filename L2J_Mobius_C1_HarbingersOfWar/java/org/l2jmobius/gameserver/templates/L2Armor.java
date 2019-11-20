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

public class L2Armor extends L2Item
{
	public static final int ARMORTYPE_NONE = 1;
	public static final int ARMORTYPE_LIGHT = 2;
	public static final int ARMORTYPE_HEAVY = 3;
	public static final int ARMORTYPE_MAGIC = 4;
	private int _armorType;
	private int _avoidModifier;
	private int _pDef;
	private int _mDef;
	private int _mpBonus;
	
	public void setAvoidModifier(int i)
	{
		_avoidModifier = i;
	}
	
	public void setMpBonus(int i)
	{
		_mpBonus = i;
	}
	
	public int getMDef()
	{
		return _mDef;
	}
	
	public void setMDef(int def)
	{
		_mDef = def;
	}
	
	public int getPDef()
	{
		return _pDef;
	}
	
	public void setPDef(int def)
	{
		_pDef = def;
	}
	
	public int getAvoidModifier()
	{
		return _avoidModifier;
	}
	
	public int getMpBonus()
	{
		return _mpBonus;
	}
	
	public int getArmorType()
	{
		return _armorType;
	}
	
	public void setArmorType(int armorType)
	{
		_armorType = armorType;
	}
}
