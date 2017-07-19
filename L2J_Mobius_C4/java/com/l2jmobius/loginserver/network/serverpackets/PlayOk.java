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
 * <p>
 * This packet tells the client that he can enter the selected gameserver and gives him a part of the session key.
 * </p>
 * <p>
 * Format: f
 * <ul>
 * <li>f: the loginOk session key</li>
 * </ul>
 * </p>
 */
public class PlayOk extends ServerBasePacket
{
	public PlayOk(SessionKey sessionKey)
	{
		writeC(0x07);
		writeD(sessionKey.playOkID1);
		writeD(sessionKey.playOkID2);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}