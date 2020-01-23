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
package org.l2jmobius.gameserver.network.serverpackets.sessionzones;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class TimedHuntingZoneList implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	
	public TimedHuntingZoneList(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_TIME_RESTRICT_FIELD_LIST.writeId(packet);
		
		packet.writeD(2); // zone count
		
		// Isle of Storms
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(150000); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(1); // field id
		packet.writeD(100); // min level
		packet.writeD(120); // max level
		packet.writeD(3600); // remain time base
		packet.writeD(3600); // remain time
		packet.writeD(21600); // remain time max
		packet.writeD(18000); // remain refill time
		packet.writeD(18000); // refill time max
		packet.writeC(_player.isInTimedHuntingZone() ? 0 : 1); // field activated
		
		// Primeval Isle
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(150000); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(6); // field id
		packet.writeD(105); // min level
		packet.writeD(120); // max level
		packet.writeD(3600); // remain time base
		packet.writeD(3600); // remain time
		packet.writeD(21600); // remain time max
		packet.writeD(18000); // remain refill time
		packet.writeD(18000); // refill time max
		packet.writeC(_player.isInTimedHuntingZone() ? 0 : 1); // field activated
		
		return true;
	}
}