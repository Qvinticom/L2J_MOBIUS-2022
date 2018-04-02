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
package com.l2jmobius.gameserver;

import com.l2jmobius.Config;
import com.l2jmobius.commons.concurrent.IThreadPoolInitializer;

/**
 * @author nos
 */
public final class GameThreadPools implements IThreadPoolInitializer
{
	@Override
	public int getScheduledThreadPoolSize()
	{
		return Config.SCHEDULED_THREAD_POOL_COUNT != -1 ? Config.SCHEDULED_THREAD_POOL_COUNT : Runtime.getRuntime().availableProcessors() * Config.THREADS_PER_SCHEDULED_THREAD_POOL;
	}
	
	@Override
	public int getThreadPoolSize()
	{
		return Config.INSTANT_THREAD_POOL_COUNT != -1 ? Config.INSTANT_THREAD_POOL_COUNT : Runtime.getRuntime().availableProcessors() * Config.THREADS_PER_INSTANT_THREAD_POOL;
	}
}
