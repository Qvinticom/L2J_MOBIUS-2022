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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class TimedHuntingZoneList implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	private final boolean _isInTimedHuntingZone;
	
	public TimedHuntingZoneList(PlayerInstance player)
	{
		_player = player;
		_isInTimedHuntingZone = player.isInsideZone(ZoneId.TIMED_HUNTING);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_TIME_RESTRICT_FIELD_LIST.writeId(packet);
		
		final long currentTime = Chronos.currentTimeMillis();
		long endTime;
		packet.writeD(6); // zone count
		
		// Storm Isle
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(1); // zone id
		packet.writeD(100); // min level
		packet.writeD(120); // max level
		packet.writeD((int) (Config.TIME_LIMITED_ZONE_INITIAL_TIME / 1000)); // remain time base?
		endTime = _player.getTimedHuntingZoneRemainingTime(1);
		if (endTime > 0)
		{
			endTime += currentTime;
		}
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		packet.writeD(3600); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0); // 245
		
		// Primeval Isle
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(6); // zone id
		packet.writeD(105); // min level
		packet.writeD(120); // max level
		packet.writeD((int) (Config.TIME_LIMITED_ZONE_INITIAL_TIME / 1000)); // remain time base?
		endTime = _player.getTimedHuntingZoneRemainingTime(6);
		if (endTime > 0)
		{
			endTime += currentTime;
		}
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		packet.writeD(3600); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0); // 245
		
		// Golden Altar
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(7); // zone id
		packet.writeD(107); // min level
		packet.writeD(120); // max level
		packet.writeD((int) (Config.TIME_LIMITED_ZONE_INITIAL_TIME / 1000)); // remain time base?
		endTime = _player.getTimedHuntingZoneRemainingTime(7);
		if (endTime > 0)
		{
			endTime += currentTime;
		}
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		packet.writeD(3600); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0); // 245
		
		// Abandoned Coal Mines
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(11); // zone id
		packet.writeD(99); // min level
		packet.writeD(105); // max level
		packet.writeD((int) (Config.TIME_LIMITED_ZONE_INITIAL_TIME / 1000)); // remain time base?
		endTime = _player.getTimedHuntingZoneRemainingTime(11);
		if (endTime > 0)
		{
			endTime += currentTime;
		}
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		packet.writeD(3600); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0); // 245
		
		// Tower of Insolence
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(0); // reset cycle
		packet.writeD(8); // zone id
		packet.writeD(110); // min level
		packet.writeD(130); // max level
		packet.writeD((int) (Config.TIME_LIMITED_ZONE_INITIAL_TIME_WEEKLY / 1000)); // remain time base?
		endTime = _player.getTimedHuntingZoneRemainingTime(8);
		if (endTime > 0)
		{
			endTime += currentTime;
		}
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_WEEKLY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME_WEEKLY;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME_WEEKLY / 1000));
		packet.writeD(3600); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0); // 245
		
		// Imperial Tomb
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(12); // zone id
		packet.writeD(105); // min level
		packet.writeD(130); // max level
		packet.writeD((int) (Config.TIME_LIMITED_ZONE_INITIAL_TIME / 1000)); // remain time base?
		endTime = _player.getTimedHuntingZoneRemainingTime(12);
		if (endTime > 0)
		{
			endTime += currentTime;
		}
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		packet.writeD(3600); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0); // 245
		
		return true;
	}
}