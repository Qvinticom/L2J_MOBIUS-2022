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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.Clan.SubPledge;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw
 */
public class ExPledgeRecruitInfo implements IClientOutgoingPacket
{
	private final Clan _clan;
	
	public ExPledgeRecruitInfo(int clanId)
	{
		_clan = ClanTable.getInstance().getClan(clanId);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_RECRUIT_INFO.writeId(packet);
		final Collection<SubPledge> subPledges = _clan.getAllSubPledges();
		packet.writeS(_clan.getName());
		packet.writeS(_clan.getLeaderName());
		packet.writeD(_clan.getLevel());
		packet.writeD(_clan.getMembersCount());
		packet.writeD(subPledges.size());
		for (SubPledge subPledge : subPledges)
		{
			packet.writeD(subPledge.getId());
			packet.writeS(subPledge.getName());
		}
		return true;
	}
}
