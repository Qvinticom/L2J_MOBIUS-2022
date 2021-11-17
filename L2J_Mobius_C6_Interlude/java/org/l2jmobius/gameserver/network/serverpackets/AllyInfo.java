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
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.SystemMessageId;

public class AllyInfo implements IClientOutgoingPacket
{
	private final Player _player;
	
	public AllyInfo(Player player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player.getAllyId() == 0)
		{
			_player.sendPacket(SystemMessageId.YOU_ARE_NOT_CURRENTLY_ALLIED_WITH_ANY_CLANS);
			return false;
		}
		
		// ======<AllyInfo>======
		SystemMessage sm = new SystemMessage(SystemMessageId.ALLIANCE_INFORMATION);
		_player.sendPacket(sm);
		// ======<Ally Name>======
		sm = new SystemMessage(SystemMessageId.ALLIANCE_NAME_S1);
		sm.addString(_player.getClan().getAllyName());
		_player.sendPacket(sm);
		int online = 0;
		int count = 0;
		int clancount = 0;
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == _player.getAllyId())
			{
				clancount++;
				online += clan.getOnlineMembers().size();
				count += clan.getMembers().size();
			}
		}
		// Connection
		sm = new SystemMessage(SystemMessageId.CONNECTION_S1_TOTAL_S2);
		sm.addString("" + online);
		sm.addString("" + count);
		_player.sendPacket(sm);
		final Clan leaderclan = ClanTable.getInstance().getClan(_player.getAllyId());
		sm = new SystemMessage(SystemMessageId.ALLIANCE_LEADER_S2_OF_S1);
		sm.addString(leaderclan.getName());
		sm.addString(leaderclan.getLeaderName());
		_player.sendPacket(sm);
		// clan count
		sm = new SystemMessage(SystemMessageId.AFFILIATED_CLANS_TOTAL_S1_CLAN_S);
		sm.addString("" + clancount);
		_player.sendPacket(sm);
		// clan information
		sm = new SystemMessage(SystemMessageId.CLAN_INFORMATION);
		_player.sendPacket(sm);
		for (Clan clan : ClanTable.getInstance().getClans())
		{
			if (clan.getAllyId() == _player.getAllyId())
			{
				// clan name
				sm = new SystemMessage(SystemMessageId.CLAN_NAME_S1);
				sm.addString(clan.getName());
				_player.sendPacket(sm);
				// clan leader name
				sm = new SystemMessage(SystemMessageId.CLAN_LEADER_S1);
				sm.addString(clan.getLeaderName());
				_player.sendPacket(sm);
				// clan level
				sm = new SystemMessage(SystemMessageId.CLAN_LEVEL_S1);
				sm.addNumber(clan.getLevel());
				_player.sendPacket(sm);
				// ---------
				sm = new SystemMessage(SystemMessageId.EMPTY_4);
				_player.sendPacket(sm);
			}
		}
		// =========================
		sm = new SystemMessage(SystemMessageId.EMPTY_5);
		_player.sendPacket(sm);
		return true;
	}
}
