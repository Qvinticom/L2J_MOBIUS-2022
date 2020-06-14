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
package org.l2jmobius.loginserver;

import java.util.logging.Logger;

import org.l2jmobius.loginserver.LoginClient.LoginClientState;
import org.l2jmobius.loginserver.network.clientpackets.ClientBasePacket;
import org.l2jmobius.loginserver.network.clientpackets.RequestAuthGG;
import org.l2jmobius.loginserver.network.clientpackets.RequestAuthLogin;
import org.l2jmobius.loginserver.network.clientpackets.RequestServerList;
import org.l2jmobius.loginserver.network.clientpackets.RequestServerLogin;

/**
 * Handler for packets received by Login Server
 * @author KenM
 */
public class LoginPacketHandler
{
	private static final Logger LOGGER = Logger.getLogger(LoginPacketHandler.class.getName());
	
	public static ClientBasePacket handlePacket(byte[] data, LoginClient client)
	{
		final int opcode = data[0] & 0xFF;
		ClientBasePacket packet = null;
		final LoginClientState state = client.getClientState();
		
		switch (state)
		{
			case CONNECTED:
			{
				if (opcode == 0x07)
				{
					packet = new RequestAuthGG(data, client);
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			}
			case AUTHED_GG:
			{
				if (opcode == 0x00)
				{
					packet = new RequestAuthLogin(data, client);
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			}
			case AUTHED_LOGIN:
			{
				if (opcode == 0x05)
				{
					packet = new RequestServerList(data, client);
				}
				else if (opcode == 0x02)
				{
					packet = new RequestServerLogin(data, client);
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			}
		}
		return packet;
	}
	
	private static void debugOpcode(int opcode, LoginClientState state)
	{
		LOGGER.info("Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}