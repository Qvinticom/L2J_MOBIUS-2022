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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoom;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchRoomList;
import org.l2jmobius.gameserver.model.partymatching.PartyMatchWaitingList;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExPartyRoomMember;
import org.l2jmobius.gameserver.network.serverpackets.PartyMatchDetail;

/**
 * author: Gnacik Packetformat Rev650 cdddddS
 */
public class RequestPartyMatchList implements IClientIncomingPacket
{
	private int _roomid;
	private int _membersmax;
	private int _minLevel;
	private int _maxLevel;
	private int _loot;
	private String _roomtitle;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_roomid = packet.readD();
		_membersmax = packet.readD();
		_minLevel = packet.readD();
		_maxLevel = packet.readD();
		_loot = packet.readD();
		_roomtitle = packet.readS();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (_roomid > 0)
		{
			final PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_roomid);
			if (room != null)
			{
				LOGGER.info("PartyMatchRoom #" + room.getId() + " changed by " + player.getName());
				room.setMaxMembers(_membersmax);
				room.setMinLevel(_minLevel);
				room.setMaxLevel(_maxLevel);
				room.setLootType(_loot);
				room.setTitle(_roomtitle);
				
				for (Player member : room.getPartyMembers())
				{
					if (member == null)
					{
						continue;
					}
					
					member.sendPacket(new PartyMatchDetail(room));
					member.sendPacket(SystemMessageId.THE_PARTY_ROOM_S_INFORMATION_HAS_BEEN_REVISED);
				}
			}
		}
		else
		{
			final int maxId = PartyMatchRoomList.getInstance().getMaxId();
			final PartyMatchRoom room = new PartyMatchRoom(maxId, _roomtitle, _loot, _minLevel, _maxLevel, _membersmax, player);
			
			LOGGER.info("PartyMatchRoom #" + maxId + " created by " + player.getName());
			
			// Remove from waiting list, and add to current room
			PartyMatchWaitingList.getInstance().removePlayer(player);
			PartyMatchRoomList.getInstance().addPartyMatchRoom(maxId, room);
			if (player.isInParty())
			{
				for (Player ptmember : player.getParty().getPartyMembers())
				{
					if (ptmember == null)
					{
						continue;
					}
					if (ptmember == player)
					{
						continue;
					}
					
					ptmember.setPartyRoom(maxId);
					
					room.addMember(ptmember);
				}
			}
			
			player.sendPacket(new PartyMatchDetail(room));
			player.sendPacket(new ExPartyRoomMember(room, 1));
			player.sendPacket(SystemMessageId.A_PARTY_ROOM_HAS_BEEN_CREATED);
			
			player.setPartyRoom(maxId);
			player.broadcastUserInfo();
		}
	}
}