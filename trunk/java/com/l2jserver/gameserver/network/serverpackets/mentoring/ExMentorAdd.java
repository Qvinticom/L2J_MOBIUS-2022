/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets.mentoring;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Gnacik, UnAfraid
 */
public class ExMentorAdd extends L2GameServerPacket
{
	final L2PcInstance _mentor;
	
	public ExMentorAdd(L2PcInstance mentor)
	{
		_mentor = mentor;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x11C);
		writeS(_mentor.getName());
		writeD(_mentor.getActiveClass());
		writeD(_mentor.getLevel());
	}
}
