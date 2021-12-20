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
 * Format: (ch)ddd.
 */
public class ExVariationResult implements IClientOutgoingPacket
{
	/** The _stat12. */
	private final int _stat12;
	/** The _stat34. */
	private final int _stat34;
	/** The _unk3. */
	private final int _unk3;
	
	/**
	 * Instantiates a new ex variation result.
	 * @param unk1 the unk1
	 * @param unk2 the unk2
	 * @param unk3 the unk3
	 */
	public ExVariationResult(int unk1, int unk2, int unk3)
	{
		_stat12 = unk1;
		_stat34 = unk2;
		_unk3 = unk3;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_VARIATION_RESULT.writeId(packet);
		packet.writeD(_stat12);
		packet.writeD(_stat34);
		packet.writeD(_unk3);
		return true;
	}
}
