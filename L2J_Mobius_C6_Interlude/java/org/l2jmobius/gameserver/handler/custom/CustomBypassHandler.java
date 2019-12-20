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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.handler.ICustomByPassHandler;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.entity.Rebirth;

/**
 * This 'Bypass Handler' is a handy tool indeed!<br>
 * Basically, you can send any custom bypass commmands to it from ANY npc and it will call the appropriate function.<br>
 * <strong>Example:</strong><br>
 * <button value=" Request Rebirth " action="bypass -h custom_rebirth_confirmrequest" width=110 height=36 back="L2UI_ct1.button_df" fore="L2UI_ct1.button_df">
 * @author JStar
 */
public class CustomBypassHandler
{
	private static final Logger LOGGER = Logger.getLogger(CustomBypassHandler.class.getName());
	
	private final Map<String, ICustomByPassHandler> _handlers;
	
	private CustomBypassHandler()
	{
		_handlers = new HashMap<>();
		registerCustomBypassHandler(new ExtractableByPassHandler());
	}
	
	/**
	 * @param handler as ICustomByPassHandler
	 */
	private void registerCustomBypassHandler(ICustomByPassHandler handler)
	{
		for (String s : handler.getByPassCommands())
		{
			_handlers.put(s, handler);
		}
	}
	
	/**
	 * Handles player's Bypass request to the Custom Content.
	 * @param player
	 * @param command
	 */
	public void handleBypass(PlayerInstance player, String command)
	{
		// Rebirth Manager and Engine Caller
		
		String cmd = "";
		String params = "";
		final int iPos = command.indexOf(' ');
		if (iPos != -1)
		{
			cmd = command.substring(7, iPos);
			params = command.substring(iPos + 1);
		}
		else
		{
			cmd = command.substring(7);
		}
		final ICustomByPassHandler ch = _handlers.get(cmd);
		if (ch != null)
		{
			ch.handleCommand(cmd, player, params);
		}
		else if (command.startsWith("custom_rebirth"))
		{
			// Check to see if Rebirth is enabled to avoid hacks
			if (!Config.REBIRTH_ENABLE)
			{
				LOGGER.warning("[WARNING] Player " + player.getName() + " is trying to use rebirth system when it's disabled.");
				return;
			}
			
			Rebirth.getInstance().handleCommand(player, command);
		}
	}
	
	public static CustomBypassHandler getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final CustomBypassHandler INSTANCE = new CustomBypassHandler();
	}
}