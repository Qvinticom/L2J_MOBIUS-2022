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

import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.TvTEvent;
import com.l2jmobius.gameserver.model.zone.L2ZoneSpawn;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * An arena
 * @author durgus
 */
public class L2ArenaZone extends L2ZoneSpawn
{
	@SuppressWarnings("unused")
	private String _arenaName;
	
	public L2ArenaZone(int id)
	{
		super(id);
		
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_arenaName = value;
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, true);
		
		if (character instanceof L2PcInstance)
		{
			final L2PcInstance player = (L2PcInstance) character;
			
			// Coliseum TvT Event restrictions
			if ((getId() == 11012) && (TvTEvent.getEventState() == TvTEvent.STARTED))
			{
				if ((player.getEventTeam() == 0) && !player.isGM())
				{
					player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessage.ENTERED_COMBAT_ZONE));
			}
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, false);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessage.LEFT_COMBAT_ZONE));
		}
	}
}