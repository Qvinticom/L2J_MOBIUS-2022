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
package org.l2jmobius.gameserver.model;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;

public class Clan
{
	private static final Logger _log = Logger.getLogger(Clan.class.getName());
	private String _name;
	private int _clanId;
	private ClanMember _leader;
	private final Map<String, ClanMember> _members = new TreeMap<>();
	private String _allyName;
	private int _allyId;
	private int _level;
	private int _hasCastle;
	private int _hasHideout;
	
	public int getClanId()
	{
		return _clanId;
	}
	
	public void setClanId(int clanId)
	{
		_clanId = clanId;
	}
	
	public int getLeaderId()
	{
		return _leader.getObjectId();
	}
	
	public void setLeader(ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getName(), leader);
	}
	
	public String getLeaderName()
	{
		return _leader.getName();
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public void addClanMember(ClanMember member)
	{
		_members.put(member.getName(), member);
	}
	
	public void addClanMember(PlayerInstance player)
	{
		ClanMember member = new ClanMember(player);
		this.addClanMember(member);
	}
	
	public ClanMember getClanMember(String name)
	{
		return _members.get(name);
	}
	
	public void removeClanMember(String name)
	{
		_members.remove(name);
	}
	
	public Collection<ClanMember> getMembers()
	{
		return _members.values();
	}
	
	public Collection<PlayerInstance> getOnlineMembers(String exclude)
	{
		List<PlayerInstance> result = new ArrayList<>();
		for (ClanMember member : _members.values())
		{
			if (!member.isOnline() || member.getName().equals(exclude))
			{
				continue;
			}
			result.add(member.getPlayerInstance());
		}
		return result;
	}
	
	public int getAllyId()
	{
		return _allyId;
	}
	
	public String getAllyName()
	{
		return _allyName;
	}
	
	public int getAllyCrestId()
	{
		return _allyId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public int getHasCastle()
	{
		return _hasCastle;
	}
	
	public int getHasHideout()
	{
		return _hasHideout;
	}
	
	public int getCrestId()
	{
		return _clanId;
	}
	
	public void setAllyId(int allyId)
	{
		_allyId = allyId;
	}
	
	public void setAllyName(String allyName)
	{
		_allyName = allyName;
	}
	
	public void setHasCastle(int hasCastle)
	{
		_hasCastle = hasCastle;
	}
	
	public void setHasHideout(int hasHideout)
	{
		_hasHideout = hasHideout;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public boolean isMember(String name)
	{
		return _members.containsKey(name);
	}
	
	public void store()
	{
		File clanFile = new File("data/clans/" + getName() + ".csv");
		FileWriter out = null;
		try
		{
			out = new FileWriter(clanFile);
			out.write("#clanId;clanName;clanLevel;hasCastle;hasHideout;allianceId;allianceName\r\n");
			out.write(getClanId() + ";");
			out.write(getName() + ";");
			out.write(getLevel() + ";");
			out.write(getHasCastle() + ";");
			out.write(getHasHideout() + ";");
			out.write(getAllyId() + ";");
			out.write("none\r\n");
			out.write("#memberName;memberLevel;classId;objectId\r\n");
			for (ClanMember member : getMembers())
			{
				if (member.getObjectId() != getLeaderId())
				{
					continue;
				}
				out.write(member.getName() + ";");
				out.write(member.getLevel() + ";");
				out.write(member.getClassId() + ";");
				out.write(member.getObjectId() + "\r\n");
			}
			for (ClanMember member : getMembers())
			{
				if (member.getObjectId() == getLeaderId())
				{
					continue;
				}
				out.write(member.getName() + ";");
				out.write(member.getLevel() + ";");
				out.write(member.getClassId() + ";");
				out.write(member.getObjectId() + "\r\n");
			}
		}
		catch (Exception e)
		{
			_log.warning("could not store clan:" + e.toString());
		}
		finally
		{
			try
			{
				if (out != null)
				{
					out.close();
				}
			}
			catch (Exception e1)
			{
			}
		}
	}
	
	public void broadcastToOnlineMembers(ServerBasePacket packet)
	{
		Iterator<ClanMember> iter = _members.values().iterator();
		while (iter.hasNext())
		{
			ClanMember member = iter.next();
			if (!member.isOnline())
			{
				continue;
			}
			member.getPlayerInstance().sendPacket(packet);
		}
	}
	
	@Override
	public String toString()
	{
		return getName();
	}
}
