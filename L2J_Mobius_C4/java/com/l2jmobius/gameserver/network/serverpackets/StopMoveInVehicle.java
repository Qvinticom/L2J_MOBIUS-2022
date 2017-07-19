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

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.util.Point3D;

/**
 * @author Maktakien
 */
public class StopMoveInVehicle extends L2GameServerPacket
{
	private final int _charObjId;
	private final int _boatId;
	private final Point3D _pos;
	private final int _heading;
	
	public StopMoveInVehicle(L2PcInstance player, int boatId)
	{
		_charObjId = player.getObjectId();
		_boatId = boatId;
		_pos = player.getInBoatPosition();
		_heading = player.getHeading();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#writeImpl()
	 */
	@Override
	protected final void writeImpl()
	{
		writeC(0x72);
		writeD(_charObjId);
		writeD(_boatId);
		writeD(_pos.getX());
		writeD(_pos.getY());
		writeD(_pos.getZ());
		writeD(_heading);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "[S] 72 StopMoveInVehicle";
	}
}