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
package com.l2jserver.gameserver.network.serverpackets;

import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

public class EnchantResult extends L2GameServerPacket
{
	public static int SUCCESS = 0;
	public static int FAIL = 1;
	public static int ERROR = 2;
	public static int BLESSED_FAIL = 3;
	public static int NO_CRYSTAL = 4;
	public static int SAFE_FAIL = 5;
	private final int _result;
	private final int _crystal;
	private final int _count;
	private final int _enchantLevel;
	private final int[] _enchantOptions;
	
	public EnchantResult(int result, int crystal, int count, int enchantLevel, int[] options)
	{
		_result = result;
		_crystal = crystal;
		_count = count;
		_enchantLevel = enchantLevel;
		_enchantOptions = options;
	}
	
	public EnchantResult(int result, int crystal, int count)
	{
		this(result, crystal, count, 0, L2ItemInstance.DEFAULT_ENCHANT_OPTIONS);
	}
	
	public EnchantResult(int result, L2ItemInstance item)
	{
		this(result, 0, 0, item.getEnchantLevel(), item.getEnchantOptions());
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x87);
		writeD(_result);
		writeD(_crystal);
		writeQ(_count);
		writeD(_enchantLevel);
		for (int option : _enchantOptions)
		{
			writeH(option);
		}
	}
}
