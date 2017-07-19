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
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import com.l2jmobius.gameserver.network.serverpackets.PartyMatchList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.4.2 $ $Date: 2005/03/27 15:29:30 $
 */
public class RequestPartyMatchConfig extends L2GameClientPacket
{
	private static final String _C__6F_REQUESTPARTYMATCHCONFIG = "[C] 6F RequestPartyMatchConfig";
	
	private int _auto, _loc, _lvl;
	
	@Override
	protected void readImpl()
	{
		_auto = readD();
		_loc = readD(); // Location
		_lvl = readD(); // my level
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (!activeChar.isInPartyMatchRoom() && (activeChar.getParty() != null) && (activeChar.getParty().getPartyMembers().get(0) != activeChar))
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.CANT_VIEW_PARTY_ROOMS));
			activeChar.sendPacket(new ActionFailed());
			return;
		}
		
		if (activeChar.isInPartyMatchRoom())
		{
			// If Player is in Room show him room, not list
			final PartyMatchRoomList _list = PartyMatchRoomList.getInstance();
			if (_list == null)
			{
				return;
			}
			
			final PartyMatchRoom _room = _list.getPlayerRoom(activeChar);
			if (_room == null)
			{
				return;
			}
			
			activeChar.sendPacket(new PartyMatchDetail(_room));
			
			if (activeChar == _room.getOwner())
			{
				activeChar.sendPacket(new ExPartyRoomMember(activeChar, _room, 1));
			}
			else
			{
				activeChar.sendPacket(new ExPartyRoomMember(activeChar, _room, 2));
			}
			
			activeChar.setPartyRoom(_room.getId());
			activeChar.broadcastUserInfo();
		}
		else
		{
			// Send Room list
			final PartyMatchList matchList = new PartyMatchList(activeChar, _auto, _loc, _lvl);
			
			activeChar.sendPacket(matchList);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__6F_REQUESTPARTYMATCHCONFIG;
	}
}