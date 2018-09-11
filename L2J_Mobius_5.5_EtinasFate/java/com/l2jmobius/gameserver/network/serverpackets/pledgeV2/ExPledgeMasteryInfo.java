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
package com.l2jmobius.gameserver.network.serverpackets.pledgeV2;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;
import com.l2jmobius.gameserver.network.serverpackets.AbstractItemPacket;

/**
 * @author Mobius
 */
public class ExPledgeMasteryInfo extends AbstractItemPacket
{
	final L2PcInstance _player;
	
	public ExPledgeMasteryInfo(L2PcInstance player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player.getClan() == null)
		{
			return false;
		}
		
		OutgoingPackets.EX_PLEDGE_MASTERY_INFO.writeId(packet);
		
		packet.writeD(10); // Consumed development points
		packet.writeD(10); // Total development points
		
		packet.writeD(1); // Masteries count
		
		packet.writeD(10); // Mastery
		packet.writeD(1); // Purchased
		packet.writeC(1); // Availability
		
		return true;
	}
}
