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

public class ExAutoSoulShot implements IClientOutgoingPacket
{
	private final int _itemId;
	@SuppressWarnings("unused")
	private final boolean _enable;
	private final int _type;
	
	/**
	 * @param itemId
	 * @param enable
	 * @param type
	 */
	public ExAutoSoulShot(int itemId, boolean enable, int type)
	{
		_itemId = itemId;
		_enable = enable;
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_AUTO_SOUL_SHOT.writeId(packet);
		packet.writeD(_itemId);
		// packet.writeD(_enable ? 1 : 0);
		packet.writeD(_type);
		return true;
	}
}
