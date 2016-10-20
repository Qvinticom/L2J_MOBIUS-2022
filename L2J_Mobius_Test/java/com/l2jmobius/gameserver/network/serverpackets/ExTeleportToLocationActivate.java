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

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;

/**
 * @author UnAfraid
 */
public class ExTeleportToLocationActivate extends L2GameServerPacket
{
	private final int _objectId;
	private final Location _loc;
	
	public ExTeleportToLocationActivate(L2Character character)
	{
		_objectId = character.getObjectId();
		_loc = character.getLocation();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x14A);
		writeD(_objectId);
		writeLoc(_loc);
		writeD(_loc.getInstanceId());
		writeD(_loc.getHeading());
		writeD(0); // Unknown
	}
}