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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.model.siege.SiegeClan;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Populates the Siege Attacker List in the SiegeInfo Window<br>
 * <br>
 * packet type id 0xca<br>
 * format: cddddddd + dSSdddSSd<br>
 * <br>
 * c = ca<br>
 * d = CastleID<br>
 * d = unknown (0)<br>
 * d = unknown (1)<br>
 * d = unknown (0)<br>
 * d = Number of Attackers Clans?<br>
 * d = Number of Attackers Clans<br>
 * { //repeats<br>
 * d = ClanID<br>
 * S = ClanName<br>
 * S = ClanLeaderName<br>
 * d = ClanCrestID<br>
 * d = signed time (seconds)<br>
 * d = AllyID<br>
 * S = AllyName<br>
 * S = AllyLeaderName<br>
 * d = AllyCrestID<br>
 * @author KenM
 */
public class SiegeAttackerList implements IClientOutgoingPacket
{
	private final int _residenceId;
	private final Collection<SiegeClan> _attackers;
	
	public SiegeAttackerList(Castle castle)
	{
		_residenceId = castle.getCastleId();
		_attackers = castle.getSiege().getAttackerClans();
	}
	
	public SiegeAttackerList(Fort fort)
	{
		_residenceId = fort.getFortId();
		_attackers = fort.getSiege().getAttackerClans();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SIEGE_ATTACKER_LIST.writeId(packet);
		packet.writeD(_residenceId);
		packet.writeD(0); // 0
		packet.writeD(1); // 1
		packet.writeD(0); // 0
		final int size = _attackers.size();
		if (size > 0)
		{
			Clan clan;
			packet.writeD(size);
			packet.writeD(size);
			for (SiegeClan siegeclan : _attackers)
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				if (clan == null)
				{
					continue;
				}
				packet.writeD(clan.getClanId());
				packet.writeS(clan.getName());
				packet.writeS(clan.getLeaderName());
				packet.writeD(clan.getCrestId());
				packet.writeD(0); // signed time (seconds) (not storated by L2J)
				packet.writeD(clan.getAllyId());
				packet.writeS(clan.getAllyName());
				packet.writeS(""); // AllyLeaderName
				packet.writeD(clan.getAllyCrestId());
			}
		}
		else
		{
			packet.writeD(0);
			packet.writeD(0);
		}
		return true;
	}
}
