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
package com.l2jmobius.gameserver.util;

/**
 * Convert different client enchant levels
 * @author Mobius
 */
public class SkillEnchantConverter
{
	public static int levelToErtheia(int enchantLevel)
	{
		final String original = "" + enchantLevel;
		String output = "";
		for (int i = 0; i < original.length(); i++)
		{
			if (i != 1) // skip extra digit
			{
				output += original.charAt(i);
			}
		}
		return Integer.valueOf(output);
	}
	
	public static int levelToUnderground(int enchantLevel)
	{
		final String original = "" + enchantLevel;
		String output = "";
		for (int i = 0; i < original.length(); i++)
		{
			output += original.charAt(i);
			if (i == 0) // add extra digit
			{
				output += "0";
			}
		}
		return Integer.valueOf(output);
	}
}
