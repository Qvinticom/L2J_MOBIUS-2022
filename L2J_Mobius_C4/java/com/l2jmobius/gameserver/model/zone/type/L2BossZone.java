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
package com.l2jmobius.gameserver.model.zone.type;

import com.l2jmobius.gameserver.GameServer;
import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;

import javolution.util.FastList;
import javolution.util.FastMap;

/**
 * @author DaRkRaGe
 */
public class L2BossZone extends L2ZoneType
{
	private String _zoneName;
	private int _timeInvade;
	private boolean _enabled = true; // default value, unless overriden by xml...
	
	// track the times that players got disconnected. Players are allowed
	// to log back into the zone as long as their log-out was within _timeInvade
	// time...
	// <player objectId, expiration time in milliseconds>
	private final FastMap<Integer, Long> _playerAllowedReEntryTimes;
	
	private int[] _oustLoc =
	{
		0,
		0,
		0
	};
	
	// track the players admitted to the zone who should be allowed back in
	// after reboot/server downtime (outside of their control), within 30
	// of server restart
	private FastList<Integer> _playersAllowed;
	
	public L2BossZone(int id)
	{
		super(id);
		_playerAllowedReEntryTimes = new FastMap<>();
		_playersAllowed = new FastList<>();
		_oustLoc = new int[3];
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_zoneName = value;
		}
		else if (name.equals("InvadeTime"))
		{
			_timeInvade = Integer.parseInt(value);
		}
		else if (name.equals("EnabledByDefault"))
		{
			_enabled = Boolean.parseBoolean(value);
		}
		else if (name.equals("oustX"))
		{
			_oustLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("oustY"))
		{
			_oustLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("oustZ"))
		{
			_oustLoc[2] = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_BOSS, true);
		
		if (!_enabled)
		{
			return;
		}
		
		L2PcInstance player = null;
		if (character instanceof L2PcInstance)
		{
			player = (L2PcInstance) character;
		}
		else if (character instanceof L2Summon)
		{
			player = ((L2Summon) character).getOwner();
		}
		
		if (player != null)
		{
			if (player.isGM())
			{
				player.sendMessage("You entered " + _zoneName);
				return;
			}
			
			// if player has been (previously) cleared by npc/ai for entry and the zone is
			// set to receive players (aka not waiting for boss to respawn)
			if (_playersAllowed.contains(player.getObjectId()))
			{
				// Get the information about this player's last logout-exit from
				// this zone.
				final Long expirationTime = _playerAllowedReEntryTimes.get(player.getObjectId());
				
				// with legal entries, do nothing.
				if (expirationTime == null) // legal null expirationTime entries
				{
					final long serverStartTime = GameServer.DateTimeServerStarted.getTimeInMillis();
					if ((serverStartTime > (System.currentTimeMillis() - _timeInvade)))
					{
						return;
					}
				}
				else
				{
					// legal non-null logoutTime entries
					_playerAllowedReEntryTimes.remove(player.getObjectId());
					if (expirationTime.longValue() > System.currentTimeMillis())
					{
						return;
					}
				}
				
				_playersAllowed.remove(_playersAllowed.indexOf(player.getObjectId()));
			}
			
			// teleport out all players who attempt "illegal" (re-)entry
			if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
			{
				player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
			}
			else
			{
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_BOSS, false);
		
		if (!_enabled)
		{
			return;
		}
		
		L2PcInstance player = null;
		if (character instanceof L2PcInstance)
		{
			player = (L2PcInstance) character;
		}
		else if (character instanceof L2Summon)
		{
			player = ((L2Summon) character).getOwner();
		}
		
		if (player != null)
		{
			if (player.isGM())
			{
				player.sendMessage("You left " + _zoneName);
				return;
			}
			
			// if the player just got disconnected/logged out, store the dc
			// time so that
			// decisions can be made later about allowing or not the player
			// to log into the zone
			if ((player.isOnline() == 0) && _playersAllowed.contains(player.getObjectId()))
			{
				// mark the time that the player left the zone
				_playerAllowedReEntryTimes.put(player.getObjectId(), System.currentTimeMillis() + _timeInvade);
			}
		}
	}
	
	public String getZoneName()
	{
		return _zoneName;
	}
	
	public int getTimeInvade()
	{
		return _timeInvade;
	}
	
	public void setAllowedPlayers(FastList<Integer> players)
	{
		if (players != null)
		{
			_playersAllowed = players;
		}
	}
	
	public FastList<Integer> getAllowedPlayers()
	{
		return _playersAllowed;
	}
	
	public boolean checkIfPlayerAllowed(L2PcInstance player)
	{
		if (player.isGM())
		{
			return true;
		}
		
		if (_playersAllowed.contains(player.getObjectId()))
		{
			return true;
		}
		
		if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
		{
			player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
		}
		else
		{
			player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
		
		return false;
	}
	
	/**
	 * Some GrandBosses send all players in zone to a specific part of the zone, rather than just removing them all. If this is the case, this command should be used. If this is no the case, then use oustAllPlayers().
	 * @param x
	 * @param y
	 * @param z
	 */
	public void movePlayersTo(int x, int y, int z)
	{
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (final L2Character character : _characterList.values())
		{
			if (character instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline() == 1)
				{
					player.teleToLocation(x, y, z, true);
				}
			}
		}
	}
	
	/**
	 * Occasionally, all players need to be sent out of the zone (for example, if the players are just running around without fighting for too long, or if all players die, etc). This call sends all online players to town and marks offline players to be teleported (by clearing their relog expiration
	 * times) when they log back in (no real need for off-line teleport).
	 */
	public void oustAllPlayers()
	{
		if (_characterList == null)
		{
			return;
		}
		
		if (_characterList.isEmpty())
		{
			return;
		}
		
		for (final L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}
			
			if (character instanceof L2PcInstance)
			{
				final L2PcInstance player = (L2PcInstance) character;
				if (player.isOnline() == 1)
				{
					if ((_oustLoc[0] != 0) && (_oustLoc[1] != 0) && (_oustLoc[2] != 0))
					{
						player.teleToLocation(_oustLoc[0], _oustLoc[1], _oustLoc[2]);
					}
					else
					{
						player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
					}
				}
			}
		}
		_playerAllowedReEntryTimes.clear();
		_playersAllowed.clear();
	}
	
	/**
	 * This function is to be used by external sources, such as quests and AI in order to allow a player for entry into the zone for some time. Naturally if the player does not enter within the allowed time, he/she will be teleported out again...
	 * @param player : reference to the player we wish to allow
	 * @param duration : amount of time in seconds during which entry is valid.
	 */
	public void allowPlayerEntry(L2PcInstance player, int duration)
	{
		if (!player.isGM())
		{
			allowPlayerEntry(player.getObjectId(), duration);
		}
	}
	
	public void allowPlayerEntry(int objectId, int duration)
	{
		_playersAllowed.add(objectId);
		
		if (duration > 0)
		{
			_playerAllowedReEntryTimes.put(objectId, System.currentTimeMillis() + duration);
		}
	}
	
	public void setZoneEnabled(boolean flag)
	{
		if (_enabled != flag)
		{
			oustAllPlayers();
		}
		_enabled = flag;
	}
}