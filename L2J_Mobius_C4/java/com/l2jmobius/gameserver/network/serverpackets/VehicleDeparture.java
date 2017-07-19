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

import com.l2jmobius.gameserver.model.actor.instance.L2BoatInstance;

/**
 * @author Maktakien
 */
public class VehicleDeparture extends L2GameServerPacket
{
	private final L2BoatInstance _boat;
	
	/**
	 * @param boat
	 */
	public VehicleDeparture(L2BoatInstance boat)
	{
		_boat = boat;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0x5a);
		writeD(_boat.getObjectId());
		writeD((int) _boat.getStat().getMoveSpeed());
		writeD(_boat.getStat().getRotationSpeed());
		writeD(_boat.getXdestination());
		writeD(_boat.getYdestination());
		writeD(_boat.getZdestination());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "[S] 5A VehicleDeparture";
	}
}