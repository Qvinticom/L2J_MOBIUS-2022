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
 * Populates the Siege Defender List in the SiegeInfo Window<br>
 * <br>
 * packet type id 0xcb<br>
 * format: cddddddd + dSSdddSSd<br>
 * <br>
 * c = 0xcb<br>
 * d = CastleID<br>
 * d = unknown (0)<br>
 * d = unknown (1)<br>
 * d = unknown (0)<br>
 * d = Number of Defending Clans?<br>
 * d = Number of Defending Clans<br>
 * { //repeats<br>
 * d = ClanID<br>
 * S = ClanName<br>
 * S = ClanLeaderName<br>
 * d = ClanCrestID<br>
 * d = signed time (seconds)<br>
 * d = Type -> Owner = 0x01 || Waiting = 0x02 || Accepted = 0x03<br>
 * d = AllyID<br>
 * S = AllyName<br>
 * S = AllyLeaderName<br>
 * d = AllyCrestID<br>
 * @author KenM
 */
public class SiegeDefenderList implements IClientOutgoingPacket
{
	private final int _residenceId;
	private final Collection<SiegeClan> _defenders;
	private final Collection<SiegeClan> _waiting;
	
	public SiegeDefenderList(Castle castle)
	{
		_residenceId = castle.getCastleId();
		_defenders = castle.getSiege().getDefenderClans();
		_waiting = castle.getSiege().getDefenderWaitingClans();
	}
	
	public SiegeDefenderList(Fort fort)
	{
		_residenceId = fort.getFortId();
		_defenders = fort.getSiege().getDefenderClans();
		_waiting = fort.getSiege().getDefenderWaitingClans();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SIEGE_DEFENDER_LIST.writeId(packet);
		packet.writeD(_residenceId);
		packet.writeD(0); // 0
		packet.writeD(1); // 1
		packet.writeD(0); // 0
		final int size = _defenders.size() + _waiting.size();
		if (size > 0)
		{
			Clan clan;
			packet.writeD(size);
			packet.writeD(size);
			// Listing the Lord and the approved clans
			for (SiegeClan siegeclan : _defenders)
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
				switch (siegeclan.getType())
				{
					case OWNER:
					{
						packet.writeD(1); // owner
						break;
					}
					case DEFENDER_PENDING:
					{
						packet.writeD(2); // approved
						break;
					}
					case DEFENDER:
					{
						packet.writeD(3); // waiting approved
						break;
					}
					default:
					{
						packet.writeD(0);
						break;
					}
				}
				packet.writeD(clan.getAllyId());
				packet.writeS(clan.getAllyName());
				packet.writeS(""); // AllyLeaderName
				packet.writeD(clan.getAllyCrestId());
			}
			for (SiegeClan siegeclan : _waiting)
			{
				clan = ClanTable.getInstance().getClan(siegeclan.getClanId());
				packet.writeD(clan.getClanId());
				packet.writeS(clan.getName());
				packet.writeS(clan.getLeaderName());
				packet.writeD(clan.getCrestId());
				packet.writeD(0); // signed time (seconds) (not storated by L2J)
				packet.writeD(2); // waiting approval
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
