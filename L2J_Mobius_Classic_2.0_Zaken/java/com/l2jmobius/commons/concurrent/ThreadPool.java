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
package com.l2jmobius.commons.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.l2jmobius.Config;

/**
 * @author _dev_ (savormix)
 * @author NB4L1
 */
public final class ThreadPool
{
	private static final Logger LOGGER = Logger.getLogger(ThreadPool.class.getName());
	
	private static ScheduledThreadPoolExecutor SCHEDULED_THREAD_POOL_EXECUTOR;
	private static ThreadPoolExecutor THREAD_POOL_EXECUTOR;
	
	public static void init() throws Exception
	{
		if ((SCHEDULED_THREAD_POOL_EXECUTOR != null) || (THREAD_POOL_EXECUTOR != null))
		{
			throw new Exception("The thread pool has been already initialized!");
		}
		
		SCHEDULED_THREAD_POOL_EXECUTOR = new ScheduledThreadPoolExecutor(Config.SCHEDULED_THREAD_POOL_COUNT != -1 ? Config.SCHEDULED_THREAD_POOL_COUNT : Runtime.getRuntime().availableProcessors() * Config.THREADS_PER_SCHEDULED_THREAD_POOL, new PoolThreadFactory("L2JM-S-", Thread.NORM_PRIORITY));
		final int instantPoolCount = Config.INSTANT_THREAD_POOL_COUNT != -1 ? Config.INSTANT_THREAD_POOL_COUNT : Runtime.getRuntime().availableProcessors() * Config.THREADS_PER_INSTANT_THREAD_POOL;
		THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(instantPoolCount, instantPoolCount, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), new PoolThreadFactory("L2JM-I-", Thread.NORM_PRIORITY));
		
		getThreadPools().forEach(tp ->
		{
			tp.setRejectedExecutionHandler(new RejectedExecutionHandlerImpl());
			tp.prestartAllCoreThreads();
		});
		
		scheduleAtFixedRate(ThreadPool::purge, 60000, 60000); // Repeats every minute.
		
		LOGGER.info("ThreadPool: Initialized with");
		LOGGER.info("..." + SCHEDULED_THREAD_POOL_EXECUTOR.getPoolSize() + "/" + SCHEDULED_THREAD_POOL_EXECUTOR.getPoolSize() + " scheduled thread(s)."); // ScheduledThreadPoolExecutor has a fixed number of threads and maximumPoolSize has no effect
		LOGGER.info("..." + THREAD_POOL_EXECUTOR.getPoolSize() + "/" + THREAD_POOL_EXECUTOR.getMaximumPoolSize() + " thread(s).");
	}
	
	/**
	 * Gets the scheduled thread pool executor.
	 * @return the scheduled thread pool executor
	 */
	public static ScheduledThreadPoolExecutor getScheduledThreadPoolExecutor()
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR;
	}
	
	/**
	 * Gets the thread pool executor.
	 * @return the thread pool executor
	 */
	public static ThreadPoolExecutor getThreadPoolExecutor()
	{
		return THREAD_POOL_EXECUTOR;
	}
	
	/**
	 * Gets a stream of all the thread pools.
	 * @return the stream of all the thread pools
	 */
	public static Stream<ThreadPoolExecutor> getThreadPools()
	{
		return Stream.of(SCHEDULED_THREAD_POOL_EXECUTOR, THREAD_POOL_EXECUTOR);
	}
	
	/**
	 * Schedules a task to be executed after the given delay in milliseconds.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public static ScheduledFuture<?> schedule(Runnable task, long delay)
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR.schedule(new RunnableWrapper(task), delay, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules a task to be executed after the given delay at fixed rate in milliseconds.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param period the period in the given time unit
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long delay, long period)
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR.scheduleAtFixedRate(new RunnableWrapper(task), delay, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Schedules a task to be executed after the given delay with fixed delay in milliseconds.
	 * @param task the task to execute
	 * @param delay the delay in the given time unit
	 * @param period the period in the given time unit
	 * @return a ScheduledFuture representing pending completion of the task, and whose get() method will throw an exception upon cancellation
	 */
	public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long delay, long period)
	{
		return SCHEDULED_THREAD_POOL_EXECUTOR.scheduleWithFixedDelay(new RunnableWrapper(task), delay, period, TimeUnit.MILLISECONDS);
	}
	
	/**
	 * Executes the given task sometime in the future.
	 * @param task the task to execute
	 */
	public static void execute(Runnable task)
	{
		THREAD_POOL_EXECUTOR.execute(new RunnableWrapper(task));
	}
	
	/**
	 * Submits a Runnable task for execution and returns a Future representing that task. The Future's get method will return null upon successful completion.
	 * @param task the task to submit
	 * @return a Future representing pending completion of the task
	 */
	public static Future<?> submit(Runnable task)
	{
		return THREAD_POOL_EXECUTOR.submit(new RunnableWrapper(task));
	}
	
	/**
	 * Purges all thread pools.
	 */
	public static void purge()
	{
		getThreadPools().forEach(ThreadPoolExecutor::purge);
	}
	
	/**
	 * Gets the thread pools stats.
	 * @return the stats
	 */
	public static List<String> getStats()
	{
		final List<String> list = new ArrayList<>(23);
		list.add("");
		list.add("Scheduled pool:");
		list.add("=================================================");
		list.add("getActiveCount: ...... " + SCHEDULED_THREAD_POOL_EXECUTOR.getActiveCount());
		list.add("getCorePoolSize: ..... " + SCHEDULED_THREAD_POOL_EXECUTOR.getCorePoolSize());
		list.add("getPoolSize: ......... " + SCHEDULED_THREAD_POOL_EXECUTOR.getPoolSize());
		list.add("getLargestPoolSize: .. " + SCHEDULED_THREAD_POOL_EXECUTOR.getLargestPoolSize());
		list.add("getMaximumPoolSize: .. " + SCHEDULED_THREAD_POOL_EXECUTOR.getCorePoolSize()); // ScheduledThreadPoolExecutor has a fixed number of threads and maximumPoolSize has no effect
		list.add("getCompletedTaskCount: " + SCHEDULED_THREAD_POOL_EXECUTOR.getCompletedTaskCount());
		list.add("getQueuedTaskCount: .. " + SCHEDULED_THREAD_POOL_EXECUTOR.getQueue().size());
		list.add("getTaskCount: ........ " + SCHEDULED_THREAD_POOL_EXECUTOR.getTaskCount());
		list.add("");
		list.add("Thread pool:");
		list.add("=================================================");
		list.add("getActiveCount: ...... " + THREAD_POOL_EXECUTOR.getActiveCount());
		list.add("getCorePoolSize: ..... " + THREAD_POOL_EXECUTOR.getCorePoolSize());
		list.add("getPoolSize: ......... " + THREAD_POOL_EXECUTOR.getPoolSize());
		list.add("getLargestPoolSize: .. " + THREAD_POOL_EXECUTOR.getLargestPoolSize());
		list.add("getMaximumPoolSize: .. " + THREAD_POOL_EXECUTOR.getMaximumPoolSize());
		list.add("getCompletedTaskCount: " + THREAD_POOL_EXECUTOR.getCompletedTaskCount());
		list.add("getQueuedTaskCount: .. " + THREAD_POOL_EXECUTOR.getQueue().size());
		list.add("getTaskCount: ........ " + THREAD_POOL_EXECUTOR.getTaskCount());
		list.add("");
		return list;
	}
	
	/**
	 * Shutdowns the thread pools waiting for tasks to finish.
	 */
	public static void shutdown()
	{
		if ((SCHEDULED_THREAD_POOL_EXECUTOR == null) && (THREAD_POOL_EXECUTOR == null))
		{
			return;
		}
		
		final long startTime = System.currentTimeMillis();
		
		LOGGER.info("ThreadPool: Shutting down.");
		LOGGER.info("...executing " + SCHEDULED_THREAD_POOL_EXECUTOR.getQueue().size() + " scheduled tasks.");
		LOGGER.info("...executing " + THREAD_POOL_EXECUTOR.getQueue().size() + " tasks.");
		
		getThreadPools().forEach(tp ->
		{
			try
			{
				tp.shutdown();
			}
			catch (Throwable t)
			{
				LOGGER.warning("" + t);
			}
		});
		
		getThreadPools().forEach(t ->
		{
			try
			{
				t.awaitTermination(15, TimeUnit.SECONDS);
			}
			catch (InterruptedException e)
			{
				LOGGER.warning("" + e);
			}
		});
		
		if (!SCHEDULED_THREAD_POOL_EXECUTOR.isTerminated())
		{
			SCHEDULED_THREAD_POOL_EXECUTOR.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			SCHEDULED_THREAD_POOL_EXECUTOR.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
			try
			{
				SCHEDULED_THREAD_POOL_EXECUTOR.awaitTermination(5, TimeUnit.SECONDS);
			}
			catch (Throwable t)
			{
				LOGGER.warning("" + t);
			}
		}
		
		LOGGER.info("...success: " + getThreadPools().allMatch(ThreadPoolExecutor::isTerminated) + " in " + (System.currentTimeMillis() - startTime) + " ms.");
		LOGGER.info("..." + SCHEDULED_THREAD_POOL_EXECUTOR.getQueue().size() + " scheduled tasks left.");
		LOGGER.info("..." + THREAD_POOL_EXECUTOR.getQueue().size() + " tasks left.");
	}
	
	private static final class PoolThreadFactory implements ThreadFactory
	{
		private final String _prefix;
		private final int _priority;
		private final AtomicInteger _threadId = new AtomicInteger();
		
		public PoolThreadFactory(String prefix, int priority)
		{
			_prefix = prefix;
			_priority = priority;
		}
		
		@Override
		public Thread newThread(Runnable r)
		{
			final Thread thread = new Thread(r, _prefix + _threadId.incrementAndGet());
			thread.setPriority(_priority);
			return thread;
		}
	}
}
