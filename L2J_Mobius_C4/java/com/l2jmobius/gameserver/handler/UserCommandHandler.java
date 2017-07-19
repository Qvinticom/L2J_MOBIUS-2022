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
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2005/03/27 15:30:09 $
 */
public class UserCommandHandler
{
	private static Logger _log = Logger.getLogger(UserCommandHandler.class.getName());
	
	private static UserCommandHandler _instance;
	
	private final Map<Integer, IUserCommandHandler> _datatable;
	
	public static UserCommandHandler getInstance()
	{
		if (_instance == null)
		{
			_instance = new UserCommandHandler();
		}
		return _instance;
	}
	
	private UserCommandHandler()
	{
		_datatable = new FastMap<>();
	}
	
	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		final int[] ids = handler.getUserCommandList();
		for (final int id : ids)
		{
			if (Config.DEBUG)
			{
				_log.fine("Adding handler for user command " + id);
			}
			_datatable.put(new Integer(id), handler);
		}
	}
	
	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		if (Config.DEBUG)
		{
			_log.fine("getting handler for user command: " + userCommand);
		}
		return _datatable.get(new Integer(userCommand));
	}
	
	/**
	 * @return
	 */
	public int size()
	{
		return _datatable.size();
	}
}