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

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Format : (h) d [dS] h sub id d: number of manors [ d: id S: manor name ]
 * @author l3x
 */
public class ExSendManorList implements IClientOutgoingPacket
{
	private final List<String> _manors;
	
	public ExSendManorList(List<String> manors)
	{
		_manors = manors;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SEND_MANOR_LIST.writeId(packet);
		packet.writeD(_manors.size());
		for (int i = 0; i < _manors.size(); i++)
		{
			final int j = i + 1;
			packet.writeD(j);
			packet.writeS(_manors.get(i));
		}
		return true;
	}
}
