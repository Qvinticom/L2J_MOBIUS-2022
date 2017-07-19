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

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;

/**
 * The Monster Derby Track Zone
 * @author durgus
 */
public class L2DerbyTrackZone extends L2PeaceZone
{
	public L2DerbyTrackZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PlayableInstance)
		{
			character.setInsideZone(L2Character.ZONE_MONSTERTRACK, true);
		}
		super.onEnter(character);
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PlayableInstance)
		{
			character.setInsideZone(L2Character.ZONE_MONSTERTRACK, false);
		}
		super.onExit(character);
	}
}