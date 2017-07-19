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

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance.TimeStamp;

/**
 * @author KenM
 */
public class SkillCoolTime extends L2GameServerPacket
{
	private final L2PcInstance _cha;
	
	public SkillCoolTime(L2PcInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xc1);
		writeD(_cha.getReuseTimeStamps().size()); // list size
		for (final TimeStamp ts : _cha.getReuseTimeStamps())
		{
			writeD(ts.getSkillId());
			
			writeD(_cha.getSkillLevel(ts.getSkillId()));
			writeD((int) ts.getReuse() / 1000);
			writeD((int) ts.getRemaining() / 1000);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return "[S] C1 SkillCoolTime";
	}
}