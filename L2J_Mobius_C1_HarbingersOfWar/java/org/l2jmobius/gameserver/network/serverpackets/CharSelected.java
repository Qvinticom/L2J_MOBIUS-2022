/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.GameTimeController;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class CharSelected extends ServerBasePacket
{
	private static final String _S__21_CHARSELECTED = "[S] 21 CharSelected";
	private final PlayerInstance _cha;
	private final int _sessionId;
	
	public CharSelected(PlayerInstance cha, int sessionId)
	{
		_cha = cha;
		_sessionId = sessionId;
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(33);
		writeS(_cha.getName());
		writeD(_cha.getCharId());
		writeS(_cha.getTitle());
		writeD(_sessionId);
		writeD(_cha.getClanId());
		writeD(0);
		writeD(_cha.getSex());
		writeD(_cha.getRace());
		writeD(_cha.getClassId());
		writeD(1);
		writeD(_cha.getX());
		writeD(_cha.getY());
		writeD(_cha.getZ());
		writeF(_cha.getCurrentHp());
		writeF(_cha.getCurrentMp());
		writeD(_cha.getSp());
		writeD(_cha.getExp());
		writeD(_cha.getLevel());
		writeD(0);
		writeD(0);
		writeD(_cha.getInt());
		writeD(_cha.getStr());
		writeD(_cha.getCon());
		writeD(_cha.getMen());
		writeD(_cha.getDex());
		writeD(_cha.getWit());
		for (int i = 0; i < 30; ++i)
		{
			writeD(0);
		}
		writeD(GameTimeController.getInstance().getGameTime());
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__21_CHARSELECTED;
	}
}
