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
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Gnacik
 */
public class PartyMatchDetail implements IClientOutgoingPacket
{
	private final PartyMatchRoom _room;
	
	/**
	 * @param room
	 */
	public PartyMatchDetail(PartyMatchRoom room)
	{
		_room = room;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PARTY_MATCH_DETAIL.writeId(packet);
		packet.writeD(_room.getId()); // Room ID
		packet.writeD(_room.getMaxMembers()); // Max Members
		packet.writeD(_room.getMinLevel()); // Level Min
		packet.writeD(_room.getMaxLevel()); // Level Max
		packet.writeD(_room.getLootType()); // Loot Type
		packet.writeD(_room.getLocation()); // Room Location
		packet.writeS(_room.getTitle()); // Room title
		return true;
	}
}