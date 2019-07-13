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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.PartyMatchRoom;
import org.l2jmobius.gameserver.model.PartyMatchRoomList;
import org.l2jmobius.gameserver.model.PartyMatchWaitingList;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExManagePartyRoomMember;
import org.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public final class RequestPartyMatchDetail implements IClientIncomingPacket
{
	private int _roomid;
	@SuppressWarnings("unused")
	private int _unk1;
	@SuppressWarnings("unused")
	private int _unk2;
	@SuppressWarnings("unused")
	private int _unk3;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_roomid = packet.readD();
		// If player click on Room all unk are 0
		// If player click AutoJoin values are -1 1 1
		_unk1 = packet.readD();
		_unk2 = packet.readD();
		_unk3 = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance _player = client.getPlayer();
		if (_player == null)
		{
			return;
		}
		
		final PartyMatchRoom _room = PartyMatchRoomList.getInstance().getRoom(_roomid);
		if (_room == null)
		{
			return;
		}
		
		if ((_player.getLevel() >= _room.getMinLvl()) && (_player.getLevel() <= _room.getMaxLvl()))
		{
			// Remove from waiting list
			PartyMatchWaitingList.getInstance().removePlayer(_player);
			
			_player.setPartyRoom(_roomid);
			
			_player.sendPacket(new PartyMatchDetail(_player, _room));
			_player.sendPacket(new ExPartyRoomMember(_player, _room, 0));
			
			for (PlayerInstance _member : _room.getPartyMembers())
			{
				if (_member == null)
				{
					continue;
				}
				
				_member.sendPacket(new ExManagePartyRoomMember(_player, _room, 0));
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_HAS_ENTERED_THE_PARTY_ROOM);
				sm.addString(_player.getName());
				_member.sendPacket(sm);
			}
			_room.addMember(_player);
			
			// Info Broadcast
			_player.broadcastUserInfo();
		}
		else
		{
			_player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIREMENTS_TO_ENTER_THAT_PARTY_ROOM);
		}
	}
}
