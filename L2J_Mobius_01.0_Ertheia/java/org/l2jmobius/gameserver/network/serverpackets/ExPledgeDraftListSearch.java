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

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.clan.entry.PledgeWaitingInfo;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw
 */
public class ExPledgeDraftListSearch implements IClientOutgoingPacket
{
	final List<PledgeWaitingInfo> _pledgeRecruitList;
	
	public ExPledgeDraftListSearch(List<PledgeWaitingInfo> pledgeRecruitList)
	{
		_pledgeRecruitList = pledgeRecruitList;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_DRAFT_LIST_SEARCH.writeId(packet);
		packet.writeD(_pledgeRecruitList.size());
		for (PledgeWaitingInfo prl : _pledgeRecruitList)
		{
			packet.writeD(prl.getPlayerId());
			packet.writeS(prl.getPlayerName());
			packet.writeD(prl.getKarma());
			packet.writeD(prl.getPlayerClassId());
			packet.writeD(prl.getPlayerLvl());
		}
		return true;
	}
}
