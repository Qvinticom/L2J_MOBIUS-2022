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

import com.l2jmobius.gameserver.instancemanager.CastleManager;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Castle;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * A castle teleporter zone used for Mass Gatekeepers
 * @author Kerberos
 */
public class L2CastleTeleportZone extends L2ZoneType
{
	private final int[] _spawnLoc;
	private int _castleId;
	private Castle _castle;
	
	public L2CastleTeleportZone(int id)
	{
		super(id);
		_spawnLoc = new int[5];
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
		{
			_castleId = Integer.parseInt(value);
			
			// Register self to the correct castle
			_castle = CastleManager.getInstance().getCastleById(_castleId);
			_castle.setTeleZone(this);
		}
		else if (name.equals("spawnMinX"))
		{
			_spawnLoc[0] = Integer.parseInt(value);
		}
		else if (name.equals("spawnMaxX"))
		{
			_spawnLoc[1] = Integer.parseInt(value);
		}
		else if (name.equals("spawnMinY"))
		{
			_spawnLoc[2] = Integer.parseInt(value);
		}
		else if (name.equals("spawnMaxY"))
		{
			_spawnLoc[3] = Integer.parseInt(value);
		}
		else if (name.equals("spawnZ"))
		{
			_spawnLoc[4] = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
	}
	
	@Override
	protected void onExit(L2Character character)
	{
	}
	
	/**
	 * Returns all players within this zone
	 * @return
	 */
	public FastList<L2PcInstance> getAllPlayers()
	{
		final FastList<L2PcInstance> players = new FastList<>();
		
		for (final L2Character temp : _characterList.values())
		{
			if (temp instanceof L2PcInstance)
			{
				players.add((L2PcInstance) temp);
			}
		}
		
		return players;
	}
	
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
					player.teleToLocation(Rnd.get(_spawnLoc[0], _spawnLoc[1]), Rnd.get(_spawnLoc[2], _spawnLoc[3]), _spawnLoc[4]);
				}
			}
		}
	}
	
	/**
	 * Get the spawn locations
	 * @return
	 */
	public int[] getSpawn()
	{
		return _spawnLoc;
	}
}