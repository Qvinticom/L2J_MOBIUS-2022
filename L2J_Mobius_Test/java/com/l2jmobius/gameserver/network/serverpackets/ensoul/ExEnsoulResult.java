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
package com.l2jmobius.gameserver.network.serverpackets.ensoul;

import com.l2jmobius.gameserver.network.clientpackets.ensoul.SoulCrystalOption;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Mathael
 */
public class ExEnsoulResult extends L2GameServerPacket
{
	public static final ExEnsoulResult FAILED = new ExEnsoulResult();
	
	private final int _result;
	private final SoulCrystalOption[] _commons;
	private final SoulCrystalOption _special;
	
	public ExEnsoulResult()
	{
		_result = 0;
		_commons = null;
		_special = null;
	}
	
	public ExEnsoulResult(int result, SoulCrystalOption[] commons, SoulCrystalOption special)
	{
		_result = result;
		_commons = commons;
		_special = special;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x17F);
		
		// Success (Yes or No)
		writeC(_result);
		
		// Primary special abilities
		int count = 0;
		for (SoulCrystalOption sc : _commons)
		{
			if (sc != null)
			{
				count++;
			}
		}
		
		writeC(count);
		for (SoulCrystalOption sc : _commons)
		{
			if (sc != null)
			{
				writeD(sc.getEffect());
			}
		}
		
		// Secondary special abilities
		writeC(_special != null);
		if (_special != null)
		{
			writeD(_special.getEffect());
		}
	}
}
