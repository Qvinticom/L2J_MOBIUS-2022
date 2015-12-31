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
package com.l2jmobius.gameserver.model.holders;

import java.util.List;
import java.util.Map;

/**
 * @author Mobius
 */
public class DailyMissionHolder
{
	private final int _id;
	private final int _clientId;
	private final String _type;
	private final int _level;
	private final List<Integer> _classes;
	private final Map<Integer, Integer> _rewards;
	
	public DailyMissionHolder(int id, int clientId, String type, int level, List<Integer> classes, Map<Integer, Integer> rewards)
	{
		_id = id;
		_clientId = clientId;
		_type = type;
		_level = level;
		_classes = classes;
		_rewards = rewards;
	}
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the clientId
	 */
	public int getClientId()
	{
		return _clientId;
	}
	
	/**
	 * @return the type
	 */
	public String getType()
	{
		return _type;
	}
	
	/**
	 * @return the level
	 */
	public int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return the classes
	 */
	public List<Integer> getAvailableClasses()
	{
		return _classes;
	}
	
	/**
	 * @return the rewards
	 */
	public Map<Integer, Integer> getRewards()
	{
		return _rewards;
	}
}
