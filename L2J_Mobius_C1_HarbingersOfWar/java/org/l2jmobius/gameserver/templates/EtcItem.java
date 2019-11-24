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

public class EtcItem extends Item
{
	public static final int TYPE_ARROW = 0;
	public static final int TYPE_MATERIAL = 1;
	public static final int TYPE_PET_COLLAR = 2;
	public static final int TYPE_POTION = 3;
	public static final int TYPE_RECIPE = 4;
	public static final int TYPE_SCROLL = 5;
	public static final int TYPE_QUEST = 6;
	public static final int TYPE_MONEY = 7;
	public static final int TYPE_OTHER = 8;
	public static final int TYPE_SPELLBOOK = 9;
	
	private int _type;
	
	public int getEtcItemType()
	{
		return _type;
	}
	
	public void setEtcItemType(int type)
	{
		_type = type;
	}
}
