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
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * An olympiad stadium
 * @author durgus
 */
public class L2OlympiadStadiumZone extends L2ZoneType
{
	private int _stadiumId;
	
	public L2OlympiadStadiumZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("stadiumId"))
		{
			_stadiumId = Integer.parseInt(value);
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
		character.setInsideZone(L2Character.ZONE_NOLANDING, true);
		
		L2PcInstance player = null;
		if (character instanceof L2PcInstance)
		{
			player = (L2PcInstance) character;
			player.sendPacket(new SystemMessage(SystemMessage.ENTERED_COMBAT_ZONE));
		}
		else if (character instanceof L2Summon)
		{
			player = ((L2Summon) character).getOwner();
		}
		
		if (player != null)
		{
			if (!player.isGM() && !player.isInOlympiadMode() && !player.inObserverMode())
			{
				player.teleToLocation(MapRegionTable.TeleportWhereType.Town);
			}
			else
			{
				if (player.isMounted())
				{
					player.dismount();
				}
			}
		}
		
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		character.setInsideZone(L2Character.ZONE_PVP, false);
		character.setInsideZone(L2Character.ZONE_NOLANDING, false);
		
		if (character instanceof L2PcInstance)
		{
			((L2PcInstance) character).sendPacket(new SystemMessage(SystemMessage.LEFT_COMBAT_ZONE));
		}
	}
	
	/**
	 * Returns this zones stadium id (if any)
	 * @return
	 */
	public int getStadiumId()
	{
		return _stadiumId;
	}
}