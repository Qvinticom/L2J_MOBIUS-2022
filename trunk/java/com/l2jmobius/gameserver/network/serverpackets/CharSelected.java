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

import com.l2jmobius.gameserver.GameTimeController;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

public class CharSelected extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _sessionId;
	
	/**
	 * @param cha
	 * @param sessionId
	 */
	public CharSelected(L2PcInstance cha, int sessionId)
	{
		_activeChar = cha;
		_sessionId = sessionId;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x0B);
		
		writeS(_activeChar.getName());
		writeD(_activeChar.getObjectId());
		writeS(_activeChar.getTitle());
		writeD(_sessionId);
		writeD(_activeChar.getClanId());
		writeD(0x00); // ??
		writeD(_activeChar.getAppearance().getSex() ? 1 : 0);
		writeD(_activeChar.getRace().ordinal());
		writeD(_activeChar.getClassId().getId());
		writeD(0x01); // active ??
		writeD(_activeChar.getX());
		writeD(_activeChar.getY());
		writeD(_activeChar.getZ());
		writeF(_activeChar.getCurrentHp());
		writeF(_activeChar.getCurrentMp());
		writeQ(_activeChar.getSp());
		writeQ(_activeChar.getExp());
		writeD(_activeChar.getLevel());
		writeD(_activeChar.getReputation());
		writeD(_activeChar.getPkKills());
		writeD(GameTimeController.getInstance().getGameTime() % (24 * 60)); // "reset" on 24th hour
		writeD(0x00);
		writeD(_activeChar.getClassId().getId());
		
		writeB(new byte[16]);
		
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		
		writeD(0x00);
		
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(0x00);
		
		writeB(new byte[28]);
		writeD(0x00);
	}
}
