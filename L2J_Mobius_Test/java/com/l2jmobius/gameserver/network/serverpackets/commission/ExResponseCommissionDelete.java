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
package com.l2jmobius.gameserver.network.serverpackets.commission;

import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author NosBit
 */
public class ExResponseCommissionDelete extends L2GameServerPacket
{
	public static final ExResponseCommissionDelete SUCCEED = new ExResponseCommissionDelete(1);
	public static final ExResponseCommissionDelete FAILED = new ExResponseCommissionDelete(0);
	
	private final int _result;
	
	private ExResponseCommissionDelete(int result)
	{
		_result = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF6);
		writeD(_result);
	}
}
