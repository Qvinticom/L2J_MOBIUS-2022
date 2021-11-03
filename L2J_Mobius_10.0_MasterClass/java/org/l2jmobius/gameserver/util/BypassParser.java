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
package org.l2jmobius.gameserver.util;

import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.l2jmobius.gameserver.model.StatSet;

/**
 * @author UnAfraid
 */
public class BypassParser extends StatSet
{
	private static final String ALLOWED_CHARS = "a-zA-Z0-9-_`!@#%^&*()\\[\\]|\\\\/";
	private static final Pattern PATTERN = Pattern.compile(String.format("([%s]*)=('([%s ]*)'|[%s]*)", ALLOWED_CHARS, ALLOWED_CHARS, ALLOWED_CHARS));
	
	public BypassParser(String bypass)
	{
		super(LinkedHashMap::new);
		process(bypass);
	}
	
	private void process(String bypass)
	{
		final Matcher regexMatcher = PATTERN.matcher(bypass);
		while (regexMatcher.find())
		{
			final String name = regexMatcher.group(1);
			final String escapedValue = regexMatcher.group(2).trim();
			final String unescapedValue = regexMatcher.group(3);
			set(name, unescapedValue != null ? unescapedValue.trim() : escapedValue);
		}
	}
}
