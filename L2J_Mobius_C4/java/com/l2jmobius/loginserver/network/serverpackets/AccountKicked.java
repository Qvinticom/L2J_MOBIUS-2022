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

public class AccountKicked extends ServerBasePacket
{
	public static int REASON_ILLEGAL_USE = 0x01;
	public static int REASON_GENERAL_VIOLATION = 0x08;
	
	public AccountKicked(int reason)
	{
		writeC(0x02);
		writeD(reason);
	}
	
	@Override
	public byte[] getContent()
	{
		return getBytes();
	}
}