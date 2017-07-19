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

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:11 $
 */
public class PlayFail extends ServerBasePacket
{
	public static int REASON_TOO_MANY_PLAYERS = 0x0f; // too many players on server
	public static int REASON_SYSTEM_ERROR = 0x01; // system error
	public static int REASON_USER_OR_PASS_WRONG = 0x02;
	public static int REASON3 = 0x03;
	public static int REASON4 = 0x04;
	
	public PlayFail(int reason)
	{
		writeC(0x06);
		writeC(reason);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}