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
package com.l2jmobius.loginserver;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

import com.l2jmobius.commons.mmocore.IPacketHandler;
import com.l2jmobius.commons.mmocore.ReceivablePacket;
import com.l2jmobius.loginserver.L2LoginClient.LoginClientState;
import com.l2jmobius.loginserver.network.clientpackets.AuthGameGuard;
import com.l2jmobius.loginserver.network.clientpackets.RequestAuthLogin;
import com.l2jmobius.loginserver.network.clientpackets.RequestServerList;
import com.l2jmobius.loginserver.network.clientpackets.RequestServerLogin;

/**
 * Handler for packets received by Login Server
 * @author KenM
 */

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	private final Logger LOGGER = Logger.getLogger(L2LoginPacketHandler.class.getName());
	
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		final int opcode = buf.get() & 0xFF;
		
		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
			{
				if (opcode == 0x07)
				{
					packet = new AuthGameGuard();
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
					packet = new RequestAuthLogin();
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
					packet = new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					packet = new RequestServerLogin();
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
	
	private void debugOpcode(int opcode, LoginClientState state)
	{
		LOGGER.info("Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}