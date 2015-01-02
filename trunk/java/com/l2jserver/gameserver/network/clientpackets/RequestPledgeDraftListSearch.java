/*
 * Copyright (C) 2004-2015 L2J Server
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
import com.l2jserver.gameserver.network.serverpackets.ExPledgeDraftListSearch;
import com.l2jserver.gameserver.util.Util;

/**
 * @author Sdw
 */
public class RequestPledgeDraftListSearch extends L2GameClientPacket
{
	private static final String _C__D0_DC_REQUESTPLEDGEDRAFTLISTSEARCH = "[C] D0;DC RequestPledgeDraftListSearch";
	
	private int _levelMin;
	private int _levelMax;
	private int _classId;
	private String _query;
	private int _sortBy;
	private boolean _descending;
	
	@Override
	protected void readImpl()
	{
		_levelMin = Util.constrain(readD(), 0, 99);
		_levelMax = Util.constrain(readD(), 0, 99);
		_classId = readD();
		_query = readS();
		_sortBy = readD();
		_descending = readD() == 2;
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
			activeChar.sendPacket(new ExPledgeDraftListSearch(ClanEntryManager.getInstance().getSortedWaitingList(_levelMin, _levelMax, _classId, _sortBy, _descending)));
		}
		else
		{
			activeChar.sendPacket(new ExPledgeDraftListSearch(ClanEntryManager.getInstance().queryWaitingListByName(_query.toLowerCase())));
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_DC_REQUESTPLEDGEDRAFTLISTSEARCH;
	}
	
}
