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
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class PledgeShowMemberListAdd implements IClientOutgoingPacket
{
	private String _name;
	private int _level;
	private int _classId;
	private int _objectId;
	private int _pledgeType;
	
	public PledgeShowMemberListAdd(Player player)
	{
		_name = player.getName();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_objectId = player.isOnline() ? player.getObjectId() : 0;
		_pledgeType = player.getPledgeType();
	}
	
	public PledgeShowMemberListAdd(ClanMember cm)
	{
		try
		{
			_name = cm.getName();
			_level = cm.getLevel();
			_classId = cm.getClassId();
			_objectId = cm.isOnline() ? cm.getObjectId() : 0;
			_pledgeType = cm.getPledgeType();
		}
		catch (Exception e)
		{
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.PLEDGE_SHOW_MEMBER_LIST_ADD.writeId(packet);
		packet.writeS(_name);
		packet.writeD(_level);
		packet.writeD(_classId);
		packet.writeD(0);
		packet.writeD(1);
		packet.writeD(_objectId); // 1=online 0=offline?
		packet.writeD(_pledgeType);
		return true;
	}
}
