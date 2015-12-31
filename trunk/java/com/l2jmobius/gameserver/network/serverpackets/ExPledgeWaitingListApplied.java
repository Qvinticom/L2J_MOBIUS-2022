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

import com.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import com.l2jmobius.gameserver.model.clan.entry.PledgeApplicantInfo;
import com.l2jmobius.gameserver.model.clan.entry.PledgeRecruitInfo;

/**
 * @author Sdw
 */
public class ExPledgeWaitingListApplied extends L2GameServerPacket
{
	private final PledgeApplicantInfo _pledgePlayerRecruitInfo;
	private final PledgeRecruitInfo _pledgeRecruitInfo;
	
	public ExPledgeWaitingListApplied(int clanId, int playerId)
	{
		_pledgePlayerRecruitInfo = ClanEntryManager.getInstance().getPlayerApplication(clanId, playerId);
		_pledgeRecruitInfo = ClanEntryManager.getInstance().getClanById(clanId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x143);
		
		writeD(_pledgeRecruitInfo.getClan().getId());
		writeS(_pledgeRecruitInfo.getClan().getName());
		writeS(_pledgeRecruitInfo.getClan().getLeaderName());
		writeD(_pledgeRecruitInfo.getClan().getLevel());
		writeD(_pledgeRecruitInfo.getClan().getMembersCount());
		writeD(_pledgeRecruitInfo.getKarma());
		writeS(_pledgeRecruitInfo.getInformation());
		writeS(_pledgePlayerRecruitInfo.getMessage());
	}
}
