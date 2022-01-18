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
package org.l2jmobius.loginserver.network.serverpackets;

import org.l2jmobius.commons.network.IOutgoingPacket;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.loginserver.enums.LoginFailReason;
import org.l2jmobius.loginserver.network.OutgoingPackets;

/**
 * Format: d d: the failure reason
 */
public class LoginFail implements IOutgoingPacket
{
	private final LoginFailReason _reason;
	
	public LoginFail(LoginFailReason reason)
	{
		_reason = reason;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.LOGIN_FAIL.writeId(packet);
		packet.writeC(_reason.getCode());
		return true;
	}
}
