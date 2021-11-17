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
package org.l2jmobius.gameserver.model.partymatching;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.data.xml.MapRegionData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ExManagePartyRoomMember;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public class PartyMatchRoom
{
	private final int _id;
	private String _title;
	private int _loot;
	private int _location;
	private int _minLevel;
	private int _maxLevel;
	private int _maxmem;
	private final List<Player> _members = new ArrayList<>();
	
	public PartyMatchRoom(int id, String title, int loot, int minLevel, int maxLevel, int maxmem, Player owner)
	{
		_id = id;
		_title = title;
		_loot = loot;
		_location = MapRegionData.getInstance().getClosestLocation(owner.getX(), owner.getY());
		_minLevel = minLevel;
		_maxLevel = maxLevel;
		_maxmem = maxmem;
		_members.add(owner);
	}
	
	public List<Player> getPartyMembers()
	{
		return _members;
	}
	
	public void addMember(Player player)
	{
		_members.add(player);
	}
	
	public void deleteMember(Player player)
	{
		if (player != _members.get(0)) // owner
		{
			_members.remove(player);
			notifyMembersAboutExit(player);
		}
		else if (_members.size() == 1)
		{
			PartyMatchRoomList.getInstance().deleteRoom(_id);
		}
		else
		{
			changeLeader(_members.get(1));
			deleteMember(player);
		}
	}
	
	public void notifyMembersAboutExit(Player player)
	{
		for (Player _member : _members)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_LEFT_THE_PARTY_ROOM);
			sm.addString(player.getName());
			_member.sendPacket(sm);
			_member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
		}
	}
	
	public void changeLeader(Player newLeader)
	{
		// Get current leader
		final Player oldLeader = _members.get(0);
		// Remove new leader
		if (_members.contains(newLeader))
		{
			_members.remove(newLeader);
		}
		
		// Move him to first position
		if (!_members.isEmpty())
		{
			_members.set(0, newLeader);
		}
		else
		{
			_members.add(newLeader);
		}
		
		// Add old leader as normal member
		if ((oldLeader != null) && (oldLeader != newLeader))
		{
			_members.add(oldLeader);
		}
		
		// Broadcast change
		for (Player member : _members)
		{
			member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
			member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
			member.sendPacket(SystemMessageId.THE_LEADER_OF_THE_PARTY_ROOM_HAS_CHANGED);
		}
	}
	
	public int getId()
	{
		return _id;
	}
	
	public Player getOwner()
	{
		return _members.get(0);
	}
	
	public int getMembers()
	{
		return _members.size();
	}
	
	public int getLootType()
	{
		return _loot;
	}
	
	public void setLootType(int loot)
	{
		_loot = loot;
	}
	
	public int getMinLevel()
	{
		return _minLevel;
	}
	
	public void setMinLevel(int minLevel)
	{
		_minLevel = minLevel;
	}
	
	public int getMaxLevel()
	{
		return _maxLevel;
	}
	
	public void setMaxLevel(int maxLevel)
	{
		_maxLevel = maxLevel;
	}
	
	public int getLocation()
	{
		return _location;
	}
	
	public void setLocation(int loc)
	{
		_location = loc;
	}
	
	public int getMaxMembers()
	{
		return _maxmem;
	}
	
	public void setMaxMembers(int maxmem)
	{
		_maxmem = maxmem;
	}
	
	public String getTitle()
	{
		return _title;
	}
	
	public void setTitle(String title)
	{
		_title = title;
	}
}