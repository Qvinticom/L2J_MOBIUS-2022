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
package com.l2jmobius.gameserver.network.clientpackets.shuttle;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * @author UnAfraid
 */
public class RequestShuttleGetOff extends L2GameClientPacket
{
	private int _x;
	private int _y;
	private int _z;
	
	@Override
	protected void readImpl()
	{
		readD(); // charId
		_x = readD();
		_y = readD();
		_z = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getShuttle() != null)
		{
			activeChar.getShuttle().removePassenger(activeChar, _x, _y, _z);
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
