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
package org.l2jmobius.loginserver.clientpackets;

public class RequestServerList
{
	private long _data1;
	private long _data2;
	private final int _data3;
	
	public long getData1()
	{
		return _data1;
	}
	
	public long getData2()
	{
		return _data2;
	}
	
	public int getData3()
	{
		return _data3;
	}
	
	public RequestServerList(byte[] rawPacket)
	{
		_data1 = rawPacket[1] & 0xFF;
		_data1 |= (rawPacket[2] << 8) & 0xFF00;
		_data1 |= (rawPacket[3] << 16) & 0xFF0000;
		_data1 |= (rawPacket[4] << 24) & 0xFF000000;
		_data2 = rawPacket[5] & 0xFF;
		_data2 |= (rawPacket[6] << 8) & 0xFF00;
		_data2 |= (rawPacket[7] << 16) & 0xFF0000;
		_data2 |= (rawPacket[8] << 24) & 0xFF000000;
		_data3 = rawPacket[9] & 0xFF;
	}
}
