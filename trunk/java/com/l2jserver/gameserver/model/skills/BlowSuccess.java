/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.model.skills;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.actor.L2Character;

public class BlowSuccess
{
	private static Map<String, Boolean> _success = new HashMap<>();
	
	public static BlowSuccess getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final BlowSuccess _instance = new BlowSuccess();
	}
	
	public void remove(L2Character l2Character, Skill skill)
	{
		_success.remove(makeKey(l2Character, skill));
	}
	
	public boolean get(L2Character l2Character, Skill skill)
	{
		return _success.get(makeKey(l2Character, skill));
	}
	
	public void set(L2Character l2Character, Skill skill, boolean success)
	{
		_success.put(makeKey(l2Character, skill), success);
	}
	
	private String makeKey(L2Character l2Character, Skill skill)
	{
		return "" + l2Character.getObjectId() + ":" + skill.getId();
	}
}
