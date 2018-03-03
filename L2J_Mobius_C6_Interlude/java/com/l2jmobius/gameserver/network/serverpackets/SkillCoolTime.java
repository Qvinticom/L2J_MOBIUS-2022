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

import java.util.Collection;
import java.util.Iterator;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

public class SkillCoolTime extends L2GameServerPacket
{
	@SuppressWarnings("rawtypes")
	public Collection _reuseTimeStamps;
	
	public SkillCoolTime(L2PcInstance cha)
	{
		_reuseTimeStamps = cha.getReuseTimeStamps();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected final void writeImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		writeC(193);
		writeD(_reuseTimeStamps.size());
		L2PcInstance.TimeStamp ts;
		for (Iterator i$ = _reuseTimeStamps.iterator(); i$.hasNext(); writeD((int) ts.getRemaining() / 1000))
		{
			ts = (L2PcInstance.TimeStamp) i$.next();
			writeD(ts.getSkill().getId());
			writeD(0);
			writeD((int) ts.getReuse() / 1000);
		}
	}
}