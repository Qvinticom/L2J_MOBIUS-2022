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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.instancemanager.ClanEntryManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ExPledgeRecruitBoardSearch;

/**
 * @author Sdw
 */
public class RequestPledgeRecruitBoardSearch extends L2GameClientPacket
{
	private static final String _C__D0_D4_REQUESTPLEDGERECRUITBOARDSEARCH = "[C] D0;D4 RequestPledgeRecruitBoardSearch";
	
	private int _clanLevel;
	private int _karma;
	private int _type;
	private String _query;
	private int _sort;
	private boolean _descending;
	private int _page;
	
	@Override
	protected void readImpl()
	{
		_clanLevel = readD();
		_karma = readD();
		_type = readD();
		_query = readS();
		_sort = readD();
		_descending = readD() == 2;
		_page = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		if (_query.isEmpty())
		{
			if ((_karma < 0) && (_clanLevel < 0))
			{
				activeChar.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getUnSortedClanList(), _page));
			}
			else
			{
				activeChar.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getSortedClanList(_clanLevel, _karma, _sort, _descending), _page));
			}
		}
		else
		{
			activeChar.sendPacket(new ExPledgeRecruitBoardSearch(ClanEntryManager.getInstance().getSortedClanListByName(_query.toLowerCase(), _type), _page));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_D4_REQUESTPLEDGERECRUITBOARDSEARCH;
	}
}
