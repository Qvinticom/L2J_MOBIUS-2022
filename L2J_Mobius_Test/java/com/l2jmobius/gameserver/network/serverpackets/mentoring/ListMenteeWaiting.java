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
package com.l2jmobius.gameserver.network.serverpackets.mentoring;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author UnAfraid
 */
public class ListMenteeWaiting extends L2GameServerPacket
{
	private final int PLAYERS_PER_PAGE = 64;
	private final List<L2PcInstance> _possibleCandiates = new ArrayList<>();
	private final int _page;
	
	public ListMenteeWaiting(int page, int minLevel, int maxLevel)
	{
		_page = page;
		for (L2PcInstance player : L2World.getInstance().getPlayers())
		{
			if ((player.getLevel() >= minLevel) && (player.getLevel() <= maxLevel) && !player.isMentee() && !player.isMentor() && !player.isInCategory(CategoryType.AWAKEN_GROUP))
			{
				_possibleCandiates.add(player);
			}
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x11D);
		
		writeD(0x01); // always 1 in retail
		if (_possibleCandiates.isEmpty())
		{
			writeD(0x00);
			writeD(0x00);
			return;
		}
		
		writeD(_possibleCandiates.size());
		writeD(_possibleCandiates.size() % PLAYERS_PER_PAGE);
		
		for (L2PcInstance player : _possibleCandiates)
		{
			if ((1 <= (PLAYERS_PER_PAGE * _page)) && (1 > (PLAYERS_PER_PAGE * (_page - 1))))
			{
				writeS(player.getName());
				writeD(player.getActiveClassId());
				writeD(player.getLevel());
			}
		}
	}
}
