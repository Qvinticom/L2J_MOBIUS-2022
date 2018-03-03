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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;

/**
 * @author Maktakien
 */
public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatId;
	private Location _destination;
	private Location _origin;
	
	/**
	 * @param actor
	 * @param destination
	 * @param origin
	 */
	public MoveToLocationInVehicle(L2Character actor, Location destination, Location origin)
	{
		if (!(actor instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) actor;
		
		if (player.getBoat() == null)
		{
			return;
		}
		
		_charObjId = player.getObjectId();
		_boatId = player.getBoat().getObjectId();
		_destination = destination;
		_origin = origin;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		writeC(0x71);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_destination.getX());
		writeD(_destination.getY());
		writeD(_destination.getZ());
		writeD(_origin.getX());
		writeD(_origin.getY());
		writeD(_origin.getZ());
	}
}
