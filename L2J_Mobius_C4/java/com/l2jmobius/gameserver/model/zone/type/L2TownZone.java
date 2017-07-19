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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.L2ZoneSpawn;

/**
 * A Town zone
 * @author durgus
 */
public class L2TownZone extends L2ZoneSpawn
{
	private String _townName;
	private int _townId;
	private int _redirectTownId;
	private int _taxById;
	
	public L2TownZone(int id)
	{
		super(id);
		
		_taxById = 0;
		
		// Default to Giran
		_redirectTownId = 9;
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("name"))
		{
			_townName = value;
		}
		else if (name.equals("townId"))
		{
			_townId = Integer.parseInt(value);
		}
		else if (name.equals("redirectTownId"))
		{
			_redirectTownId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			_taxById = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			// PVP possible during siege, now for siege participants only
			// Could also check if this town is in siege, or if any siege is going on
			if ((((L2PcInstance) character).getSiegeState() != 0) && (Config.ZONE_TOWN == 1))
			{
				return;
			}
		}
		
		// Floran is not a peace zone
		if ((getTownId() != 16) && (Config.ZONE_TOWN != 2))
		{
			character.setInsideZone(L2Character.ZONE_PEACE, true);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		// Floran is not a peace zone
		if (getTownId() != 16)
		{
			character.setInsideZone(L2Character.ZONE_PEACE, false);
		}
	}
	
	/**
	 * Returns this town zones name
	 * @return
	 */
	@Deprecated
	public String getName()
	{
		return _townName;
	}
	
	/**
	 * Returns this zones town id (if any)
	 * @return
	 */
	public int getTownId()
	{
		return _townId;
	}
	
	/**
	 * Gets the id for this town zones redir town
	 * @return
	 */
	@Deprecated
	public int getRedirectTownId()
	{
		return _redirectTownId;
	}
	
	/**
	 * Returns this town zones castle id
	 * @return
	 */
	public final int getTaxById()
	{
		return _taxById;
	}
}