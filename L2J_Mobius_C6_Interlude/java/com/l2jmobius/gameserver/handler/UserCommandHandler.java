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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GameServer;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ChannelDelete;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ChannelLeave;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ChannelListUpdate;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ClanPenalty;
import com.l2jmobius.gameserver.handler.usercommandhandlers.ClanWarsList;
import com.l2jmobius.gameserver.handler.usercommandhandlers.DisMount;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Escape;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Loc;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Mount;
import com.l2jmobius.gameserver.handler.usercommandhandlers.OfflineShop;
import com.l2jmobius.gameserver.handler.usercommandhandlers.OlympiadStat;
import com.l2jmobius.gameserver.handler.usercommandhandlers.PartyInfo;
import com.l2jmobius.gameserver.handler.usercommandhandlers.SiegeStatus;
import com.l2jmobius.gameserver.handler.usercommandhandlers.Time;

public class UserCommandHandler
{
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	
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
		_datatable = new HashMap<>();
		registerUserCommandHandler(new Time());
		registerUserCommandHandler(new OlympiadStat());
		registerUserCommandHandler(new ChannelLeave());
		registerUserCommandHandler(new ChannelDelete());
		registerUserCommandHandler(new ChannelListUpdate());
		registerUserCommandHandler(new ClanPenalty());
		registerUserCommandHandler(new ClanWarsList());
		registerUserCommandHandler(new DisMount());
		registerUserCommandHandler(new Escape());
		registerUserCommandHandler(new Loc());
		registerUserCommandHandler(new Mount());
		registerUserCommandHandler(new PartyInfo());
		registerUserCommandHandler(new SiegeStatus());
		if (Config.OFFLINE_TRADE_ENABLE && Config.OFFLINE_COMMAND1)
		{
			registerUserCommandHandler(new OfflineShop());
		}
		LOGGER.info("UserCommandHandler: Loaded " + _datatable.size() + " handlers.");
	}
	
	public void registerUserCommandHandler(IUserCommandHandler handler)
	{
		int[] ids = handler.getUserCommandList();
		
		for (int id : ids)
		{
			_datatable.put(new Integer(id), handler);
		}
	}
	
	public IUserCommandHandler getUserCommandHandler(int userCommand)
	{
		return _datatable.get(new Integer(userCommand));
	}
	
	public int size()
	{
		return _datatable.size();
	}
}