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
package org.l2jmobius.commons.time;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.TreeMap;

import org.l2jmobius.commons.util.Rnd;

public class SchedulingPattern implements NextTime
{
	private static final int MINUTE_MIN_VALUE = 0;
	private static final int MINUTE_MAX_VALUE = 59;
	private static final int HOUR_MIN_VALUE = 0;
	private static final int HOUR_MAX_VALUE = 23;
	private static final int DAY_OF_MONTH_MIN_VALUE = 1;
	private static final int DAY_OF_MONTH_MAX_VALUE = 31;
	private static final int MONTH_MIN_VALUE = 1;
	private static final int MONTH_MAX_VALUE = 12;
	private static final int DAY_OF_WEEK_MIN_VALUE = 0;
	private static final int DAY_OF_WEEK_MAX_VALUE = 7;
	private static final ValueParser MINUTE_VALUE_PARSER = new MinuteValueParser();
	private static final ValueParser HOUR_VALUE_PARSER = new HourValueParser();
	private static final ValueParser DAY_OF_MONTH_VALUE_PARSER = new DayOfMonthValueParser();
	private static final ValueParser MONTH_VALUE_PARSER = new MonthValueParser();
	private static final ValueParser DAY_OF_WEEK_VALUE_PARSER = new DayOfWeekValueParser();
	private final String _asString;
	protected List<ValueMatcher> _minuteMatchers = new ArrayList<>();
	protected List<ValueMatcher> _hourMatchers = new ArrayList<>();
	protected List<ValueMatcher> _dayOfMonthMatchers = new ArrayList<>();
	protected List<ValueMatcher> _monthMatchers = new ArrayList<>();
	protected List<ValueMatcher> _dayOfWeekMatchers = new ArrayList<>();
	protected int _matcherSize = 0;
	protected Map<Integer, Integer> _hourAdder = new TreeMap<>();
	protected Map<Integer, Integer> _hourAdderRnd = new TreeMap<>();
	protected Map<Integer, Integer> _dayOfYearAdder = new TreeMap<>();
	protected Map<Integer, Integer> _minuteAdderRnd = new TreeMap<>();
	protected Map<Integer, Integer> _weekOfYearAdder = new TreeMap<>();
	
	public static boolean validate(String schedulingPattern)
	{
		try
		{
			new SchedulingPattern(schedulingPattern);
		}
		catch (RuntimeException e)
		{
			return false;
		}
		return true;
	}
	
	public SchedulingPattern(String pattern) throws RuntimeException
	{
		_asString = pattern;
		StringTokenizer st1 = new StringTokenizer(pattern, "|");
		if (st1.countTokens() < 1)
		{
			throw new RuntimeException("invalid pattern: \"" + pattern + "\"");
		}
		
		while (st1.hasMoreTokens())
		{
			int i;
			String localPattern = st1.nextToken();
			StringTokenizer st2 = new StringTokenizer(localPattern, " \t");
			int tokCnt = st2.countTokens();
			if ((tokCnt < 5) || (tokCnt > 6))
			{
				throw new RuntimeException("invalid pattern: \"" + localPattern + "\"");
			}
			
			try
			{
				String minutePattern = st2.nextToken();
				String[] minutePatternParts = minutePattern.split(":");
				if (minutePatternParts.length > 1)
				{
					for (i = 0; i < (minutePatternParts.length - 1); ++i)
					{
						if (minutePatternParts[i].length() <= 1)
						{
							continue;
						}
						
						if (minutePatternParts[i].startsWith("~"))
						{
							_minuteAdderRnd.put(_matcherSize, Integer.parseInt(minutePatternParts[i].substring(1)));
							continue;
						}
						
						throw new RuntimeException("Unknown hour modifier \"" + minutePatternParts[i] + "\"");
					}
					minutePattern = minutePatternParts[minutePatternParts.length - 1];
				}
				_minuteMatchers.add(buildValueMatcher(minutePattern, MINUTE_VALUE_PARSER));
			}
			catch (Exception e)
			{
				throw new RuntimeException("invalid pattern \"" + localPattern + "\". Error parsing minutes field: " + e.getMessage() + ".");
			}
			
			try
			{
				String hourPattern = st2.nextToken();
				String[] hourPatternParts = hourPattern.split(":");
				if (hourPatternParts.length > 1)
				{
					for (i = 0; i < (hourPatternParts.length - 1); ++i)
					{
						if (hourPatternParts[i].length() <= 1)
						{
							continue;
						}
						
						if (hourPatternParts[i].startsWith("+"))
						{
							_hourAdder.put(_matcherSize, Integer.parseInt(hourPatternParts[i].substring(1)));
							continue;
						}
						
						if (hourPatternParts[i].startsWith("~"))
						{
							_hourAdderRnd.put(_matcherSize, Integer.parseInt(hourPatternParts[i].substring(1)));
							continue;
						}
						
						throw new RuntimeException("Unknown hour modifier \"" + hourPatternParts[i] + "\"");
					}
					hourPattern = hourPatternParts[hourPatternParts.length - 1];
				}
				_hourMatchers.add(buildValueMatcher(hourPattern, HOUR_VALUE_PARSER));
			}
			catch (Exception e)
			{
				throw new RuntimeException("invalid pattern \"" + localPattern + "\". Error parsing hours field: " + e.getMessage() + ".");
			}
			
			try
			{
				String dayOfMonthPattern = st2.nextToken();
				String[] dayOfMonthPatternParts = dayOfMonthPattern.split(":");
				if (dayOfMonthPatternParts.length > 1)
				{
					for (i = 0; i < (dayOfMonthPatternParts.length - 1); ++i)
					{
						if (dayOfMonthPatternParts[i].length() <= 1)
						{
							continue;
						}
						
						if (dayOfMonthPatternParts[i].startsWith("+"))
						{
							_dayOfYearAdder.put(_matcherSize, Integer.parseInt(dayOfMonthPatternParts[i].substring(1)));
							continue;
						}
						
						throw new RuntimeException("Unknown day modifier \"" + dayOfMonthPatternParts[i] + "\"");
					}
					dayOfMonthPattern = dayOfMonthPatternParts[dayOfMonthPatternParts.length - 1];
				}
				_dayOfMonthMatchers.add(buildValueMatcher(dayOfMonthPattern, DAY_OF_MONTH_VALUE_PARSER));
			}
			catch (Exception e)
			{
				throw new RuntimeException("invalid pattern \"" + localPattern + "\". Error parsing days of month field: " + e.getMessage() + ".");
			}
			
			try
			{
				_monthMatchers.add(buildValueMatcher(st2.nextToken(), MONTH_VALUE_PARSER));
			}
			catch (Exception e)
			{
				throw new RuntimeException("invalid pattern \"" + localPattern + "\". Error parsing months field: " + e.getMessage() + ".");
			}
			
			try
			{
				_dayOfWeekMatchers.add(buildValueMatcher(st2.nextToken(), DAY_OF_WEEK_VALUE_PARSER));
			}
			catch (Exception e)
			{
				throw new RuntimeException("invalid pattern \"" + localPattern + "\". Error parsing days of week field: " + e.getMessage() + ".");
			}
			
			if (st2.hasMoreTokens())
			{
				try
				{
					String weekOfYearAdderText = st2.nextToken();
					if (weekOfYearAdderText.charAt(0) != '+')
					{
						throw new RuntimeException("Unknown week of year addition in pattern \"" + localPattern + "\".");
					}
					weekOfYearAdderText = weekOfYearAdderText.substring(1);
					_weekOfYearAdder.put(_matcherSize, Integer.parseInt(weekOfYearAdderText));
				}
				catch (Exception e)
				{
					throw new RuntimeException("invalid pattern \"" + localPattern + "\". Error parsing days of week field: " + e.getMessage() + ".");
				}
			}
			++_matcherSize;
		}
	}
	
	private ValueMatcher buildValueMatcher(String str, ValueParser parser) throws Exception
	{
		if ((str.length() == 1) && str.equals("*"))
		{
			return new AlwaysTrueValueMatcher();
		}
		
		ArrayList<Integer> values = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(str, ",");
		while (st.hasMoreTokens())
		{
			List<Integer> local;
			String element = st.nextToken();
			try
			{
				local = parseListElement(element, parser);
			}
			catch (Exception e)
			{
				throw new Exception("invalid field \"" + str + "\", invalid element \"" + element + "\", " + e.getMessage());
			}
			
			for (Integer value : local)
			{
				if (values.contains(value))
				{
					continue;
				}
				
				values.add(value);
			}
		}
		
		if (values.size() == 0)
		{
			throw new Exception("invalid field \"" + str + "\"");
		}
		
		if (parser == DAY_OF_MONTH_VALUE_PARSER)
		{
			return new DayOfMonthValueMatcher(values);
		}
		
		return new IntArrayValueMatcher(values);
	}
	
	private List<Integer> parseListElement(String str, ValueParser parser) throws Exception
	{
		List<Integer> values;
		StringTokenizer st = new StringTokenizer(str, "/");
		int size = st.countTokens();
		if ((size < 1) || (size > 2))
		{
			throw new Exception("syntax error");
		}
		
		try
		{
			values = parseRange(st.nextToken(), parser);
		}
		catch (Exception e)
		{
			throw new Exception("invalid range, " + e.getMessage());
		}
		
		if (size == 2)
		{
			int div;
			String dStr = st.nextToken();
			
			try
			{
				div = Integer.parseInt(dStr);
			}
			catch (NumberFormatException e)
			{
				throw new Exception("invalid divisor \"" + dStr + "\"");
			}
			
			if (div < 1)
			{
				throw new Exception("non positive divisor \"" + div + "\"");
			}
			
			ArrayList<Integer> values2 = new ArrayList<>();
			for (int i = 0; i < values.size(); i += div)
			{
				values2.add(values.get(i));
			}
			
			return values2;
		}
		return values;
	}
	
	private List<Integer> parseRange(String str, ValueParser parser) throws Exception
	{
		int v2;
		int v1;
		if (str.equals("*"))
		{
			int min = parser.getMinValue();
			int max = parser.getMaxValue();
			ArrayList<Integer> values = new ArrayList<>();
			for (int i = min; i <= max; ++i)
			{
				values.add(i);
			}
			return values;
		}
		
		StringTokenizer st = new StringTokenizer(str, "-");
		int size = st.countTokens();
		if ((size < 1) || (size > 2))
		{
			throw new Exception("syntax error");
		}
		
		String v1Str = st.nextToken();
		try
		{
			v1 = parser.parse(v1Str);
		}
		catch (Exception e)
		{
			throw new Exception("invalid value \"" + v1Str + "\", " + e.getMessage());
		}
		
		if (size == 1)
		{
			ArrayList<Integer> values = new ArrayList<>();
			values.add(v1);
			return values;
		}
		
		String v2Str = st.nextToken();
		try
		{
			v2 = parser.parse(v2Str);
		}
		catch (Exception e)
		{
			throw new Exception("invalid value \"" + v2Str + "\", " + e.getMessage());
		}
		
		ArrayList<Integer> values = new ArrayList<>();
		if (v1 < v2)
		{
			for (int i = v1; i <= v2; ++i)
			{
				values.add(i);
			}
		}
		else if (v1 > v2)
		{
			int i;
			int min = parser.getMinValue();
			int max = parser.getMaxValue();
			for (i = v1; i <= max; ++i)
			{
				values.add(i);
			}
			for (i = min; i <= v2; ++i)
			{
				values.add(i);
			}
		}
		else
		{
			values.add(v1);
		}
		
		return values;
	}
	
	public boolean match(TimeZone timezone, long millis)
	{
		GregorianCalendar gc = new GregorianCalendar(timezone);
		gc.setTimeInMillis(millis);
		gc.set(13, 0);
		gc.set(14, 0);
		for (int i = 0; i < _matcherSize; ++i)
		{
			boolean eval = false;
			if (_weekOfYearAdder.containsKey(i))
			{
				gc.add(3, -_weekOfYearAdder.get(i).intValue());
			}
			if (_dayOfYearAdder.containsKey(i))
			{
				gc.add(6, -_dayOfYearAdder.get(i).intValue());
			}
			if (_hourAdder.containsKey(i))
			{
				gc.add(10, -_hourAdder.get(i).intValue());
			}
			int minute = gc.get(MONTH_MAX_VALUE);
			int hour = gc.get(11);
			int dayOfMonth = gc.get(5);
			int month = gc.get(2) + 1;
			int dayOfWeek = gc.get(DAY_OF_WEEK_MAX_VALUE) - 1;
			int year = gc.get(1);
			ValueMatcher minuteMatcher = _minuteMatchers.get(i);
			ValueMatcher hourMatcher = _hourMatchers.get(i);
			ValueMatcher dayOfMonthMatcher = _dayOfMonthMatchers.get(i);
			ValueMatcher monthMatcher = _monthMatchers.get(i);
			ValueMatcher dayOfWeekMatcher = _dayOfWeekMatchers.get(i);
			@SuppressWarnings("unused")
			boolean bl = minuteMatcher.match(minute) && hourMatcher.match(hour) && (dayOfMonthMatcher instanceof DayOfMonthValueMatcher ? ((DayOfMonthValueMatcher) dayOfMonthMatcher).match(dayOfMonth, month, gc.isLeapYear(year)) : dayOfMonthMatcher.match(dayOfMonth)) && monthMatcher.match(month) && dayOfWeekMatcher.match(dayOfWeek) ? true : (eval = false);
			if (!eval)
			{
				continue;
			}
			return true;
		}
		return false;
	}
	
	public boolean match(long millis)
	{
		return match(TimeZone.getDefault(), millis);
	}
	
	public long next(TimeZone timezone, long millis)
	{
		long result = -1L;
		GregorianCalendar gc = new GregorianCalendar(timezone);
		for (int i = 0; i < _matcherSize; ++i)
		{
			long next = -1L;
			gc.setTimeInMillis(millis);
			gc.set(13, 0);
			gc.set(14, 0);
			if (_weekOfYearAdder.containsKey(i))
			{
				gc.add(3, _weekOfYearAdder.get(i));
			}
			if (_dayOfYearAdder.containsKey(i))
			{
				gc.add(6, _dayOfYearAdder.get(i));
			}
			if (_hourAdder.containsKey(i))
			{
				gc.add(10, _hourAdder.get(i));
			}
			ValueMatcher minuteMatcher = _minuteMatchers.get(i);
			ValueMatcher hourMatcher = _hourMatchers.get(i);
			ValueMatcher dayOfMonthMatcher = _dayOfMonthMatchers.get(i);
			ValueMatcher monthMatcher = _monthMatchers.get(i);
			ValueMatcher dayOfWeekMatcher = _dayOfWeekMatchers.get(i);
			SEARCH: do
			{
				int year = gc.get(1);
				boolean isLeapYear = gc.isLeapYear(year);
				for (int month = gc.get(2) + 1; month <= MONTH_MAX_VALUE; ++month)
				{
					if (monthMatcher.match(month))
					{
						gc.set(2, month - 1);
						int maxDayOfMonth = DayOfMonthValueMatcher.getLastDayOfMonth(month, isLeapYear);
						for (int dayOfMonth = gc.get(5); dayOfMonth <= maxDayOfMonth; ++dayOfMonth)
						{
							if (dayOfMonthMatcher instanceof DayOfMonthValueMatcher ? ((DayOfMonthValueMatcher) dayOfMonthMatcher).match(dayOfMonth, month, isLeapYear) : dayOfMonthMatcher.match(dayOfMonth))
							{
								gc.set(5, dayOfMonth);
								int dayOfWeek = gc.get(DAY_OF_WEEK_MAX_VALUE) - 1;
								if (dayOfWeekMatcher.match(dayOfWeek))
								{
									for (int hour = gc.get(11); hour <= HOUR_MAX_VALUE; ++hour)
									{
										if (hourMatcher.match(hour))
										{
											gc.set(11, hour);
											for (int minute = gc.get(MONTH_MAX_VALUE); minute <= MINUTE_MAX_VALUE; ++minute)
											{
												if (!minuteMatcher.match(minute))
												{
													continue;
												}
												
												gc.set(MONTH_MAX_VALUE, minute);
												long next0 = gc.getTimeInMillis();
												if (next0 <= millis)
												{
													continue;
												}
												
												if ((next != -1L) && (next0 >= next))
												{
													break SEARCH;
												}
												
												next = next0;
												if (_hourAdderRnd.containsKey(i))
												{
													next += Rnd.get(_hourAdderRnd.get(i)) * 60 * 60 * 1000L;
												}
												
												if (!_minuteAdderRnd.containsKey(i))
												{
													break SEARCH;
												}
												
												next += Rnd.get(_minuteAdderRnd.get(i)) * 60 * 1000L;
												break SEARCH;
											}
										}
										gc.set(MONTH_MAX_VALUE, 0);
									}
								}
							}
							gc.set(11, 0);
							gc.set(MONTH_MAX_VALUE, 0);
						}
					}
					gc.set(5, 1);
					gc.set(11, 0);
					gc.set(MONTH_MAX_VALUE, 0);
				}
				gc.set(2, 0);
				gc.set(11, 0);
				gc.set(MONTH_MAX_VALUE, 0);
				gc.roll(1, true);
			}
			
			while (true);
			if ((next <= millis) || ((result != -1L) && (next >= result)))
			{
				continue;
			}
			
			result = next;
		}
		return result;
	}
	
	@Override
	public long next(long millis)
	{
		return next(TimeZone.getDefault(), millis);
	}
	
	@Override
	public String toString()
	{
		return _asString;
	}
	
	private static int parseAlias(String value, String[] aliases, int offset) throws Exception
	{
		for (int i = 0; i < aliases.length; ++i)
		{
			if (!aliases[i].equalsIgnoreCase(value))
			{
				continue;
			}
			return offset + i;
		}
		throw new Exception("invalid alias \"" + value + "\"");
	}
	
	private static class DayOfMonthValueMatcher extends IntArrayValueMatcher
	{
		private static final int[] lastDays = new int[]
		{
			DAY_OF_MONTH_MAX_VALUE,
			28,
			DAY_OF_MONTH_MAX_VALUE,
			30,
			DAY_OF_MONTH_MAX_VALUE,
			30,
			DAY_OF_MONTH_MAX_VALUE,
			DAY_OF_MONTH_MAX_VALUE,
			30,
			DAY_OF_MONTH_MAX_VALUE,
			30,
			DAY_OF_MONTH_MAX_VALUE
		};
		
		public DayOfMonthValueMatcher(List<Integer> integers)
		{
			super(integers);
		}
		
		public boolean match(int value, int month, boolean isLeapYear)
		{
			return super.match(value) || ((value > 27) && match(32) && DayOfMonthValueMatcher.isLastDayOfMonth(value, month, isLeapYear));
		}
		
		public static int getLastDayOfMonth(int month, boolean isLeapYear)
		{
			if (isLeapYear && (month == 2))
			{
				return 29;
			}
			
			return lastDays[month - 1];
		}
		
		public static boolean isLastDayOfMonth(int value, int month, boolean isLeapYear)
		{
			return value == DayOfMonthValueMatcher.getLastDayOfMonth(month, isLeapYear);
		}
	}
	
	private static class IntArrayValueMatcher implements ValueMatcher
	{
		private final int[] values;
		
		public IntArrayValueMatcher(List<Integer> integers)
		{
			int size = integers.size();
			values = new int[size];
			for (int i = 0; i < size; ++i)
			{
				try
				{
					values[i] = integers.get(i);
					continue;
				}
				catch (Exception e)
				{
					throw new IllegalArgumentException(e.getMessage());
				}
			}
		}
		
		@Override
		public boolean match(int value)
		{
			for (int i = 0; i < values.length; ++i)
			{
				if (values[i] != value)
				{
					continue;
				}
				return true;
			}
			return false;
		}
	}
	
	private static class AlwaysTrueValueMatcher implements ValueMatcher
	{
		private AlwaysTrueValueMatcher()
		{
		}
		
		@Override
		public boolean match(int value)
		{
			return true;
		}
	}
	
	private static interface ValueMatcher
	{
		public boolean match(int var1);
	}
	
	private static class DayOfWeekValueParser extends SimpleValueParser
	{
		private static String[] ALIASES = new String[]
		{
			"sun",
			"mon",
			"tue",
			"wed",
			"thu",
			"fri",
			"sat"
		};
		
		public DayOfWeekValueParser()
		{
			super(DAY_OF_WEEK_MIN_VALUE, DAY_OF_WEEK_MAX_VALUE);
		}
		
		@Override
		public int parse(String value) throws Exception
		{
			try
			{
				return super.parse(value) % DAY_OF_WEEK_MAX_VALUE;
			}
			catch (Exception e)
			{
				return SchedulingPattern.parseAlias(value, ALIASES, 0);
			}
		}
	}
	
	private static class MonthValueParser extends SimpleValueParser
	{
		private static String[] ALIASES = new String[]
		{
			"jan",
			"feb",
			"mar",
			"apr",
			"may",
			"jun",
			"jul",
			"aug",
			"sep",
			"oct",
			"nov",
			"dec"
		};
		
		public MonthValueParser()
		{
			super(MONTH_MIN_VALUE, MONTH_MAX_VALUE);
		}
		
		@Override
		public int parse(String value) throws Exception
		{
			try
			{
				return super.parse(value);
			}
			catch (Exception e)
			{
				return SchedulingPattern.parseAlias(value, ALIASES, 1);
			}
		}
	}
	
	private static class DayOfMonthValueParser extends SimpleValueParser
	{
		public DayOfMonthValueParser()
		{
			super(DAY_OF_MONTH_MIN_VALUE, DAY_OF_MONTH_MAX_VALUE);
		}
		
		@Override
		public int parse(String value) throws Exception
		{
			if (value.equalsIgnoreCase("L"))
			{
				return 32;
			}
			return super.parse(value);
		}
	}
	
	private static class HourValueParser extends SimpleValueParser
	{
		public HourValueParser()
		{
			super(HOUR_MIN_VALUE, HOUR_MAX_VALUE);
		}
	}
	
	private static class MinuteValueParser extends SimpleValueParser
	{
		public MinuteValueParser()
		{
			super(MINUTE_MIN_VALUE, MINUTE_MAX_VALUE);
		}
	}
	
	private static class SimpleValueParser implements ValueParser
	{
		protected int _minValue;
		protected int _maxValue;
		
		public SimpleValueParser(int minValue, int maxValue)
		{
			_minValue = minValue;
			_maxValue = maxValue;
		}
		
		@Override
		public int parse(String value) throws Exception
		{
			int i;
			try
			{
				i = Integer.parseInt(value);
			}
			catch (NumberFormatException e)
			{
				throw new Exception("invalid integer value");
			}
			if ((i < _minValue) || (i > _maxValue))
			{
				throw new Exception("value out of range");
			}
			return i;
		}
		
		@Override
		public int getMinValue()
		{
			return _minValue;
		}
		
		@Override
		public int getMaxValue()
		{
			return _maxValue;
		}
	}
	
	private static interface ValueParser
	{
		public int parse(String var1) throws Exception;
		
		public int getMinValue();
		
		public int getMaxValue();
	}
}
