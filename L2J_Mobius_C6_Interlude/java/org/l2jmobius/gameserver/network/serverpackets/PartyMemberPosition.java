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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author zabbix
 */
public class PartyMemberPosition implements IClientOutgoingPacket
{
	Map<Integer, Location> _locations = new HashMap<>();
	
	public PartyMemberPosition(Party party)
	{
		reuse(party);
	}
	
	public void reuse(Party party)
	{
		_locations.clear();
		for (Player member : party.getPartyMembers())
		{
			if (member == null)
			{
				continue;
			}
			_locations.put(member.getObjectId(), new Location(member));
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PARTY_MEMBER_POSITION.writeId(packet);
		packet.writeD(_locations.size());
		
		for (Entry<Integer, Location> entry : _locations.entrySet())
		{
			final Location loc = entry.getValue();
			packet.writeD(entry.getKey());
			packet.writeD(loc.getX());
			packet.writeD(loc.getY());
			packet.writeD(loc.getZ());
		}
		return true;
	}
}
