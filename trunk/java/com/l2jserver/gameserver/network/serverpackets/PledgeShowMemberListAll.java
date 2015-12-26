/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

import java.util.Collection;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2Clan.SubPledge;
import com.l2jserver.gameserver.model.L2ClanMember;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private final L2Clan _clan;
	private final SubPledge _subPledge;
	private final Collection<L2ClanMember> _members;
	private int _pledgeType;
	
	public PledgeShowMemberListAll(L2Clan clan)
	{
		_clan = clan;
		_subPledge = null;
		_members = _clan.getMembers();
	}
	
	public PledgeShowMemberListAll(L2Clan clan, SubPledge subPledge)
	{
		_clan = clan;
		_subPledge = subPledge;
		_members = _clan.getMembers();
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_subPledge == null)
		{
			// write main Clan
			writePledge(null, _clan.getName(), _clan.getLeaderName());
			
			for (SubPledge subPledge : _clan.getAllSubPledges())
			{
				getClient().sendPacket(new PledgeShowMemberListAll(_clan, subPledge));
			}
		}
		else
		{
			writePledge(_subPledge, _subPledge.getName(), getLeaderName());
		}
	}
	
	private void writePledge(SubPledge pledge, String name, String ldname)
	{
		_pledgeType = (pledge == null ? 0x00 : pledge.getId());
		writeC(0x5a);
		
		String _name = "";
		for (SubPledge subPledge : _clan.getAllSubPledges())
		{
			_name = subPledge.getName();
		}
		if ((_name == "") || (_name == name))
		{
			writeD(0);
		}
		else
		{
			writeD(1);
		}
		writeD(_clan.getId());
		writeD(Config.SERVER_ID);
		writeD(_pledgeType);
		writeS(name);
		writeS(ldname);
		
		writeD(_clan.getCrestId()); // crest id .. is used again
		writeD(_clan.getLevel());
		writeD(_clan.getCastleId());
		writeD(_clan.getHideoutId());
		writeD(_clan.getFortId());
		writeD(0x00);
		writeD(_clan.getRank());
		writeD(_clan.getReputationScore());
		writeD(0x00); // 0
		writeD(0x00); // 0
		writeD(_clan.getAllyId());
		writeS(_clan.getAllyName());
		writeD(_clan.getAllyCrestId());
		writeD(_clan.isAtWar() ? 1 : 0);// new c3
		writeD(0x00); // Territory castle ID
		writeD(_clan.getSubPledgeMembersCount(_pledgeType));
		
		for (L2ClanMember m : _members)
		{
			if (m.getPledgeType() != _pledgeType)
			{
				continue;
			}
			writeS(m.getName());
			writeD(m.getLevel());
			writeD(m.getClassId());
			final L2PcInstance player = m.getPlayerInstance();
			if (player != null)
			{
				writeD(player.getAppearance().getSex() ? 1 : 0); // no visible effect
				writeD(player.getRace().ordinal());// writeD(1);
			}
			else
			{
				writeD(0x01); // no visible effect
				writeD(0x01); // writeD(1);
			}
			writeD(m.isOnline() ? m.getObjectId() : 0); // objectId = online 0 = offline
			writeD(m.getSponsor() != 0 ? 1 : 0);
			writeC(0x00);
		}
	}
	
	private String getLeaderName()
	{
		final int LeaderId = _subPledge.getLeaderId();
		if ((_subPledge.getId() == L2Clan.SUBUNIT_ACADEMY) || (LeaderId == 0))
		{
			return "";
		}
		else if (_clan.getClanMember(LeaderId) == null)
		{
			_log.warning("SubPledgeLeader: " + LeaderId + " is missing from clan: " + _clan.getName() + "[" + _clan.getId() + "]");
			return "";
		}
		else
		{
			return _clan.getClanMember(LeaderId).getName();
		}
	}
}
