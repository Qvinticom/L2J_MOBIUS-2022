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
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.KeyPacket;

public class ProtocolVersion implements IClientIncomingPacket
{
	static Logger LOGGER = Logger.getLogger(ProtocolVersion.class.getName());
	private int _version;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_version = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if ((_version == 65534) || (_version == -2)) // Ping
		{
			// this is just a ping attempt from the new C2 client
			client.closeNow();
		}
		else if ((_version < Config.MIN_PROTOCOL_REVISION) || (_version > Config.MAX_PROTOCOL_REVISION))
		{
			LOGGER.info("Client: " + client + " -> Protocol Revision: " + _version + " is invalid. Minimum is " + Config.MIN_PROTOCOL_REVISION + " and Maximum is " + Config.MAX_PROTOCOL_REVISION + " are supported. Closing connection.");
			LOGGER.warning("Wrong Protocol Version " + _version);
			client.close(new KeyPacket(client.enableCrypt(), 0));
		}
		else
		{
			client.sendPacket(new KeyPacket(client.enableCrypt(), 1));
			client.setProtocolVersion(_version);
		}
	}
}