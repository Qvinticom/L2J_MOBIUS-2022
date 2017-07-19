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

import com.l2jmobius.loginserver.SessionKey;

/**
 * Format: dddddddd f: the session key d: ? d: ? d: ? d: ? d: ? d: ?
 */
public class LoginOk extends ServerBasePacket
{
	public LoginOk(SessionKey sessionKey)
	{
		writeC(0x03);
		writeD(sessionKey.loginOkID1);
		writeD(sessionKey.loginOkID2);
		writeD(0x00);
		writeD(0x00);
		writeD(0x000003ea);
		writeD(0x00);
		writeD(0x00);
		writeD(0x02);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}