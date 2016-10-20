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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.logging.Level;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.stat.PcStat;
import com.l2jmobius.gameserver.taskmanager.Task;
import com.l2jmobius.gameserver.taskmanager.TaskManager;
import com.l2jmobius.gameserver.taskmanager.TaskManager.ExecutedTask;
import com.l2jmobius.gameserver.taskmanager.TaskTypes;

/**
 * @author UnAfraid
 */
public class TaskVitalityReset extends Task
{
	private static final String NAME = "vitalityreset";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Config.ALT_VITALITY_DATE_RESET)
		{
			return;
		}
		
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			player.setVitalityPoints(PcStat.MAX_VITALITY_POINTS, false);
		}
		
		try (Connection con = DatabaseFactory.getInstance().getConnection();
			PreparedStatement st = con.prepareStatement("DELETE FROM account_gsdata WHERE var = ?"))
		{
			st.setString(1, PcStat.VITALITY_VARIABLE);
			st.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "", e);
		}
		_log.info(getClass().getSimpleName() + ": launched.");
	}
	
	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", Config.ALT_VITALITY_HOUR_RESET, "");
	}
}
