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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author KenM
 */
public class ExPCCafePointInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _addPoint;
	private int _periodType;
	private final int _remainTime;
	private int _pointType;
	
	/**
	 * Instantiates a new ex pc cafe point info.
	 * @param user the user
	 * @param modify the modify
	 * @param add the add
	 * @param hour the hour
	 * @param value the double
	 */
	public ExPCCafePointInfo(Player user, int modify, boolean add, int hour, boolean value)
	{
		_player = user;
		_addPoint = modify;
		if (add)
		{
			_periodType = 1;
			_pointType = 1;
		}
		else if (add && value) // check first?
		{
			_periodType = 1;
			_pointType = 0;
		}
		else
		{
			_periodType = 2;
			_pointType = 2;
		}
		
		_remainTime = hour;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PC_CAFE_POINT_INFO.writeId(packet);
		packet.writeD(_player.getPcBangScore());
		packet.writeD(_addPoint);
		packet.writeC(_periodType);
		packet.writeD(_remainTime);
		packet.writeC(_pointType);
		return true;
	}
}
