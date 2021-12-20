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
import org.l2jmobius.gameserver.model.clan.Clan.SubPledge;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author -Wooden-
 */
public class PledgeReceiveSubPledgeCreated implements IClientOutgoingPacket
{
	private final SubPledge _subPledge;
	
	public PledgeReceiveSubPledgeCreated(SubPledge subPledge)
	{
		_subPledge = subPledge;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_RECEIVE_SUB_PLEDGE_CREATED.writeId(packet);
		packet.writeD(1);
		packet.writeD(_subPledge.getId());
		packet.writeS(_subPledge.getName());
		packet.writeS(_subPledge.getLeaderName());
		return true;
	}
}
