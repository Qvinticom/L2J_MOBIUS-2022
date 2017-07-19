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
package com.l2jmobius.gameserver.handler;

import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;

import javolution.util.FastMap;

/**
 * This class ...
 * @version $Revision: 1.1.4.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class VoicedCommandHandler
{
	private static Logger _log = Logger.getLogger(ItemHandler.class.getName());
	
	private static VoicedCommandHandler _instance;
	
	private final Map<String, IVoicedCommandHandler> _datatable;
	
	public static VoicedCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new VoicedCommandHandler();
		}
		return _instance;
	}
	
	private VoicedCommandHandler()
	{
		_datatable = new FastMap<>();
	}
	
	public void registerVoicedCommandHandler(IVoicedCommandHandler handler)
	{
		final String[] ids = handler.getVoicedCommandList();
		for (final String id : ids)
		{
			if (Config.DEBUG)
			{
				_log.fine("Adding handler for command " + id);
			}
			_datatable.put(new String(id), handler);
		}
	}
	
	public IVoicedCommandHandler getVoicedCommandHandler(String voicedCommand)
	{
		String command = voicedCommand;
		if (voicedCommand.indexOf(" ") != -1)
		{
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		}
		if (Config.DEBUG)
		{
			_log.fine("getting handler for command: " + command + " -> " + (_datatable.get(new String(command)) != null));
		}
		return _datatable.get(command);
	}
	
	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
}