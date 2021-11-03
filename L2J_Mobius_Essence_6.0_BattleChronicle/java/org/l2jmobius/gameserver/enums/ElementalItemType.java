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
package org.l2jmobius.gameserver.enums;

/**
 * @author Mobius
 */
public enum ElementalItemType
{
	STONE(3),
	STONE_SUPER(3),
	CRYSTAL(6),
	CRYSTAL_SUPER(6),
	JEWEL(9),
	ENERGY(12);
	
	private int _maxLevel;
	
	ElementalItemType(int maxLevel)
	{
		_maxLevel = maxLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
}
