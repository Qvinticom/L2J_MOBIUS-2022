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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.StopMoveInVehicle;
import com.l2jmobius.util.Point3D;

/**
 * @author Maktakien
 */
public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _heading;
	private int _boatid;
	
	@Override
	protected void readImpl()
	{
		_boatid = readD();
		_x = readD();
		_y = readD();
		_z = readD();
		_heading = readD();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	public void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.isInBoat())
		{
			if (player.getBoat().getObjectId() == _boatid)
			{
				player.setInBoatPosition(new Point3D(_x, _y, _z));
				player.getPosition().setHeading(_heading);
				final StopMoveInVehicle msg = new StopMoveInVehicle(player, _boatid);
				player.broadcastPacket(msg);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		// TODO Auto-generated method stub
		return "[C] 5D CannotMoveAnymoreInVehicle";
	}
}