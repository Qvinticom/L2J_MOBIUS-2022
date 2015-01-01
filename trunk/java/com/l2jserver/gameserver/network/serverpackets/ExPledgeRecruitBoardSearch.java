/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

import java.util.List;

import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.clan.entry.PledgeRecruitInfo;

/**
 * @author Sdw
 */
public class ExPledgeRecruitBoardSearch extends L2GameServerPacket
{
	final List<PledgeRecruitInfo> _clanList;
	final private int _currentPage;
	final private int _totalNumberOfPage;
	final private int _clanOnCurrentPage;
	final private int _startIndex;
	final private int _endIndex;
	
	final static int CLAN_PER_PAGE = 12;
	
	public ExPledgeRecruitBoardSearch(List<PledgeRecruitInfo> clanList, int currentPage)
	{
		_clanList = clanList;
		_currentPage = currentPage;
		_totalNumberOfPage = (int) Math.ceil(_clanList.size() / CLAN_PER_PAGE);
		_startIndex = (_currentPage - 1) * CLAN_PER_PAGE;
		_endIndex = (_startIndex + CLAN_PER_PAGE) > _clanList.size() ? _clanList.size() : _startIndex + CLAN_PER_PAGE;
		_clanOnCurrentPage = _endIndex - _startIndex;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x141);
		
		writeD(_currentPage);
		writeD(_totalNumberOfPage);
		writeD(_clanOnCurrentPage);
		
		for (int i = _startIndex; i < _endIndex; i++)
		{
			writeD(_clanList.get(i).getClanId());
			writeD(0x00); // find me
		}
		for (int i = _startIndex; i < _endIndex; i++)
		{
			final L2Clan clan = _clanList.get(i).getClan();
			writeD(clan.getAllyCrestId());
			writeD(clan.getCrestId());
			writeS(clan.getName());
			writeS(clan.getLeaderName());
			writeD(clan.getLevel());
			writeD(clan.getMembersCount());
			writeD(_clanList.get(i).getKarma());
			writeS(_clanList.get(i).getInformation());
		}
	}
}
