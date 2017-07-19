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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.datatables.MapRegionTable;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * format (ch) d
 * @author -Wooden-
 */
public class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private static final String _C__D0_01_REQUESTOUSTFROMPARTYROOM = "[C] D0:01 RequestOustFromPartyRoom";
	
	private int _charId;
	
	@Override
	protected void readImpl()
	{
		_charId = readD();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#runImpl()
	 */
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2Object object = L2World.getInstance().findObject(_charId);
		if (object == null)
		{
			return;
		}
		
		if (!(object instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance member = (L2PcInstance) object;
		
		final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getPlayerRoom(member);
		if (_room == null)
		{
			return;
		}
		
		if (_room.getOwner() != activeChar)
		{
			return;
		}
		
		if (_room.getOwner() == member)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
			return;
		}
		
		if (activeChar.isInParty() && member.isInParty() && (activeChar.getParty().getPartyLeaderOID() == member.getParty().getPartyLeaderOID()))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISMISS_PARTY_MEMBER));
		}
		else
		{
			// Remove member from party room
			_room.deleteMember(member);
			member.setPartyRoom(0);
			
			// Close the PartyRoom window
			member.sendPacket(new ExClosePartyRoom());
			
			// Send Room list
			final int loc = MapRegionTable.getInstance().getClosestTownNumber(member);
			member.sendPacket(new PartyMatchList(member, 0, loc, member.getLevel()));
			
			// Clean Looking for Party title
			member.broadcastUserInfo();
			member.sendPacket(new SystemMessage(SystemMessage.OUSTED_FROM_PARTY_ROOM));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.BasePacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__D0_01_REQUESTOUSTFROMPARTYROOM;
	}
}