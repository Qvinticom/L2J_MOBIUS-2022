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
package org.l2jmobius.gameserver.handler.custom;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.Rebirth;

/**
 * This will simply manage any custom 'Enter World callers' needed.<br>
 * Rather then having to add them to the core's. (yuck!)
 * @author JStar
 */
public class CustomWorldHandler
{
	private CustomWorldHandler()
	{
		// Do Nothing ^_-
	}
	
	/**
	 * Requests entry into the world - manages appropriately.
	 * @param player
	 */
	public void enterWorld(PlayerInstance player)
	{
		// Rebirth's skills must be actived only on main class
		if (!player.isSubClassActive())
		{
			Rebirth.getInstance().grantRebirthSkills(player); // Rebirth Caller - if player has any skills, they will be granted them.
		}
	}
	
	/**
	 * Requests removal from the world - manages appropriately.
	 * @param player
	 */
	public void exitWorld(PlayerInstance player)
	{
		// TODO: Remove the rebirth engine's bonus skills from player?
	}
	
	public static CustomWorldHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomWorldHandler INSTANCE = new CustomWorldHandler();
	}
}
