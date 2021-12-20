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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class ObservationMode implements IClientOutgoingPacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	
	/**
	 * @param x
	 * @param y
	 * @param z
	 */
	public ObservationMode(int x, int y, int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.OBSERVATION_MODE.writeId(packet);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeC(0);
		packet.writeC(0xc0);
		packet.writeC(0);
		return true;
	}
}
