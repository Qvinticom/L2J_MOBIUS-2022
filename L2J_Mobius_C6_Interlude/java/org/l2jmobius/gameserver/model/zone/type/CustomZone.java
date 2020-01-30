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
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneType;

public class CustomZone extends ZoneType
{
	public CustomZone(int id)
	{
		super(id);
		_isFlyingEnable = true;
	}
	
	@Override
	protected void onDieInside(Creature l2character)
	{
	}
	
	@Override
	protected void onReviveInside(Creature l2character)
	{
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		switch (name)
		{
			case "name":
			{
				_zoneName = value;
				break;
			}
			case "flying":
			{
				_isFlyingEnable = Boolean.parseBoolean(value);
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
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			if (!player.isGM() && player.isFlying() && !player.isInJail() && !_isFlyingEnable)
			{
				player.teleToLocation(TeleportWhereType.TOWN);
			}
			
			if (_zoneName.equalsIgnoreCase("tradeoff"))
			{
				player.sendMessage("Trade restrictions are involved.");
				player.setTradeDisabled(true);
			}
		}
	}
	
	@Override
	protected void onExit(Creature creature)
	{
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			
			if (_zoneName.equalsIgnoreCase("tradeoff"))
			{
				player.sendMessage("Trade restrictions removed.");
				player.setTradeDisabled(false);
			}
		}
	}
	
	public String getZoneName()
	{
		return _zoneName;
	}
	
	public boolean isFlyingEnable()
	{
		return _isFlyingEnable;
	}
	
	private String _zoneName;
	private boolean _isFlyingEnable;
}
