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
package org.l2jmobius.gameserver.model.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author HorridoJoho
 */
public class TvTEventTeam
{
	/**
	 * The name of the team
	 */
	private final String _name;
	/**
	 * The team spot coordinated
	 */
	private int[] _coordinates = new int[3];
	/**
	 * The points of the team
	 */
	private short _points;
	/** Name and instance of all participated players in map. */
	private final Map<Integer, PlayerInstance> _participatedPlayers = new ConcurrentHashMap<>();
	
	/**
	 * C'tor initialize the team
	 * @param name as String
	 * @param coordinates as int[]
	 */
	public TvTEventTeam(String name, int[] coordinates)
	{
		_name = name;
		_coordinates = coordinates;
		_points = 0;
	}
	
	/**
	 * Adds a player to the team
	 * @param playerInstance as PlayerInstance
	 * @return boolean: true if success, otherwise false
	 */
	public boolean addPlayer(PlayerInstance playerInstance)
	{
		if (playerInstance == null)
		{
			return false;
		}
		
		_participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		
		return true;
	}
	
	/**
	 * Removes a player from the team
	 * @param playerObjectId
	 */
	public void removePlayer(int playerObjectId)
	{
		_participatedPlayers.remove(playerObjectId);
	}
	
	/**
	 * Increases the points of the team
	 */
	public void increasePoints()
	{
		++_points;
	}
	
	/**
	 * Cleanup the team and make it ready for adding players again
	 */
	public void cleanMe()
	{
		_participatedPlayers.clear();
		_points = 0;
	}
	
	/**
	 * Is given player in this team?
	 * @param playerObjectId
	 * @return boolean: true if player is in this team, otherwise false
	 */
	public boolean containsPlayer(int playerObjectId)
	{
		return _participatedPlayers.containsKey(playerObjectId);
	}
	
	/**
	 * Returns the name of the team
	 * @return String: name of the team
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * Returns the coordinates of the team spot
	 * @return int[]: team coordinates
	 */
	public int[] getCoordinates()
	{
		return _coordinates;
	}
	
	/**
	 * Returns the points of the team
	 * @return short: team points
	 */
	public short getPoints()
	{
		return _points;
	}
	
	/**
	 * Returns name and instance of all participated players in Map
	 * @return Map<String, PlayerInstance>: map of players in this team
	 */
	public Map<Integer, PlayerInstance> getParticipatedPlayers()
	{
		return _participatedPlayers;
	}
	
	/**
	 * Returns player count of this team
	 * @return int: number of players in team
	 */
	public int getParticipatedPlayerCount()
	{
		return _participatedPlayers.size();
	}
}
