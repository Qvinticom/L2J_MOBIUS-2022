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
package com.l2jmobius.gameserver.model;

import java.util.Map;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExClosePartyRoom;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastMap;

/**
 * @author Gnacik
 */
public class PartyMatchRoomList
{
	private int _maxid;
	private final Map<Integer, PartyMatchRoom> _rooms;
	private static PartyMatchRoomList _instance;
	
	public static PartyMatchRoomList getInstance()
	{
		if (_instance == null)
		{
			_instance = new PartyMatchRoomList();
		}
		return _instance;
	}
	
	private PartyMatchRoomList()
	{
		_rooms = new FastMap<>();
	}
	
	public synchronized void addPartyMatchRoom(PartyMatchRoom room)
	{
		_rooms.put(room.getId(), room);
	}
	
	public void deleteRoom(int id)
	{
		for (final L2PcInstance _member : getRoom(id).getPartyMembers())
		{
			if (_member == null)
			{
				continue;
			}
			
			_member.sendPacket(new ExClosePartyRoom());
			_member.sendPacket(new SystemMessage(SystemMessage.PARTY_ROOM_DISBANDED));
			
			_member.setPartyRoom(0);
			_member.broadcastUserInfo();
		}
		_rooms.remove(id);
	}
	
	public PartyMatchRoom getRoom(int id)
	{
		return _rooms.get(id);
	}
	
	public PartyMatchRoom[] getRooms()
	{
		return _rooms.values().toArray(new PartyMatchRoom[_rooms.size()]);
	}
	
	public int getPartyMatchRoomCount()
	{
		return _rooms.size();
	}
	
	public int getAutoIncrementId()
	{
		// reset all ids as free
		// if room list is empty
		if (_rooms.size() == 0)
		{
			_maxid = 0;
		}
		
		_maxid++;
		
		return _maxid;
	}
	
	public PartyMatchRoom getPlayerRoom(L2PcInstance player)
	{
		for (final PartyMatchRoom _room : _rooms.values())
		{
			for (final L2PcInstance member : _room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return _room;
				}
			}
		}
		return null;
	}
	
	public int getPlayerRoomId(L2PcInstance player)
	{
		for (final PartyMatchRoom _room : _rooms.values())
		{
			for (final L2PcInstance member : _room.getPartyMembers())
			{
				if (member.equals(player))
				{
					return _room.getId();
				}
			}
		}
		return -1;
	}
}