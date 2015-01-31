/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets.auctionhouse;

import com.l2jserver.gameserver.instancemanager.AuctionHouseManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.auctionhouse.ExResponseCommissionList;

/**
 * @author Erlandys
 */
public final class RequestCommissionList extends L2GameClientPacket
{
	private static final String _C__D0_A0_REQUESTCOMMISSIONLIST = "[C] D0:A0 RequestCommissionList";
	
	private long _category;
	private int _type;
	private int _grade;
	private String _searchName;
	
	@Override
	protected void readImpl()
	{
		_category = readQ();
		_type = readD();
		_grade = readD();
		_searchName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		AuctionHouseManager am = AuctionHouseManager.getInstance();
		
		if (_category == 0)
		{
			_category = 100;
		}
		else if (_category == 1)
		{
			_category = 101;
		}
		
		if ((_category != 101) && (_category != 100) && ((_category % 10000) != 7297) && ((_category % 10000) != 4593) && ((_category % 10000) != 1889) && ((_category % 10000) != 9185) && ((_category % 10000) != 6481))
		{
			_category = am.getClientCategory((int) (_category / 1000));
		}
		else if ((_category != 101) && (_category != 100))
		{
			_category = am.getMainClientCategory((int) (_category / 1000));
		}
		
		if (((_category > 60) && (_category < 66)) || (_category == 101))
		{
			if (am.getAuctionsSizeById(_category, _grade, _searchName) > 999)
			{
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEEDED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT_PLEASE_SEARCH_BY_SELECTING_DETAILED_CATEGORY);
			}
			else if (am.getAuctionsSizeById(_category, _grade, _searchName) <= 0)
			{
				activeChar.sendPacket(SystemMessageId.CURRENTLY_THERE_ARE_NO_REGISTERED_ITEMS);
			}
		}
		else if (_category == 100)
		{
			if (am.getAuctionsSizeById(_grade, _searchName) > 999)
			{
				activeChar.sendPacket(SystemMessageId.THE_SEARCH_RESULT_EXCEEDED_THE_MAXIMUM_ALLOWED_RANGE_FOR_OUTPUT_PLEASE_SEARCH_BY_SELECTING_DETAILED_CATEGORY);
			}
		}
		
		am.checkForAuctionsDeletion();
		activeChar.sendPacket(new ExResponseCommissionList(activeChar, _category, _type, _grade, _searchName));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_A0_REQUESTCOMMISSIONLIST;
	}
}
