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

import org.python.util.PythonInterpreter;

import com.l2jmobius.gameserver.taskmanager.Task;
import com.l2jmobius.gameserver.taskmanager.TaskManager.ExecutedTask;

/**
 * @author Layane
 */
public class TaskJython extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskJython.class.getName());
	public static final String NAME = "jython";
	
	private final PythonInterpreter _python = new PythonInterpreter();
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		_python.cleanup();
		_python.exec("import sys");
		_python.execfile("data/scripts/cron/" + task.getParams()[2]);
		LOGGER.info("[GlobalTask] Python Cleanup launched.");
	}
}