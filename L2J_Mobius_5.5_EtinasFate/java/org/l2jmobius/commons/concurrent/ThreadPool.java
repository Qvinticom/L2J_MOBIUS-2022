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
package org.l2jmobius.commons.concurrent;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.l2jmobius.Config;

/**
 * This class handles thread pooling system. It relies on two ThreadPoolExecutor arrays, which poolers number is generated using config.
 * <p>
 * Those arrays hold following pools:
 * </p>
 * <ul>
 * <li>Scheduled pool keeps a track about incoming, future events.</li>
 * <li>Instant pool handles short-life events.</li>
 * </ul>
 */
public final class ThreadPool
{
	private static final Logger LOGGER = Logger.getLogger(ThreadPool.class.getName());
	
	private static ScheduledThreadPoolExecutor[] SCHEDULED_POOLS = new ScheduledThreadPoolExecutor[Config.SCHEDULED_THREAD_POOL_COUNT];
	private static ThreadPoolExecutor[] INSTANT_POOLS = new ThreadPoolExecutor[Config.INSTANT_THREAD_POOL_COUNT];
	private static volatile int SCHEDULED_THREAD_RANDOMIZER = 0;
	private static volatile int INSTANT_THREAD_RANDOMIZER = 0;
	
	public static void init()
	{
		// Feed scheduled pool.
		for (int i = 0; i < Config.SCHEDULED_THREAD_POOL_COUNT; i++)
		{
			SCHEDULED_POOLS[i] = new ScheduledThreadPoolExecutor(Config.THREADS_PER_SCHEDULED_THREAD_POOL);
		}
		
		// Feed instant pool.
		for (int i = 0; i < Config.INSTANT_THREAD_POOL_COUNT; i++)
		{
			INSTANT_POOLS[i] = new ThreadPoolExecutor(Config.THREADS_PER_INSTANT_THREAD_POOL, Config.THREADS_PER_INSTANT_THREAD_POOL, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(100000));
		}
		
		// Prestart core threads.
		for (ScheduledThreadPoolExecutor threadPool : SCHEDULED_POOLS)
		{
			threadPool.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
			threadPool.setRemoveOnCancelPolicy(true);
			threadPool.prestartAllCoreThreads();
		}
		for (ThreadPoolExecutor threadPool : INSTANT_POOLS)
		{
			threadPool.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
			threadPool.prestartAllCoreThreads();
		}
		
		// Launch purge task.
		scheduleAtFixedRate(() ->
		{
			purge();
		}, 600000, 600000);
		
		LOGGER.info("ThreadPool: Initialized");
		LOGGER.info("..." + Config.SCHEDULED_THREAD_POOL_COUNT + " scheduled pool executors with " + (Config.SCHEDULED_THREAD_POOL_COUNT * Config.THREADS_PER_SCHEDULED_THREAD_POOL) + " total threads.");
		LOGGER.info("..." + Config.INSTANT_THREAD_POOL_COUNT + " instant pool executors with " + (Config.INSTANT_THREAD_POOL_COUNT * Config.THREADS_PER_INSTANT_THREAD_POOL) + " total threads.");
	}
	
	public static void purge()
	{
		for (ScheduledThreadPoolExecutor threadPool : SCHEDULED_POOLS)
		{
			threadPool.purge();
		}
		for (ThreadPoolExecutor threadPool : INSTANT_POOLS)
		{
			threadPool.purge();
		}
	}
	
	/**
	 * Creates and executes a one-shot action that becomes enabled after the given delay.
	 * @param runnable : the task to execute.
	 * @param delay : the time from now to delay execution.
	 * @return a ScheduledFuture representing pending completion of the task and whose get() method will return null upon completion.
	 */
	public static ScheduledFuture<?> schedule(Runnable runnable, long delay)
	{
		try
		{
			return SCHEDULED_POOLS[SCHEDULED_THREAD_RANDOMIZER++ % Config.SCHEDULED_THREAD_POOL_COUNT].schedule(new RunnableWrapper(runnable), delay, TimeUnit.MILLISECONDS);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Creates and executes a periodic action that becomes enabled first after the given initial delay.
	 * @param runnable : the task to execute.
	 * @param initialDelay : the time to delay first execution.
	 * @param period : the period between successive executions.
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation.
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long initialDelay, long period)
	{
		try
		{
			return SCHEDULED_POOLS[SCHEDULED_THREAD_RANDOMIZER++ % Config.SCHEDULED_THREAD_POOL_COUNT].scheduleAtFixedRate(new RunnableWrapper(runnable), initialDelay, period, TimeUnit.MILLISECONDS);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Executes the given task sometime in the future.
	 * @param runnable : the task to execute.
	 */
	public static void execute(Runnable runnable)
	{
		try
		{
			INSTANT_POOLS[INSTANT_THREAD_RANDOMIZER++ % Config.INSTANT_THREAD_POOL_COUNT].execute(new RunnableWrapper(runnable));
		}
		catch (Exception e)
		{
		}
	}
	
	public static String[] getStats()
	{
		final String[] stats = new String[(SCHEDULED_POOLS.length + INSTANT_POOLS.length) * 10];
		int pos = 0;
		for (int i = 0; i < SCHEDULED_POOLS.length; i++)
		{
			final ScheduledThreadPoolExecutor threadPool = SCHEDULED_POOLS[i];
			stats[pos++] = "Scheduled pool #" + i + ":";
			stats[pos++] = " |- ActiveCount: ...... " + threadPool.getActiveCount();
			stats[pos++] = " |- CorePoolSize: ..... " + threadPool.getCorePoolSize();
			stats[pos++] = " |- PoolSize: ......... " + threadPool.getPoolSize();
			stats[pos++] = " |- LargestPoolSize: .. " + threadPool.getLargestPoolSize();
			stats[pos++] = " |- MaximumPoolSize: .. " + threadPool.getMaximumPoolSize();
			stats[pos++] = " |- CompletedTaskCount: " + threadPool.getCompletedTaskCount();
			stats[pos++] = " |- QueuedTaskCount: .. " + threadPool.getQueue().size();
			stats[pos++] = " |- TaskCount: ........ " + threadPool.getTaskCount();
			stats[pos++] = " | -------";
		}
		for (int i = 0; i < INSTANT_POOLS.length; i++)
		{
			final ThreadPoolExecutor threadPool = INSTANT_POOLS[i];
			stats[pos++] = "Instant pool #" + i + ":";
			stats[pos++] = " |- ActiveCount: ...... " + threadPool.getActiveCount();
			stats[pos++] = " |- CorePoolSize: ..... " + threadPool.getCorePoolSize();
			stats[pos++] = " |- PoolSize: ......... " + threadPool.getPoolSize();
			stats[pos++] = " |- LargestPoolSize: .. " + threadPool.getLargestPoolSize();
			stats[pos++] = " |- MaximumPoolSize: .. " + threadPool.getMaximumPoolSize();
			stats[pos++] = " |- CompletedTaskCount: " + threadPool.getCompletedTaskCount();
			stats[pos++] = " |- QueuedTaskCount: .. " + threadPool.getQueue().size();
			stats[pos++] = " |- TaskCount: ........ " + threadPool.getTaskCount();
			stats[pos++] = " | -------";
		}
		return stats;
	}
	
	/**
	 * Shutdown thread pooling system correctly. Send different informations.
	 */
	public static void shutdown()
	{
		try
		{
			LOGGER.info("ThreadPool: Shutting down.");
			for (ScheduledThreadPoolExecutor threadPool : SCHEDULED_POOLS)
			{
				threadPool.shutdownNow();
			}
			for (ThreadPoolExecutor threadPool : INSTANT_POOLS)
			{
				threadPool.shutdownNow();
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}