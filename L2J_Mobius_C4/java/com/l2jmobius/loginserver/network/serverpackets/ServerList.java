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
package com.l2jmobius.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import com.l2jmobius.loginserver.GameServerTable.GameServer;
import com.l2jmobius.loginserver.network.gameserverpackets.ServerStatus;

import javolution.util.FastList;

/**
 * ServerList Format: cc [cddcchhcdc] c: server list size (number of servers) c: ? [ (repeat for each servers) c: server id (ignored by client?) d: server ip d: server port c: age limit (used by client?) c: pvp or not (used by client?) h: current number of players h: max number of players c: 0 if
 * server is down d: 2nd bit: clock 3rd bit: wont dsiplay server name 4th bit: test server (used by client?) c: 0 if you dont want to display brackets in front of sever name ] Server will be considered as Good when the number of online players is less than half the maximum. as Normal between half
 * and 4/5 and Full when there's more than 4/5 of the maximum number of players
 */
public class ServerList extends ServerBasePacket
{
	private final List<ServerData> _servers;
	
	private boolean _listDone = false;
	private final int _lastServer;
	
	class ServerData
	{
		String ip;
		int port;
		boolean pvp;
		int currentPlayers;
		int maxPlayers;
		boolean testServer;
		boolean brackets;
		boolean clock;
		int status;
		public int server_id;
		
		ServerData(String pIp, GameServer gs, int pStatus)
		{
			ip = pIp;
			port = gs.port;
			pvp = gs.pvp;
			testServer = gs.testServer;
			currentPlayers = (gs.gst == null ? 0 : gs.gst.getCurrentPlayers());
			maxPlayers = gs.maxPlayers;
			brackets = gs.brackets;
			clock = gs.clock;
			status = pStatus;
			server_id = gs.server_id;
		}
	}
	
	public ServerList(int lastServer)
	{
		_lastServer = lastServer;
		
		_servers = new FastList<>();
	}
	
	public void addServer(String ip, GameServer game, int status)
	{
		_servers.add(new ServerData(ip, game, status));
	}
	
	@Override
	public byte[] getContent()
	{
		if (!_listDone) // list should only be done once even if there are multiple getContent calls
		{
			writeC(0x04);
			writeC(_servers.size());
			writeC(_lastServer);
			
			for (final ServerData server : _servers)
			{
				writeC(server.server_id); // server id
				try
				{
					final InetAddress i4 = InetAddress.getByName(server.ip);
					final byte[] raw = i4.getAddress();
					writeC(raw[0] & 0xff);
					writeC(raw[1] & 0xff);
					writeC(raw[2] & 0xff);
					writeC(raw[3] & 0xff);
				}
				catch (final UnknownHostException e)
				{
					e.printStackTrace();
					writeC(127);
					writeC(0);
					writeC(0);
					writeC(1);
				}
				
				writeD(server.port);
				writeC(0x0f); // age limit
				if (server.pvp)
				{
					writeC(0x01);
				}
				else
				{
					writeC(0x00);
				}
				
				writeH(server.currentPlayers);
				writeH(server.maxPlayers);
				
				if (server.status == ServerStatus.STATUS_DOWN)
				{
					writeC(0x00);
				}
				else
				{
					writeC(0x01);
				}
				
				int bits = 0;
				if (server.testServer)
				{
					bits |= 0x04;
				}
				
				if (server.clock)
				{
					bits |= 0x02;
				}
				
				writeD(bits);
				if (server.brackets)
				{
					writeC(0x01);
				}
				else
				{
					writeC(0x00);
				}
			}
			_listDone = true;
		}
		
		return getBytes();
	}
}