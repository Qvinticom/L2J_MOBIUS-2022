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

import java.util.GregorianCalendar;
import java.util.TimeZone;

public class AddPattern implements NextTime
{
	private int _monthInc = -1;
	private int _monthSet = -1;
	private int _dayOfMonthInc = -1;
	private int _dayOfMonthSet = -1;
	private int _hourOfDayInc = -1;
	private int _hourOfDaySet = -1;
	private int _minuteInc = -1;
	private int _minuteSet = -1;
	
	public AddPattern(String pattern)
	{
		String[] timeparts;
		String[] parts = pattern.split("\\s+");
		if (parts.length == 2)
		{
			String datemodstr;
			String datepartsstr = parts[0];
			String[] dateparts = datepartsstr.split(":");
			if (dateparts.length == 2)
			{
				if (dateparts[0].startsWith("+"))
				{
					_monthInc = Integer.parseInt(dateparts[0].substring(1));
				}
				else
				{
					_monthSet = Integer.parseInt(dateparts[0]) - 1;
				}
			}
			if ((datemodstr = dateparts[dateparts.length - 1]).startsWith("+"))
			{
				_dayOfMonthInc = Integer.parseInt(datemodstr.substring(1));
			}
			else
			{
				_dayOfMonthSet = Integer.parseInt(datemodstr);
			}
		}
		if ((timeparts = parts[parts.length - 1].split(":"))[0].startsWith("+"))
		{
			_hourOfDayInc = Integer.parseInt(timeparts[0].substring(1));
		}
		else
		{
			_hourOfDaySet = Integer.parseInt(timeparts[0]);
		}
		if (timeparts[1].startsWith("+"))
		{
			_minuteInc = Integer.parseInt(timeparts[1].substring(1));
		}
		else
		{
			_minuteSet = Integer.parseInt(timeparts[1]);
		}
	}
	
	@Override
	public long next(long millis)
	{
		final GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault());
		gc.setTimeInMillis(millis);
		if (_monthInc >= 0)
		{
			gc.add(2, _monthInc);
		}
		if (_monthSet >= 0)
		{
			gc.set(2, _monthSet);
		}
		if (_dayOfMonthInc >= 0)
		{
			gc.add(5, _dayOfMonthInc);
		}
		if (_dayOfMonthSet >= 0)
		{
			gc.set(5, _dayOfMonthSet);
		}
		if (_hourOfDayInc >= 0)
		{
			gc.add(11, _hourOfDayInc);
		}
		if (_hourOfDaySet >= 0)
		{
			gc.set(11, _hourOfDaySet);
		}
		if (_minuteInc >= 0)
		{
			gc.add(12, _minuteInc);
		}
		if (_minuteSet >= 0)
		{
			gc.set(12, _minuteSet);
		}
		return gc.getTimeInMillis();
	}
}
