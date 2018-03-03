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

import java.util.Map;

import com.l2jmobius.gameserver.datatables.csv.MapRegionTable;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;
import com.l2jmobius.gameserver.model.entity.ClanHall;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.model.zone.ZoneId;
import com.l2jmobius.gameserver.network.serverpackets.ClanHallDecoration;

/**
 * A clan hall zone
 * @author durgus
 */
public class L2ClanHallZone extends L2ZoneType
{
	private int _clanHallId;
	private final int[] _spawnLoc;
	
	public L2ClanHallZone(int id)
	{
		super(id);
		
		_spawnLoc = new int[3];
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "clanHallId":
			{
				_clanHallId = Integer.parseInt(value);
				// Register self to the correct clan hall
				ClanHallManager.getInstance().getClanHallById(_clanHallId).setZone(this);
				break;
			}
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
		if (character instanceof L2PcInstance)
		{
			// Set as in clan hall
			character.setInsideZone(ZoneId.CLANHALL, true);
			
			ClanHall clanHall = ClanHallManager.getInstance().getClanHallById(_clanHallId);
			
			if (clanHall == null)
			{
				return;
			}
			
			// Send decoration packet
			final ClanHallDecoration deco = new ClanHallDecoration(clanHall);
			((L2PcInstance) character).sendPacket(deco);
			
			// Send a message
			if ((clanHall.getOwnerId() != 0) && (clanHall.getOwnerId() == ((L2PcInstance) character).getClanId()))
			{
				((L2PcInstance) character).sendMessage("You have entered your clan hall");
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			// Unset clanhall zone
			character.setInsideZone(ZoneId.CLANHALL, false);
			
			// Send a message
			if ((((L2PcInstance) character).getClanId() != 0) && (ClanHallManager.getInstance().getClanHallById(_clanHallId).getOwnerId() == ((L2PcInstance) character).getClanId()))
			{
				((L2PcInstance) character).sendMessage("You have left your clan hall");
			}
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
	
	/**
	 * Removes all foreigners from the clan hall
	 * @param owningClanId
	 */
	public void banishForeigners(int owningClanId)
	{
		for (L2Character temp : _characterList.values())
		{
			if (!(temp instanceof L2PcInstance))
			{
				continue;
			}
			
			if (((L2PcInstance) temp).getClanId() == owningClanId)
			{
				continue;
			}
			
			((L2PcInstance) temp).teleToLocation(MapRegionTable.TeleportWhereType.Town);
		}
	}
	
	@Override
	public Map<Integer, L2Character> getCharactersInside()
	{
		return _characterList;
	}
	
	/**
	 * Get the clan hall's spawn
	 * @return
	 */
	public Location getSpawn()
	{
		return new Location(_spawnLoc[0], _spawnLoc[1], _spawnLoc[2]);
	}
}
