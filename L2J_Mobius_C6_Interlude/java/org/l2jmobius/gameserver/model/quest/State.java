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
package org.l2jmobius.gameserver.model.quest;

/**
 * @author Luis Arias
 */
public class State
{
	public static final byte CREATED = 0;
	public static final byte STARTED = 1;
	public static final byte COMPLETED = 2;
	
	// discover the string representation of the state, for readable DB storage
	public static String getStateName(byte state)
	{
		switch (state)
		{
			case 1:
			{
				return "Started";
			}
			case 2:
			{
				return "Completed";
			}
			default:
			{
				return "Start";
			}
		}
	}
	
	// discover the state from its string representation (for reconstruction after DB read)
	public static byte getStateId(String statename)
	{
		switch (statename)
		{
			case "Started":
			{
				return 1;
			}
			case "Completed":
			{
				return 2;
			}
			default:
			{
				return 0;
			}
		}
	}
}
