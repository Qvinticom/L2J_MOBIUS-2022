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
 * Format: ch d.
 * @author KenM
 */
public class ExDuelStart implements IClientOutgoingPacket
{
	/** The _unk1. */
	private final int _unk1;
	
	/**
	 * Instantiates a new ex duel start.
	 * @param unk1 the unk1
	 */
	public ExDuelStart(int unk1)
	{
		_unk1 = unk1;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_DUEL_START.writeId(packet);
		packet.writeD(_unk1);
		return true;
	}
}
