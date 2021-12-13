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
 * @author KenM
 */
public class ExSetCompassZoneCode implements IClientOutgoingPacket
{
	public static final int ALTEREDZONE = 7;
	public static final int SIEGEWARZONE = 10;
	public static final int PEACEZONE = 11;
	public static final int SEVENSIGNSZONE = 12;
	public static final int NOPVPZONE = 13;
	public static final int PVPZONE = 14;
	public static final int GENERALZONE = 15;
	
	private final int _zoneType;
	
	public ExSetCompassZoneCode(int value)
	{
		_zoneType = value;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SET_COMPASS_ZONE_CODE.writeId(packet);
		
		packet.writeD(_zoneType);
		return true;
	}
}
