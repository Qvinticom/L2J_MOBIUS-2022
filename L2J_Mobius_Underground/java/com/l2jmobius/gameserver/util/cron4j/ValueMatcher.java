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
package com.l2jmobius.gameserver.util.cron4j;

/**
 * <p>
 * This interface describes the ValueMatcher behavior. A ValueMatcher is an object that validate an integer value against a set of rules.
 * </p>
 * @author Carlo Pelliccia
 */
interface ValueMatcher
{
	/**
	 * Validate the given integer value against a set of rules.
	 * @param value The value.
	 * @return true if the given value matches the rules of the ValueMatcher, false otherwise.
	 */
	public boolean match(int value);
}
