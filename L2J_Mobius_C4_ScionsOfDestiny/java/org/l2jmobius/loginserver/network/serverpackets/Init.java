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

import org.l2jmobius.loginserver.network.AbstractServerPacket;
import org.l2jmobius.loginserver.network.LoginClient;

/**
 * Format: dd b dddd s d: session id d: protocol revision b: 0x90 bytes : 0x80 bytes for the scrambled RSA public key 0x10 bytes at 0x00 d: unknown d: unknown d: unknown d: unknown s: blowfish key
 */
public final class Init extends AbstractServerPacket
{
	public Init(LoginClient client)
	{
		writeC(0x00);
		writeD(client.getSessionId());
		writeD(0x0000c621); // protocol revision
		
		writeB(client.getScrambledModulus());
		
		// unk GG related?
		writeD(0x29DD954E);
		writeD(0x77C39CFC);
		writeD(0x97ADB620);
		writeD(0x07BDE0F7);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}