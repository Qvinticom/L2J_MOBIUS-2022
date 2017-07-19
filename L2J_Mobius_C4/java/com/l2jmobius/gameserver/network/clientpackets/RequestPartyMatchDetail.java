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

import com.l2jmobius.gameserver.model.PartyMatchRoom;
import com.l2jmobius.gameserver.model.PartyMatchRoomList;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @athor Gnacik
 */
public class RequestPartyMatchDetail extends L2GameClientPacket
{
	private static final String _C__71_REQUESTPARTYMATCHDETAIL = "[C] 71 RequestPartyMatchDetail";
	
	private int _roomId;
	@SuppressWarnings("unused")
	private int _unk1;
	
	@Override
	protected void readImpl()
	{
		_roomId = readD();
		_unk1 = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(_roomId);
		if (_room == null)
		{
			return;
		}
		
		if ((activeChar.getParty() == null) && (activeChar.getLevel() >= _room.getMinLvl()) && (activeChar.getLevel() <= _room.getMaxLvl()) && (_room.getPartyMembers().size() < _room.getMaxMembers()))
		{
			_room.addMember(activeChar);
			activeChar.setPartyRoom(_roomId);
			
			activeChar.sendPacket(new PartyMatchDetail(_room));
			activeChar.sendPacket(new ExPartyRoomMember(activeChar, _room, 0));
			
			for (final L2PcInstance _member : _room.getPartyMembers())
			{
				if ((_member == null) || (_member == activeChar))
				{
					continue;
				}
				
				_member.sendPacket(new ExManagePartyRoomMember(activeChar, _room, 0));
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_ENTER_PARTY_ROOM));
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__71_REQUESTPARTYMATCHDETAIL;
	}
}