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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PartySmallWindowAll extends ServerBasePacket
{
	private static final String _S__63_PARTYSMALLWINDOWALL = "[S] 63 PartySmallWindowAll";
	private List<PlayerInstance> _partyMembers = new ArrayList<>();
	
	public void setPartyList(List<PlayerInstance> party)
	{
		_partyMembers = party;
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(99);
		writeD(_partyMembers.size());
		for (int i = 0; i < _partyMembers.size(); ++i)
		{
			final PlayerInstance member = _partyMembers.get(i);
			writeD(member.getObjectId());
			writeS(member.getName());
			writeD((int) member.getCurrentHp());
			writeD(member.getMaxHp());
			writeD((int) member.getCurrentMp());
			writeD(member.getMaxMp());
			writeD(member.getClassId());
			writeD(member.getLevel());
			writeD(0);
			writeD(0);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__63_PARTYSMALLWINDOWALL;
	}
}
