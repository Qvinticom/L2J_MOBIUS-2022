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

import com.l2jmobius.gameserver.model.actor.instance.HennaInstance;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public final class HennaInfo extends GameServerPacket
{
	private final PlayerInstance _player;
	private final HennaInstance[] _hennas = new HennaInstance[3];
	private final int _count;
	
	public HennaInfo(PlayerInstance player)
	{
		_player = player;
		
		int j = 0;
		for (int i = 0; i < 3; i++)
		{
			final HennaInstance h = _player.getHennas(i + 1);
			if (h != null)
			{
				_hennas[j++] = h;
			}
		}
		_count = j;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xe4);
		
		writeC(_player.getHennaStatINT()); // equip INT
		writeC(_player.getHennaStatSTR()); // equip STR
		writeC(_player.getHennaStatCON()); // equip CON
		writeC(_player.getHennaStatMEN()); // equip MEM
		writeC(_player.getHennaStatDEX()); // equip DEX
		writeC(_player.getHennaStatWIT()); // equip WIT
		
		writeD(3); // slots?
		
		writeD(_count); // size
		for (int i = 0; i < _count; i++)
		{
			writeD(_hennas[i].getSymbolId());
			writeD(0x01);
		}
	}
}
