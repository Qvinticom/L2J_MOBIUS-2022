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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Gnacik
 */
public class ExPartyRoomMember implements IClientOutgoingPacket
{
	private final PartyMatchRoom _room;
	private final int _mode;
	
	public ExPartyRoomMember(PartyMatchRoom room, int mode)
	{
		_room = room;
		_mode = mode;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PARTY_ROOM_MEMBER.writeId(packet);
		packet.writeD(_mode);
		packet.writeD(_room.getMembers());
		for (Player member : _room.getPartyMembers())
		{
			packet.writeD(member.getObjectId());
			packet.writeS(member.getName());
			packet.writeD(member.getActiveClass());
			packet.writeD(member.getLevel());
			packet.writeD(_room.getLocation());
			if (_room.getOwner().equals(member))
			{
				packet.writeD(1);
			}
			else
			{
				if ((_room.getOwner().isInParty() && member.isInParty()) && (_room.getOwner().getParty().getLeaderObjectId() == member.getParty().getLeaderObjectId()))
				{
					packet.writeD(2);
				}
				else
				{
					packet.writeD(0);
				}
			}
			packet.writeD(0); // TODO: Instance datas there is more if that is not 0!
		}
		return true;
	}
}