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
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
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
		packet.writeD(4); // zone count
		
		// Primeval Isle
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(1); // zone id
		packet.writeD(40); // min level
		packet.writeD(999); // max level
		packet.writeD(0); // remain time base?
		endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 1, 0);
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_PRIMEVAL;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_PRIMEVAL / 1000));
		packet.writeD(43200); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0);
		
		// Forgotten Primeval Garden
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(4); // zone id
		packet.writeD(76); // min level
		packet.writeD(999); // max level
		packet.writeD(0); // remain time base?
		endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 4, 0);
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_GARDEN / 1000));
		packet.writeD(18000); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0);
		
		// Alligator Island
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(1); // reset cycle
		packet.writeD(11); // zone id
		packet.writeD(60); // min level
		packet.writeD(999); // max level
		packet.writeD(0); // remain time base?
		endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 11, 0);
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_ALLIGATOR / 1000));
		packet.writeD(7200); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0);
		
		// Antharas Lair
		packet.writeD(1); // required item count
		packet.writeD(57); // item id
		packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		packet.writeD(0); // reset cycle
		packet.writeD(12); // zone id
		packet.writeD(80); // min level
		packet.writeD(999); // max level
		packet.writeD(0); // remain time base?
		endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 12, 0);
		if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY_ANTHARAS) < currentTime)
		{
			endTime = currentTime + Config.TIME_LIMITED_ZONE_ANTHARAS;
		}
		packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_ANTHARAS / 1000));
		packet.writeD(126000); // remain refill time
		packet.writeD(3600); // refill time max
		packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		packet.writeH(0);
		
		// Transcendent Instance Zone 1
		// packet.writeD(1); // required item count
		// packet.writeD(57); // item id
		// packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		// packet.writeD(1); // reset cycle
		// packet.writeD(101); // zone id
		// packet.writeD(40); // min level
		// packet.writeD(49); // max level
		// packet.writeD(0); // remain time base?
		// endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 101, 0);
		// if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		// {
		// endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		// }
		// packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		// packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		// packet.writeD(3600); // remain refill time
		// packet.writeD(3600); // refill time max
		// packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		// packet.writeH(0);
		
		// Transcendent Instance Zone 2
		// packet.writeD(1); // required item count
		// packet.writeD(57); // item id
		// packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		// packet.writeD(1); // reset cycle
		// packet.writeD(102); // zone id
		// packet.writeD(50); // min level
		// packet.writeD(59); // max level
		// packet.writeD(0); // remain time base?
		// endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 102, 0);
		// if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		// {
		// endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		// }
		// packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		// packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		// packet.writeD(3600); // remain refill time
		// packet.writeD(3600); // refill time max
		// packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		// packet.writeH(0);
		
		// Transcendent Instance Zone 3
		// packet.writeD(1); // required item count
		// packet.writeD(57); // item id
		// packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		// packet.writeD(1); // reset cycle
		// packet.writeD(103); // zone id
		// packet.writeD(60); // min level
		// packet.writeD(69); // max level
		// packet.writeD(0); // remain time base?
		// endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 103, 0);
		// if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		// {
		// endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		// }
		// packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		// packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		// packet.writeD(3600); // remain refill time
		// packet.writeD(3600); // refill time max
		// packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		// packet.writeH(0);
		
		// Transcendent Instance Zone 4
		// packet.writeD(1); // required item count
		// packet.writeD(57); // item id
		// packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		// packet.writeD(1); // reset cycle
		// packet.writeD(104); // zone id
		// packet.writeD(70); // min level
		// packet.writeD(79); // max level
		// packet.writeD(0); // remain time base?
		// endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 104, 0);
		// if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		// {
		// endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		// }
		// packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		// packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		// packet.writeD(3600); // remain refill time
		// packet.writeD(3600); // refill time max
		// packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		// packet.writeH(0);
		
		// Transcendent Instance Zone 6
		// packet.writeD(1); // required item count
		// packet.writeD(57); // item id
		// packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		// packet.writeD(1); // reset cycle
		// packet.writeD(106); // zone id
		// packet.writeD(80); // min level
		// packet.writeD(999); // max level
		// packet.writeD(0); // remain time base?
		// endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 106, 0);
		// if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		// {
		// endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		// }
		// packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		// packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		// packet.writeD(3600); // remain refill time
		// packet.writeD(3600); // refill time max
		// packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		// packet.writeH(0);
		
		// Transcendent Instance Zone 7
		// packet.writeD(1); // required item count
		// packet.writeD(57); // item id
		// packet.writeQ(Config.TIME_LIMITED_ZONE_TELEPORT_FEE); // item count
		// packet.writeD(1); // reset cycle
		// packet.writeD(107); // zone id
		// packet.writeD(85); // min level
		// packet.writeD(999); // max level
		// packet.writeD(0); // remain time base?
		// endTime = _player.getVariables().getLong(PlayerVariables.HUNTING_ZONE_RESET_TIME + 107, 0);
		// if ((endTime + Config.TIME_LIMITED_ZONE_RESET_DELAY) < currentTime)
		// {
		// endTime = currentTime + Config.TIME_LIMITED_ZONE_INITIAL_TIME;
		// }
		// packet.writeD((int) (Math.max(endTime - currentTime, 0)) / 1000); // remain time
		// packet.writeD((int) (Config.TIME_LIMITED_MAX_ADDED_TIME / 1000));
		// packet.writeD(3600); // remain refill time
		// packet.writeD(3600); // refill time max
		// packet.writeD(_isInTimedHuntingZone ? 0 : 1); // field activated (272 C to D)
		// packet.writeH(0);
		
		return true;
	}
}