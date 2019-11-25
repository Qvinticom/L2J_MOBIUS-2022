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

import org.l2jmobius.gameserver.model.Clan;
import org.l2jmobius.gameserver.model.ClanMember;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PledgeShowMemberListAll extends ServerBasePacket
{
	private final Clan _clan;
	private final PlayerInstance _activeChar;
	
	public PledgeShowMemberListAll(Clan clan, PlayerInstance activeChar)
	{
		_clan = clan;
		_activeChar = activeChar;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x68);
		writeD(_clan.getClanId());
		writeS(_clan.getName());
		writeS(_clan.getLeaderName());
		writeD(0);
		writeD(_clan.getLevel());
		writeD(_clan.getHasCastle());
		writeD(_clan.getHasHideout());
		writeD(0);
		writeD(_activeChar.getLevel());
		writeD(0);
		writeD(0);
		writeD(0);
		writeS("");
		writeD(0);
		writeD(_clan.getMembers().size() - 1);
		for (ClanMember member : _clan.getMembers())
		{
			if (member.getName().equals(_activeChar.getName()))
			{
				continue;
			}
			writeS(member.getName());
			writeD(member.getLevel());
			writeD(member.getClassId());
			writeD(0);
			writeD(1);
			if (member.isOnline())
			{
				writeD(1);
				continue;
			}
			writeD(0);
		}
	}
}
