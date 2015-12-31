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
 * @author Sdw
 */
public class ExResponseBeautyRegistReset extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _type;
	private final int _result;
	
	public final static int FAILURE = 0;
	public final static int SUCCESS = 1;
	
	public final static int CHANGE = 0;
	public final static int RESTORE = 1;
	
	public ExResponseBeautyRegistReset(L2PcInstance activeChar, int type, int result)
	{
		_activeChar = activeChar;
		_type = type;
		_result = result;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x136);
		
		writeQ(_activeChar.getAdena());
		writeQ(_activeChar.getBeautyTickets());
		writeD(_type);
		writeD(_result);
		writeD(_activeChar.getVisualHair());
		writeD(_activeChar.getVisualFace());
		writeD(_activeChar.getVisualHairColor());
	}
}
