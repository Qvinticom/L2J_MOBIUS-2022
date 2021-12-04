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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author -Wooden-
 */
public class PledgeShowMemberListUpdate implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _pledgeType;
	private int _hasSponsor;
	private final String _name;
	private final int _level;
	private final int _classId;
	private final int _isOnline;
	
	public PledgeShowMemberListUpdate(Player player)
	{
		_player = player;
		_pledgeType = _player.getPledgeType();
		if (_pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _player.getSponsor() != 0 ? 1 : 0;
		}
		else if (_player.isOnline())
		{
			_hasSponsor = _player.isClanLeader() ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
		_name = _player.getName();
		_level = _player.getLevel();
		_classId = _player.getClassId().getId();
		_isOnline = _player.isOnline() ? _player.getObjectId() : 0;
	}
	
	public PledgeShowMemberListUpdate(ClanMember player)
	{
		_player = player.getPlayer();
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId();
		_isOnline = player.isOnline() ? player.getObjectId() : 0;
		_pledgeType = player.getPledgeType();
		if (_pledgeType == Clan.SUBUNIT_ACADEMY)
		{
			_hasSponsor = _player.getSponsor() != 0 ? 1 : 0;
		}
		else if (player.isOnline())
		{
			_hasSponsor = _player.isClanLeader() ? 1 : 0;
		}
		else
		{
			_hasSponsor = 0;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_UPDATE.writeId(packet);
		packet.writeS(_name);
		packet.writeD(_level);
		packet.writeD(_classId);
		packet.writeD(0);
		packet.writeD(1);
		packet.writeD(_isOnline);
		packet.writeD(_pledgeType);
		packet.writeD(_hasSponsor);
		return true;
	}
}
