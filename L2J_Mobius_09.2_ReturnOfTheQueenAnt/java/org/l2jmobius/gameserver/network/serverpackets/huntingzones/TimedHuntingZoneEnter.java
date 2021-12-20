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
package org.l2jmobius.gameserver.network.serverpackets.huntingzones;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius, Index, NasSeKa`, Serenitty
 */
public class TimedHuntingZoneEnter implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _zoneId;
	
	public TimedHuntingZoneEnter(Player player, int zoneId)
	{
		_player = player;
		_zoneId = zoneId;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_TIME_RESTRICT_FIELD_USER_ENTER.writeId(packet);
		packet.writeC(1); // bEnterSuccess
		packet.writeD(_zoneId);
		packet.writeD((int) ((Chronos.currentTimeMillis() / 60) / 1000)); // nEnterTimeStamp (current time in minutes)
		packet.writeD(_player.getTimedHuntingZoneRemainingTime(_zoneId) / 1000); // nRemainTime (zone left time)
		return true;
	}
}