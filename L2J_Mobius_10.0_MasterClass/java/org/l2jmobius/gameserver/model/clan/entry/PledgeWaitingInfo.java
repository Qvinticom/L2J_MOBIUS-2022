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
package org.l2jmobius.gameserver.model.clan.entry;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author Sdw
 */
public class PledgeWaitingInfo
{
	private int _playerId;
	private int _playerClassId;
	private int _playerLvl;
	private final int _karma;
	private String _playerName;
	
	public PledgeWaitingInfo(int playerId, int playerLvl, int karma, int classId, String playerName)
	{
		_playerId = playerId;
		_playerClassId = classId;
		_playerLvl = playerLvl;
		_karma = karma;
		_playerName = playerName;
	}
	
	public int getPlayerId()
	{
		return _playerId;
	}
	
	public void setPlayerId(int playerId)
	{
		_playerId = playerId;
	}
	
	public int getPlayerClassId()
	{
		if (isOnline() && (getPlayer().getBaseClass() != _playerClassId))
		{
			_playerClassId = getPlayer().getClassId().getId();
		}
		return _playerClassId;
	}
	
	public int getPlayerLvl()
	{
		if (isOnline() && (getPlayer().getLevel() != _playerLvl))
		{
			_playerLvl = getPlayer().getLevel();
		}
		return _playerLvl;
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	public String getPlayerName()
	{
		if (isOnline() && !getPlayer().getName().equalsIgnoreCase(_playerName))
		{
			_playerName = getPlayer().getName();
		}
		return _playerName;
	}
	
	public Player getPlayer()
	{
		return World.getInstance().getPlayer(_playerId);
	}
	
	public boolean isOnline()
	{
		return (getPlayer() != null) && (getPlayer().isOnlineInt() > 0);
	}
}
