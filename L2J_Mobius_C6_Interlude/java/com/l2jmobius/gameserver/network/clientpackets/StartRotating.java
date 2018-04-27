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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.network.serverpackets.BeginRotation;

public final class StartRotating extends L2GameClientPacket
{
	private int _degree;
	private int _side;
	
	@Override
	protected void readImpl()
	{
		_degree = readD();
		_side = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (!Config.ENABLE_KEYBOARD_MOVEMENT)
		{
			return;
		}
		
		if (getClient().getActiveChar() == null)
		{
			return;
		}
		
		final BeginRotation br = new BeginRotation(getClient().getActiveChar(), _degree, _side, 0);
		getClient().getActiveChar().broadcastPacket(br);
	}
}
