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
package com.l2jmobius.gameserver.script;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author Luis Arias
 */
public class DateRange
{
	
	private final Date startDate, endDate;
	
	public DateRange(Date from, Date to)
	{
		startDate = from;
		endDate = to;
	}
	
	public static DateRange parse(String dateRange, DateFormat format)
	{
		final String[] date = dateRange.split("-");
		if (date.length == 2)
		{
			try
			{
				final Date start = format.parse(date[0]);
				final Date end = format.parse(date[1]);
				
				return new DateRange(start, end);
			}
			catch (final ParseException e)
			{
				System.err.println("Invalid Date Format.");
				e.printStackTrace();
			}
		}
		return new DateRange(null, null);
	}
	
	public boolean isValid()
	{
		return (startDate == null) || (endDate == null);
	}
	
	public boolean isWithinRange(Date date)
	{
		return date.after(startDate) && date.before(endDate);
	}
	
	public Date getEndDate()
	{
		return endDate;
	}
	
	public Date getStartDate()
	{
		return startDate;
	}
}