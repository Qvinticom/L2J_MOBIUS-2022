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
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Format: (ch) d[ddddd].
 * @author -Wooden-
 */
public class ExCursedWeaponLocation implements IClientOutgoingPacket
{
	private final List<CursedWeaponInfo> _cursedWeaponInfo;
	
	/**
	 * Instantiates a new ex cursed weapon location.
	 * @param cursedWeaponInfo the cursed weapon info
	 */
	public ExCursedWeaponLocation(List<CursedWeaponInfo> cursedWeaponInfo)
	{
		_cursedWeaponInfo = cursedWeaponInfo;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_CURSED_WEAPON_LOCATION.writeId(packet);
		if (!_cursedWeaponInfo.isEmpty())
		{
			packet.writeD(_cursedWeaponInfo.size());
			for (CursedWeaponInfo w : _cursedWeaponInfo)
			{
				packet.writeD(w.id);
				packet.writeD(w.activated);
				packet.writeD(w.loc.getX());
				packet.writeD(w.loc.getY());
				packet.writeD(w.loc.getZ());
			}
		}
		else
		{
			packet.writeD(0);
			packet.writeD(0);
		}
		return true;
	}
	
	/**
	 * The Class CursedWeaponInfo.
	 */
	public static class CursedWeaponInfo
	{
		/** The location. */
		public Location loc;
		/** The id. */
		public int id;
		/** The activated. */
		public int activated; // 0 - not activated ? 1 - activated
		
		/**
		 * Instantiates a new cursed weapon info.
		 * @param location the Location
		 * @param cwId the Id
		 * @param status the status
		 */
		public CursedWeaponInfo(Location location, int cwId, int status)
		{
			loc = location;
			id = cwId;
			activated = status;
		}
	}
}
