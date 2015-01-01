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
package com.l2jserver.gameserver.network.serverpackets;

public class ExAttributeEnchantResult extends L2GameServerPacket
{
	private final int _result;
	private final int _isWeapon;
	private final int _type;
	private final int _before;
	private final int _after;
	private final int _successCount;
	private final int _failedCount;
	
	public ExAttributeEnchantResult(int result, boolean isWeapon, int type, int before, int after, int successCount, int failedCount)
	{
		_result = result;
		_isWeapon = isWeapon ? 1 : 0;
		_type = type;
		_before = before;
		_after = after;
		_successCount = successCount;
		_failedCount = failedCount;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x62);
		
		writeD(_result);
		writeC(_isWeapon);
		writeH(_type);
		writeH(_before);
		writeH(_after);
		writeH(_successCount);
		writeH(_failedCount);
	}
}
