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
package org.l2jmobius.gameserver.model;

public class DropData
{
	public static final int MAX_CHANCE = 1000000;
	private int _itemId;
	private int _mindrop;
	private int _maxdrop;
	private boolean _sweep;
	private int _chance;
	
	public int getItemId()
	{
		return _itemId;
	}
	
	public void setItemId(int itemId)
	{
		_itemId = itemId;
	}
	
	public int getMinDrop()
	{
		return _mindrop;
	}
	
	public int getMaxDrop()
	{
		return _maxdrop;
	}
	
	public boolean isSweep()
	{
		return _sweep;
	}
	
	public int getChance()
	{
		return _chance;
	}
	
	public void setMinDrop(int mindrop)
	{
		_mindrop = mindrop;
	}
	
	public void setMaxDrop(int maxdrop)
	{
		_maxdrop = maxdrop;
	}
	
	public void setSweep(boolean sweep)
	{
		_sweep = sweep;
	}
	
	public void setChance(int chance)
	{
		_chance = chance;
	}
}
