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
package com.l2jmobius.gameserver.model.quest;

/**
 * @author Luis Arias Functions in this class are used in python files
 */
public class State
{
	/** Prototype of empty String list */
	private static final String[] emptyStrList = new String[0];
	
	/** Quest object associated to the state */
	private final Quest _quest;
	
	private final String[] _Events = emptyStrList;
	
	/** Name of the quest */
	private final String _Name;
	
	/**
	 * Constructor for the state of the quest.
	 * @param name : String pointing out the name of the quest
	 * @param quest : Quest
	 */
	public State(String name, Quest quest)
	{
		_Name = name;
		_quest = quest;
		quest.addState(this);
	}
	
	/**
	 * Return list of events
	 * @return String[]
	 */
	public String[] getEvents()
	{
		return _Events;
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	public String getName()
	{
		return _Name;
	}
	
	/**
	 * Return name of the quest
	 * @return String
	 */
	@Override
	public String toString()
	{
		return _Name;
	}
	
	public Quest getQuest()
	{
		return _quest;
	}
}