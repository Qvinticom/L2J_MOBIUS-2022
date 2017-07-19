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

import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_FIXED_SHEDULED;
import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_GLOBAL_TASK;
import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_NONE;
import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_SHEDULED;
import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_SPECIAL;
import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_STARTUP;
import static com.l2jmobius.gameserver.taskmanager.TaskTypes.TYPE_TIME;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskCleanUp;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskJython;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskOlympiadSave;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskRecom;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskRestart;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskSevenSignsUpdate;
import com.l2jmobius.gameserver.taskmanager.tasks.TaskShutdown;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author Layane
 */
public final class TaskManager
{
	protected static final Logger _log = Logger.getLogger(TaskManager.class.getName());
	private static TaskManager _instance;
	
	protected static final String[] SQL_STATEMENTS =
	{
		"SELECT id,task,type,last_activation,param1,param2,param3 FROM global_tasks",
		"UPDATE global_tasks SET last_activation=? WHERE id=?",
		"SELECT id FROM global_tasks WHERE task=?",
		"INSERT INTO global_tasks (task,type,last_activation,param1,param2,param3) VALUES(?,?,?,?,?,?)"
	};
	
	private final FastMap<Integer, Task> _tasks = new FastMap<>();
	protected final FastList<ExecutedTask> _currentTasks = new FastList<>();
	
	public class ExecutedTask implements Runnable
	{
		int _id;
		long _lastActivation;
		Task _task;
		TaskTypes _type;
		String[] _params;
		ScheduledFuture<?> _scheduled;
		
		public ExecutedTask(Task task, TaskTypes type, ResultSet rset) throws SQLException
		{
			_task = task;
			_type = type;
			_id = rset.getInt("id");
			_lastActivation = rset.getLong("last_activation");
			_params = new String[]
			{
				rset.getString("param1"),
				rset.getString("param2"),
				rset.getString("param3")
			};
		}
		
		@Override
		public void run()
		{
			_task.onTimeElapsed(this);
			
			_lastActivation = System.currentTimeMillis();
			
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[1]))
			{
				statement.setLong(1, _lastActivation);
				statement.setInt(2, _id);
				statement.executeUpdate();
			}
			catch (final SQLException e)
			{
				_log.warning("cannot updated the Global Task " + _id + ": " + e.getMessage());
			}
			
			if ((_type == TYPE_SHEDULED) || (_type == TYPE_TIME))
			{
				stopTask();
			}
		}
		
		@Override
		public boolean equals(Object object)
		{
			return _id == ((ExecutedTask) object)._id;
		}
		
		public Task getTask()
		{
			return _task;
		}
		
		public TaskTypes getType()
		{
			return _type;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public String[] getParams()
		{
			return _params;
		}
		
		public long getLastActivation()
		{
			return _lastActivation;
		}
		
		public void stopTask()
		{
			_task.onDestroy();
			
			if (_scheduled != null)
			{
				_scheduled.cancel(true);
			}
			
			_currentTasks.remove(this);
		}
		
	}
	
	public static TaskManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new TaskManager();
		}
		return _instance;
	}
	
	public TaskManager()
	{
		initializate();
		startAllTasks();
	}
	
	private void initializate()
	{
		registerTask(new TaskCleanUp());
		registerTask(new TaskJython());
		registerTask(new TaskOlympiadSave());
		registerTask(new TaskRecom());
		registerTask(new TaskRestart());
		registerTask(new TaskSevenSignsUpdate());
		registerTask(new TaskShutdown());
	}
	
	public void registerTask(Task task)
	{
		final int key = task.getName().hashCode();
		if (!_tasks.containsKey(key))
		{
			_tasks.put(key, task);
			task.initializate();
		}
	}
	
	private void startAllTasks()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[0]);
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				final Task task = _tasks.get(rset.getString("task").trim().toLowerCase().hashCode());
				
				if (task == null)
				{
					continue;
				}
				
				final TaskTypes type = TaskTypes.valueOf(rset.getString("type"));
				
				if (type != TYPE_NONE)
				{
					final ExecutedTask current = new ExecutedTask(task, type, rset);
					if (launchTask(current))
					{
						_currentTasks.add(current);
					}
				}
			}
		}
		catch (final Exception e)
		{
			_log.severe("error while loading Global Task table " + e);
			e.printStackTrace();
		}
	}
	
	private boolean launchTask(ExecutedTask task)
	{
		final ThreadPoolManager scheduler = ThreadPoolManager.getInstance();
		final TaskTypes type = task.getType();
		
		if (type == TYPE_STARTUP)
		{
			task.run();
			return false;
		}
		else if (type == TYPE_SHEDULED)
		{
			final long delay = Long.valueOf(task.getParams()[0]);
			task._scheduled = scheduler.scheduleGeneral(task, delay);
			return true;
		}
		else if (type == TYPE_FIXED_SHEDULED)
		{
			final long delay = Long.valueOf(task.getParams()[0]);
			final long interval = Long.valueOf(task.getParams()[1]);
			
			task._scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
			return true;
		}
		else if (type == TYPE_TIME)
		{
			try
			{
				final Date desired = DateFormat.getInstance().parse(task.getParams()[0]);
				final long diff = desired.getTime() - System.currentTimeMillis();
				if (diff >= 0)
				{
					task._scheduled = scheduler.scheduleGeneral(task, diff);
					return true;
				}
				_log.info("Task " + task.getId() + " is obsoleted.");
			}
			catch (final Exception e)
			{
			}
		}
		else if (type == TYPE_SPECIAL)
		{
			final ScheduledFuture<?> result = task.getTask().launchSpecial(task);
			if (result != null)
			{
				task._scheduled = result;
				return true;
			}
		}
		else if (type == TYPE_GLOBAL_TASK)
		{
			final long interval = Long.valueOf(task.getParams()[0]) * 86400000L;
			final String[] hour = task.getParams()[1].split(":");
			
			if (hour.length != 3)
			{
				_log.warning("Task " + task.getId() + " has incorrect parameters");
				return false;
			}
			
			final Calendar check = Calendar.getInstance();
			check.setTimeInMillis(task.getLastActivation() + interval);
			
			final Calendar min = Calendar.getInstance();
			try
			{
				min.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour[0]));
				min.set(Calendar.MINUTE, Integer.valueOf(hour[1]));
				min.set(Calendar.SECOND, Integer.valueOf(hour[2]));
			}
			catch (final Exception e)
			{
				_log.warning("Bad parameter on task " + task.getId() + ": " + e.getMessage());
				return false;
			}
			
			long delay = min.getTimeInMillis() - System.currentTimeMillis();
			
			if (check.after(min) || (delay < 0))
			{
				delay += interval;
			}
			
			task._scheduled = scheduler.scheduleGeneralAtFixedRate(task, delay, interval);
			
			return true;
		}
		
		return false;
	}
	
	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addUniqueTask(task, type, param1, param2, param3, 0);
	}
	
	public static boolean addUniqueTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[2]))
		{
			statement.setString(1, task);
			try (ResultSet rset = statement.executeQuery())
			{
				if (!rset.next())
				{
					try (PreparedStatement statement2 = con.prepareStatement(SQL_STATEMENTS[3]))
					{
						statement2.setString(1, task);
						statement2.setString(2, type.toString());
						statement2.setLong(3, lastActivation);
						statement2.setString(4, param1);
						statement2.setString(5, param2);
						statement2.setString(6, param3);
						statement2.execute();
					}
				}
			}
		}
		catch (final SQLException e)
		{
			_log.warning("cannot add the unique task: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3)
	{
		return addTask(task, type, param1, param2, param3, 0);
	}
	
	public static boolean addTask(String task, TaskTypes type, String param1, String param2, String param3, long lastActivation)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SQL_STATEMENTS[3]))
		{
			statement.setString(1, task);
			statement.setString(2, type.toString());
			statement.setLong(3, lastActivation);
			statement.setString(4, param1);
			statement.setString(5, param2);
			statement.setString(6, param3);
			statement.execute();
		}
		catch (final SQLException e)
		{
			_log.warning("cannot add the task:  " + e.getMessage());
			return false;
		}
		return true;
	}
}