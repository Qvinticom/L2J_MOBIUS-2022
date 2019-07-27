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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

/**
 * Format: ch ddcdc.
 * @author KenM
 * @author ProGramMoS
 */
public class ExPCCafePointInfo extends GameServerPacket
{
	/** The _character. */
	private final PlayerInstance _player;
	
	/** The m_ add point. */
	private final int m_AddPoint;
	
	/** The m_ period type. */
	private int m_PeriodType;
	
	/** The Remain time. */
	private final int RemainTime;
	
	/** The Point type. */
	private int PointType;
	
	/**
	 * Instantiates a new ex pc cafe point info.
	 * @param user the user
	 * @param modify the modify
	 * @param add the add
	 * @param hour the hour
	 * @param _double the _double
	 */
	public ExPCCafePointInfo(PlayerInstance user, int modify, boolean add, int hour, boolean _double)
	{
		_player = user;
		m_AddPoint = modify;
		
		if (add)
		{
			m_PeriodType = 1;
			PointType = 1;
		}
		else if (add && _double)
		{
			m_PeriodType = 1;
			PointType = 0;
		}
		else
		{
			m_PeriodType = 2;
			PointType = 2;
		}
		
		RemainTime = hour;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x31);
		writeD(_player.getPcBangScore());
		writeD(m_AddPoint);
		writeC(m_PeriodType);
		writeD(RemainTime);
		writeC(PointType);
	}
}
