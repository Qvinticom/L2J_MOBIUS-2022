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
package com.l2jmobius.gameserver.taskmanager.tasks;

import java.util.logging.Logger;

import com.l2jmobius.gameserver.Shutdown;
import com.l2jmobius.gameserver.taskmanager.Task;
import com.l2jmobius.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public final class TaskRestart extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskRestart.class.getName());
	public static final String NAME = "restart";
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.tasks.Task#getName()
	 */
	@Override
	public String getName()
	{
		return NAME;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.tasks.Task#onTimeElapsed(com.l2jmobius.gameserver.tasks.TaskManager.ExecutedTask)
	 */
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		LOGGER.info("[GlobalTask] Server Restart launched.");
		Shutdown.getInstance().startShutdown(null, Integer.valueOf(task.getParams()[2]), true);
	}
}