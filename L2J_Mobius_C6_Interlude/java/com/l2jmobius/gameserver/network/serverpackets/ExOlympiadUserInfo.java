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

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author godson
 */
public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private final int _side;
	private final L2PcInstance _activeChar;
	
	/**
	 * @param player
	 * @param side (1 = right, 2 = left)
	 */
	public ExOlympiadUserInfo(L2PcInstance player, int side)
	{
		_activeChar = player;
		_side = side;
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_activeChar == null)
		{
			return;
		}
		writeC(0xfe);
		writeH(0x29);
		writeC(_side);
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getName());
		writeD(_activeChar.getClassId().getId());
		writeD((int) _activeChar.getCurrentHp());
		writeD(_activeChar.getMaxHp());
		writeD((int) _activeChar.getCurrentCp());
		writeD(_activeChar.getMaxCp());
	}
}
