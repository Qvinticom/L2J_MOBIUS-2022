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

import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Clan.RankPrivs;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final L2Clan _clan;
	
	public PledgePowerGradeList(L2Clan clan)
	{
		_clan = clan;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x3D);
		writeD(_clan.getAllRankPrivs().length);
		for (RankPrivs temp : _clan.getAllRankPrivs())
		{
			writeD(temp.getRank());
			writeD(_clan.getPowerGradeMemberCount(temp.getRank()));
		}
	}
}
