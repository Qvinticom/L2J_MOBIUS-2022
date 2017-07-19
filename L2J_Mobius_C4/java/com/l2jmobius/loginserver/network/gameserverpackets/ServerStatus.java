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
package com.l2jmobius.loginserver.network.gameserverpackets;

import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.loginserver.GameServerTable;

/**
 * @author -Wooden-
 */
public class ServerStatus extends GameServerBasePacket
{
	protected static Logger _log = Logger.getLogger(ServerStatus.class.getName());
	
	public static final String[] statusString =
	{
		"Auto",
		"Good",
		"Normal",
		"Full",
		"Down",
		"Gm Only"
	};
	
	public static final int SERVER_LIST_STATUS = 0x01;
	public static final int SERVER_LIST_CLOCK = 0x02;
	public static final int SERVER_LIST_SQUARE_BRACKET = 0x03;
	public static final int MAX_PLAYERS = 0x04;
	public static final int TEST_SERVER = 0x05;
	
	public static final int STATUS_AUTO = 0x00;
	public static final int STATUS_GOOD = 0x01;
	public static final int STATUS_NORMAL = 0x02;
	public static final int STATUS_FULL = 0x03;
	public static final int STATUS_DOWN = 0x04;
	public static final int STATUS_GM_ONLY = 0x05;
	
	public static final int ON = 0x01;
	public static final int OFF = 0x00;
	
	/**
	 * @param decrypt
	 * @param serverID
	 */
	public ServerStatus(byte[] decrypt, int serverID)
	{
		super(decrypt);
		final int size = readD();
		for (int i = 0; i < size; i++)
		{
			final int type = readD();
			final int value = readD();
			switch (type)
			{
				case SERVER_LIST_STATUS:
					GameServerTable.getInstance().setStatus(value, serverID);
					if (Config.DEBUG)
					{
						_log.info("ServerList Status (" + value + ")");
					}
					break;
				case SERVER_LIST_CLOCK:
					if (value == ON)
					{
						GameServerTable.getInstance().setClock(true, serverID);
					}
					else
					{
						GameServerTable.getInstance().setClock(false, serverID);
					}
					if (Config.DEBUG)
					{
						_log.info("ServerList Clock (" + value + ")");
					}
					break;
				case SERVER_LIST_SQUARE_BRACKET:
					if (value == ON)
					{
						GameServerTable.getInstance().setBracket(true, serverID);
					}
					else
					{
						GameServerTable.getInstance().setBracket(false, serverID);
					}
					if (Config.DEBUG)
					{
						_log.info("ServerList Bracket (" + value + ")");
					}
					break;
				case TEST_SERVER:
					if (value == ON)
					{
						GameServerTable.getInstance().setTestServer(true, serverID);
					}
					else
					{
						GameServerTable.getInstance().setTestServer(false, serverID);
					}
					if (Config.DEBUG)
					{
						_log.info("ServerList test server (" + value + ")");
					}
					break;
				case MAX_PLAYERS:
					GameServerTable.getInstance().setMaxPlayers(value, serverID);
					if (Config.DEBUG)
					{
						_log.info("ServerMaxPlayer (" + value + ")");
					}
					break;
			}
		}
	}
}