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
package com.l2jserver.gameserver.model.clan.entry;

import com.l2jserver.gameserver.datatables.ClanTable;
import com.l2jserver.gameserver.model.L2Clan;

/**
 * @author Sdw
 */
public class PledgeRecruitInfo
{
	private int _clanId;
	private int _karma;
	private String _information;
	private String _detailedInformation;
	private final L2Clan _clan;
	
	public PledgeRecruitInfo(int clanId, int karma, String information, String detailedInformation)
	{
		_clanId = clanId;
		_karma = karma;
		_information = information;
		_detailedInformation = detailedInformation;
		_clan = ClanTable.getInstance().getClan(clanId);
	}
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public String getClanName()
	{
		return _clan.getName();
	}
	
	public String getClanLeaderName()
	{
		return _clan.getLeaderName();
	}
	
	public int getClanLevel()
	{
		return _clan.getLevel();
	}
	
	public int getKarma()
	{
		return _karma;
	}
	
	public void setKarma(int karma)
	{
		_karma = karma;
	}
	
	public String getInformation()
	{
		return _information;
	}
	
	public void setInformation(String information)
	{
		_information = information;
	}
	
	public String getDetailedInformation()
	{
		return _detailedInformation;
	}
	
	public void setDetailedInformation(String detailedInformation)
	{
		_detailedInformation = detailedInformation;
	}
	
	public L2Clan getClan()
	{
		return _clan;
	}
}
