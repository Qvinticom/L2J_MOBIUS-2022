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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.actor.stat.PcStat;
import com.l2jmobius.gameserver.model.variables.AccountVariables;
import com.l2jmobius.gameserver.network.L2GameClient;

/**
 * @author Sdw
 */
public class ExLoginVitalityEffectInfo extends L2GameServerPacket
{
	private final int _points;
	
	public ExLoginVitalityEffectInfo(L2GameClient client)
	{
		final AccountVariables vars = new AccountVariables(client.getAccountName());
		_points = vars.getInt(PcStat.VITALITY_VARIABLE, Config.STARTING_VITALITY_POINTS);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0x119);
		
		writeD((int) (Config.RATE_VITALITY_EXP_MULTIPLIER * 100));
		writeD(5); // Remaining item counts
		
		writeD(_points);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}
