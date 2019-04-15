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

import org.l2jmobius.gameserver.model.actor.instance.BoatInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

/**
 * @author Maktakien
 */
public class GetOffVehicle extends GameServerPacket
{
	private final int _x;
	private final int _y;
	private final int _z;
	private final PlayerInstance _player;
	private final BoatInstance _boat;
	
	/**
	 * @param player
	 * @param boat
	 * @param x
	 * @param y
	 * @param z
	 */
	public GetOffVehicle(PlayerInstance player, BoatInstance boat, int x, int y, int z)
	{
		_player = player;
		_boat = boat;
		_x = x;
		_y = y;
		_z = z;
		
		if (_player != null)
		{
			_player.setInBoat(false);
			_player.setBoat(null);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.l2jmobius.gameserver.serverpackets.ServerBasePacket#writeImpl()
	 */
	@Override
	protected void writeImpl()
	{
		if ((_boat == null) || (_player == null))
		{
			return;
		}
		
		writeC(0x5d);
		writeD(_player.getObjectId());
		writeD(_boat.getObjectId());
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
