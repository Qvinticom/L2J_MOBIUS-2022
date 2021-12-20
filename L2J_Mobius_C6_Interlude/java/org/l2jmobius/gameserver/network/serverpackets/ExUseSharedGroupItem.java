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
 * Format: ch dddd.
 * @author KenM
 */
public class ExUseSharedGroupItem implements IClientOutgoingPacket
{
	/** The _unk4. */
	private final int _unk1;
	/**
	 * The _unk4.
	 */
	private final int _unk2;
	/**
	 * The _unk4.
	 */
	private final int _unk3;
	/**
	 * The _unk4.
	 */
	private final int _unk4;
	
	/**
	 * Instantiates a new ex use shared group item.
	 * @param unk1 the unk1
	 * @param unk2 the unk2
	 * @param unk3 the unk3
	 * @param unk4 the unk4
	 */
	public ExUseSharedGroupItem(int unk1, int unk2, int unk3, int unk4)
	{
		_unk1 = unk1;
		_unk2 = unk2;
		_unk3 = unk3;
		_unk4 = unk4;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_USE_SHARED_GROUP_ITEM.writeId(packet);
		packet.writeD(_unk1);
		packet.writeD(_unk2);
		packet.writeD(_unk3);
		packet.writeD(_unk4);
		return true;
	}
}
