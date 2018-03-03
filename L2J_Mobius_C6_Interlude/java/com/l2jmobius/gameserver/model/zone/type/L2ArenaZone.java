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

import com.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.SystemMessageId;

/**
 * An arena
 * @author durgus
 */
public class L2ArenaZone extends L2ZoneType
{
	// private String _arenaName;
	private final int[] _spawnLoc;
	
	public L2ArenaZone(int id)
	{
		super(id);
		
		_spawnLoc = new int[3];
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		// if(name.equals("name"))
		// {
		// _arenaName = value;
		// }
		/* else */
		switch (name)
		{
			case "spawnX":
			{
				_spawnLoc[0] = Integer.parseInt(value);
				break;
			}
			case "spawnY":
			{
				_spawnLoc[1] = Integer.parseInt(value);
				break;
			}
			case "spawnZ":
			{
				_spawnLoc[2] = Integer.parseInt(value);
				break;
			}
			default:
			{
				super.setParameter(name, value);
				break;
			}
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(ZoneId.PVP, true);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(ZoneId.PVP, false);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
		}
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(L2Character character)
	{
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
		
		for (L2Character character : _characterList.values())
		{
			if (character == null)
			{
				continue;
			}
			
			if (character instanceof L2PcInstance)
			{
				L2PcInstance player = (L2PcInstance) character;
				
				if (player.isOnline() == 1)
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
		}
	}
	
	public final int[] getSpawnLoc()
	{
		return _spawnLoc;
	}
}
