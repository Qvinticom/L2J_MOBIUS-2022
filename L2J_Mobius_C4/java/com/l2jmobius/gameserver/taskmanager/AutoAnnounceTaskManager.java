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
package com.l2jmobius.gameserver.taskmanager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.util.Broadcast;

import javolution.util.FastList;

/**
 * @author nBd
 */
public class AutoAnnounceTaskManager
{
	protected static final Logger _log = Logger.getLogger(AutoAnnounceTaskManager.class.getName());
	
	private static AutoAnnounceTaskManager _instance;
	protected List<AutoAnnouncement> _announces = new FastList<>();
	
	public static AutoAnnounceTaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new AutoAnnounceTaskManager();
		}
		
		return _instance;
	}
	
	public AutoAnnounceTaskManager()
	{
		restore();
	}
	
	public void restore()
	{
		if (!_announces.isEmpty())
		{
			for (final AutoAnnouncement a : _announces)
			{
				a.stopAnnounce();
			}
			
			_announces.clear();
		}
		
		java.sql.Connection con = null;
		@SuppressWarnings("unused")
		final int count = 0;
		try
		{
			con = L2DatabaseFactory.getInstance().getConnection();
			final PreparedStatement statement = con.prepareStatement("SELECT id, initial, delay, cycle, memo FROM auto_announcements");
			final ResultSet data = statement.executeQuery();
			
			while (data.next())
			{
				final int id = data.getInt("id");
				final long initial = data.getLong("initial");
				final long delay = data.getLong("delay");
				final int repeat = data.getInt("cycle");
				final String memo = data.getString("memo");
				final String[] text = memo.split("/n");
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(id, delay, repeat, text), initial);
			}
			data.close();
			statement.close();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "AutoAnnouncements: Failed to load announcements data.", e);
		}
		finally
		{
			try
			{
				if (con != null)
				{
					con.close();
				}
			}
			catch (final Exception e)
			{
			}
		}
		_log.log(Level.INFO, "AutoAnnouncements: Loaded " + _announces.size() + " Auto Announcement Data.");
	}
	
	private class AutoAnnouncement implements Runnable
	{
		private final int _id;
		private final long _delay;
		private int _repeat = -1;
		private final String[] _memo;
		private boolean _stopped = false;
		
		public AutoAnnouncement(int id, long delay, int repeat, String[] memo)
		{
			_id = id;
			_delay = delay;
			_repeat = repeat;
			_memo = memo;
			if (!_announces.contains(this))
			{
				_announces.add(this);
			}
		}
		
		public void stopAnnounce()
		{
			_stopped = true;
		}
		
		@Override
		public void run()
		{
			for (final String text : _memo)
			{
				announce(text);
			}
			
			if (!_stopped && (_repeat > 0))
			{
				ThreadPoolManager.getInstance().scheduleGeneral(new AutoAnnouncement(_id, _delay, _repeat--, _memo), _delay);
			}
		}
	}
	
	public void announce(String text)
	{
		Broadcast.announceToOnlinePlayers(text);
		_log.warning("AutoAnnounce: " + text);
	}
}