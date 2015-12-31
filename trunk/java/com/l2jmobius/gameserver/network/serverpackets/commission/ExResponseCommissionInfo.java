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
public class ExResponseCommissionInfo extends L2GameServerPacket
{
	public static final ExResponseCommissionInfo EMPTY = new ExResponseCommissionInfo();
	
	private final int _result;
	private final int _itemId;
	private final long _presetPricePerUnit;
	private final long _presetAmount;
	private final int _presetDurationType;
	
	private ExResponseCommissionInfo()
	{
		_result = 0;
		_itemId = 0;
		_presetPricePerUnit = 0;
		_presetAmount = 0;
		_presetDurationType = -1;
	}
	
	public ExResponseCommissionInfo(int itemId, long presetPricePerUnit, long presetAmount, int presetDurationType)
	{
		_result = 1;
		_itemId = itemId;
		_presetPricePerUnit = presetPricePerUnit;
		_presetAmount = presetAmount;
		_presetDurationType = presetDurationType;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF4);
		writeD(_result);
		writeD(_itemId);
		writeQ(_presetPricePerUnit);
		writeQ(_presetAmount);
		writeD(_presetDurationType);
	}
}
