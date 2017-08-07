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
package com.l2jmobius.gameserver.model.eventengine.cron4j;

import java.util.ArrayList;

/**
 * <p>
 * A ValueMatcher whose rules are in a plain array of integer values. When asked to validate a value, this ValueMatcher checks if it is in the array and, if not, checks whether the last-day-of-month setting applies.
 * </p>
 * @author Paul Fernley
 */
class DayOfMonthValueMatcher extends IntArrayValueMatcher
{
	private static final int[] lastDays =
	{
		31,
		28,
		31,
		30,
		31,
		30,
		31,
		31,
		30,
		31,
		30,
		31
	};
	
	/**
	 * Builds the ValueMatcher.
	 * @param values An ArrayList of Integer elements, one for every value accepted by the matcher. The match() method will return true only if its parameter will be one of this list or the last-day-of-month setting applies.
	 */
	public DayOfMonthValueMatcher(ArrayList<?> values)
	{
		super(values);
	}
	
	/**
	 * Returns true if the given value is included in the matcher list or the last-day-of-month setting applies.
	 * @param value
	 * @param month
	 * @param isLeapYear
	 * @return
	 */
	public boolean match(int value, int month, boolean isLeapYear)
	{
		return (super.match(value) || ((value > 27) && match(32) && isLastDayOfMonth(value, month, isLeapYear)));
	}
	
	public boolean isLastDayOfMonth(int value, int month, boolean isLeapYear)
	{
		if (isLeapYear && (month == 2))
		{
			return value == 29;
		}
		return value == lastDays[month - 1];
	}
}
