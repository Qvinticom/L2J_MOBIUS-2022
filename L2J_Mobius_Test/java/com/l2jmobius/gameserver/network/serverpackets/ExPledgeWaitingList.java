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

import java.util.Map;

import com.l2jmobius.gameserver.instancemanager.ClanEntryManager;
import com.l2jmobius.gameserver.model.clan.entry.PledgeApplicantInfo;

/**
 * @author Sdw
 */
public class ExPledgeWaitingList extends L2GameServerPacket
{
	Map<Integer, PledgeApplicantInfo> pledgePlayerRecruitInfos;
	
	public ExPledgeWaitingList(int clanId)
	{
		pledgePlayerRecruitInfos = ClanEntryManager.getInstance().getApplicantListForClan(clanId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x144);
		
		writeD(pledgePlayerRecruitInfos.size());
		for (PledgeApplicantInfo recruitInfo : pledgePlayerRecruitInfos.values())
		{
			writeD(recruitInfo.getPlayerId());
			writeS(recruitInfo.getPlayerName());
			writeD(recruitInfo.getClassId());
			writeD(recruitInfo.getPlayerLvl());
		}
	}
}
