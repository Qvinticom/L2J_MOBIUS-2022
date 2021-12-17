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
package org.l2jmobius.gameserver.taskmanager.tasks;

import java.util.Calendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.instancemanager.RaidBossPointsManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.taskmanager.Task;
import org.l2jmobius.gameserver.taskmanager.TaskManager;
import org.l2jmobius.gameserver.taskmanager.TaskManager.ExecutedTask;
import org.l2jmobius.gameserver.taskmanager.TaskTypes;

public class TaskRaidPointsReset extends Task
{
	private static final Logger LOGGER = Logger.getLogger(TaskRaidPointsReset.class.getName());
	public static final String NAME = "raid_points_reset";
	
	@Override
	public String getName()
	{
		return NAME;
	}
	
	@Override
	public void onTimeElapsed(ExecutedTask task)
	{
		String playerName = "";
		final Calendar cal = Calendar.getInstance();
		if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
		{
			// reward clan reputation points
			final Map<Integer, Integer> rankList = RaidBossPointsManager.getRankList();
			for (Clan c : ClanTable.getInstance().getClans())
			{
				for (Entry<Integer, Integer> entry : rankList.entrySet())
				{
					final WorldObject obj = World.getInstance().findObject(entry.getKey());
					if (obj instanceof Player)
					{
						playerName = ((Player) obj).getName();
					}
					if ((entry.getValue() <= 100) && c.isMember(playerName))
					{
						int reputation = 0;
						switch (entry.getValue())
						{
							case 1:
							{
								reputation = Config.RAID_RANKING_1ST;
								break;
							}
							case 2:
							{
								reputation = Config.RAID_RANKING_2ND;
								break;
							}
							case 3:
							{
								reputation = Config.RAID_RANKING_3RD;
								break;
							}
							case 4:
							{
								reputation = Config.RAID_RANKING_4TH;
								break;
							}
							case 5:
							{
								reputation = Config.RAID_RANKING_5TH;
								break;
							}
							case 6:
							{
								reputation = Config.RAID_RANKING_6TH;
								break;
							}
							case 7:
							{
								reputation = Config.RAID_RANKING_7TH;
								break;
							}
							case 8:
							{
								reputation = Config.RAID_RANKING_8TH;
								break;
							}
							case 9:
							{
								reputation = Config.RAID_RANKING_9TH;
								break;
							}
							case 10:
							{
								reputation = Config.RAID_RANKING_10TH;
								break;
							}
							default:
							{
								if (entry.getValue() <= 50)
								{
									reputation = Config.RAID_RANKING_UP_TO_50TH;
								}
								else
								{
									reputation = Config.RAID_RANKING_UP_TO_100TH;
								}
								break;
							}
						}
						c.setReputationScore(c.getReputationScore() + reputation);
					}
				}
			}
			
			RaidBossPointsManager.cleanUp();
			LOGGER.info("[GlobalTask] Raid Points Reset launched.");
		}
	}
	
	@Override
	public void initializate()
	{
		super.initializate();
		TaskManager.addUniqueTask(NAME, TaskTypes.TYPE_GLOBAL_TASK, "1", "00:10:00", "");
	}
}