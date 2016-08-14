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
package handlers.telnethandlers.server;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.ThreadPoolManager.RunnableWrapper;
import com.l2jmobius.gameserver.network.telnet.ITelnetCommand;
import com.l2jmobius.gameserver.network.telnet.TelnetServer;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author UnAfraid
 */
public class ThreadPoolDebug implements ITelnetCommand
{
	private static final Logger LOGGER = Logger.getLogger(ThreadPoolDebug.class.getName());
	
	@Override
	public String getCommand()
	{
		return "threadpooldebug";
	}
	
	@Override
	public String getUsage()
	{
		return "threadpooldebug [effect, general, ai, events]";
	}
	
	@Override
	public String handle(ChannelHandlerContext ctx, String[] args)
	{
		String pool = "_generalScheduledThreadPool";
		if (args.length > 0)
		{
			switch (args[0])
			{
				case "effect":
				{
					pool = "_effectsScheduledThreadPool";
					break;
				}
				case "general":
				{
					pool = "_generalScheduledThreadPool";
					break;
				}
				case "ai":
				{
					pool = "_aiScheduledThreadPool";
					break;
				}
				case "events":
				{
					pool = "_eventScheduledThreadPool";
					break;
				}
				default:
				{
					return args[0] + " is not implemented!";
				}
			}
		}
		final ScheduledThreadPoolExecutor executor = getObject(ThreadPoolManager.class, ThreadPoolManager.getInstance(), pool, ScheduledThreadPoolExecutor.class);
		if (executor == null)
		{
			return "Couldn't retreive " + pool + "!";
		}
		
		Class<?> adapterClass;
		try
		{
			adapterClass = Class.forName("java.util.concurrent.Executors$RunnableAdapter");
		}
		catch (Exception e)
		{
			return e.getMessage();
		}
		
		final Map<String, Integer> tasks = new HashMap<>();
		for (Runnable run : executor.getQueue())
		{
			try
			{
				if (run instanceof FutureTask)
				{
					final Object callableObject = getObject(FutureTask.class, run, "callable", Object.class);
					final Object taskObject = getObject(adapterClass, callableObject, "task", Object.class);
					
					if (taskObject instanceof RunnableWrapper)
					{
						final Runnable task = getObject(RunnableWrapper.class, taskObject, "_r", Runnable.class);
						final String name = task.getClass().getName();
						final int times = tasks.containsKey(name) ? tasks.get(name) : 0;
						tasks.put(name, times + 1);
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append(pool + " queue size: " + executor.getQueue().size() + Config.EOL);
		
		tasks.entrySet().stream().sorted(Comparator.comparingInt(Entry::getValue)).forEach(entry -> sb.append("Class: " + entry.getKey() + " = " + entry.getValue() + Config.EOL));
		
		return sb.toString();
	}
	
	private static final <T> T getObject(Class<?> sourceClass, Object sourceInstance, String fieldName, Class<T> targetClass)
	{
		try
		{
			final Field field = sourceClass.getDeclaredField(fieldName);
			
			// Mark down if field was accessible
			final boolean isAccessible = field.isAccessible();
			
			// Enforce accessible to retrieve the object associated with this field
			if (!isAccessible)
			{
				field.setAccessible(true);
			}
			
			// Get the object
			final Object fieldObject = field.get(sourceInstance);
			
			// Restore the original accessible state.
			field.setAccessible(isAccessible);
			
			// Make sure the object is the one we expect to be
			if (targetClass.isInstance(fieldObject))
			{
				return targetClass.cast(fieldObject);
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while retrieving object of " + sourceInstance.getClass().getName() + "." + fieldName, e);
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		TelnetServer.getInstance().addHandler(new ThreadPoolDebug());
	}
}
