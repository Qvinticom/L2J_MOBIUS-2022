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

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class PartySmallWindowUpdate extends ServerBasePacket
{
	private static final String _S__67_PARTYSMALLWINDOWUPDATE = "[S] 67 PartySmallWindowUpdate";
	private final PlayerInstance _member;
	
	public PartySmallWindowUpdate(PlayerInstance member)
	{
		_member = member;
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(103);
		writeD(_member.getObjectId());
		writeS(_member.getName());
		writeD((int) _member.getCurrentHp());
		writeD(_member.getMaxHp());
		writeD((int) _member.getCurrentMp());
		writeD(_member.getMaxMp());
		writeD(_member.getClassId());
		writeD(_member.getLevel());
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__67_PARTYSMALLWINDOWUPDATE;
	}
}
