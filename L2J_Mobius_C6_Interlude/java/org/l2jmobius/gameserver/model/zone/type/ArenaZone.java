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
package org.l2jmobius.gameserver.model.zone.type;

import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * An arena
 * @author durgus
 */
public class ArenaZone extends ZoneType
{
	private final Location _spawnLoc = new Location(0, 0, 0);
	
	public ArenaZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "spawnX":
			{
				_spawnLoc.setX(Integer.parseInt(value));
				break;
			}
			case "spawnY":
			{
				_spawnLoc.setY(Integer.parseInt(value));
				break;
			}
			case "spawnZ":
			{
				_spawnLoc.setZ(Integer.parseInt(value));
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
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, true);
		
		if (creature instanceof PlayerInstance)
		{
			((PlayerInstance) creature).sendPacket(SystemMessageId.ENTERED_COMBAT_ZONE);
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, false);
		
		if (creature instanceof PlayerInstance)
		{
			((PlayerInstance) creature).sendPacket(SystemMessageId.LEFT_COMBAT_ZONE);
		}
	}
	
	@Override
	protected void onDieInside(Creature creature)
	{
	}
	
	@Override
	protected void onReviveInside(Creature creature)
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
		
		for (Creature creature : _characterList.values())
		{
			if (creature == null)
			{
				continue;
			}
			
			if (creature instanceof PlayerInstance)
			{
				final PlayerInstance player = (PlayerInstance) creature;
				
				if (player.isOnline() == 1)
				{
					player.teleToLocation(TeleportWhereType.TOWN);
				}
			}
		}
	}
	
	public Location getSpawnLoc()
	{
		return _spawnLoc;
	}
}
