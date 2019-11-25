/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.loginserver.network.serverpackets;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ServerList extends ServerBasePacket
{
	private final List<ServerData> _servers = new ArrayList<>();
	
	public void addServer(String ip, int port, boolean pvp, boolean testServer, int currentPlayer, int maxPlayer)
	{
		_servers.add(new ServerData(ip, port, testServer, pvp, currentPlayer, maxPlayer));
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x04);
		writeC(_servers.size());
		writeC(0);
		for (int i = 0; i < _servers.size(); ++i)
		{
			final ServerData server = _servers.get(i);
			writeC(i + 1);
			try
			{
				final InetAddress i4 = InetAddress.getByName(server.ip);
				final byte[] raw = i4.getAddress();
				writeC(raw[0] & 0xFF);
				writeC(raw[1] & 0xFF);
				writeC(raw[2] & 0xFF);
				writeC(raw[3] & 0xFF);
			}
			catch (UnknownHostException e)
			{
				e.printStackTrace();
				writeC(127);
				writeC(0);
				writeC(0);
				writeC(1);
			}
			writeD(server.port);
			writeC(15);
			if (server.pvp)
			{
				writeC(1);
			}
			else
			{
				writeC(0);
			}
			writeH(server.currentPlayers);
			writeH(server.maxPlayers);
			writeC(1);
			if (server.testServer)
			{
				writeD(4);
				continue;
			}
			writeD(0);
		}
	}
	
	class ServerData
	{
		String ip;
		int port;
		boolean pvp;
		int currentPlayers;
		int maxPlayers;
		boolean testServer;
		
		ServerData(String ip, int port, boolean pvp, boolean testServer, int currentPlayers, int maxPlayers)
		{
			this.ip = ip;
			this.port = port;
			this.pvp = pvp;
			this.testServer = testServer;
			this.currentPlayers = currentPlayers;
			this.maxPlayers = maxPlayers;
		}
	}
}
