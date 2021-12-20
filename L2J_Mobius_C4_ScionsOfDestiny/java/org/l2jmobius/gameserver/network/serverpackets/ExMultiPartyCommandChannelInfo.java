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
import org.l2jmobius.gameserver.model.CommandChannel;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author chris_00 ch sdd d[sdd]
 */
public class ExMultiPartyCommandChannelInfo implements IClientOutgoingPacket
{
	private final CommandChannel _channel;
	
	public ExMultiPartyCommandChannelInfo(CommandChannel channel)
	{
		_channel = channel;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_channel == null)
		{
			return false;
		}
		OutgoingPackets.EX_MULTI_PARTY_COMMAND_CHANNEL_INFO.writeId(packet);
		packet.writeS(_channel.getChannelLeader().getName());
		// packet.writeD(0); // Channel loot
		packet.writeD(_channel.getMemberCount());
		packet.writeD(_channel.getParties().size());
		for (Party p : _channel.getParties())
		{
			packet.writeS(p.getLeader().getName());
			// packet.writeD(p.getPartyLeaderOID());
			packet.writeD(p.getMemberCount());
		}
		return true;
	}
}