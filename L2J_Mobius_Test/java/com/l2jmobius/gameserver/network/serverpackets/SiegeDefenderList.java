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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.data.sql.impl.ClanTable;
import com.l2jmobius.gameserver.enums.SiegeClanType;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2SiegeClan;
import com.l2jmobius.gameserver.model.entity.Castle;

/**
 * Populates the Siege Defender List in the SiegeInfo Window<BR>
 * <BR>
 * c = 0xcb<BR>
 * d = CastleID<BR>
 * d = unknow (0x00)<BR>
 * d = unknow (0x01)<BR>
 * d = unknow (0x00)<BR>
 * d = Number of Defending Clans?<BR>
 * d = Number of Defending Clans<BR>
 * { //repeats<BR>
 * d = ClanID<BR>
 * S = ClanName<BR>
 * S = ClanLeaderName<BR>
 * d = ClanCrestID<BR>
 * d = signed time (seconds)<BR>
 * d = Type -> Owner = 0x01 || Waiting = 0x02 || Accepted = 0x03<BR>
 * d = AllyID<BR>
 * S = AllyName<BR>
 * S = AllyLeaderName<BR>
 * d = AllyCrestID<BR>
 * @author KenM
 */
public final class SiegeDefenderList extends L2GameServerPacket
{
	private final Castle _castle;
	
	public SiegeDefenderList(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xcb);
		writeD(_castle.getResidenceId());
		writeD(0x00); // Unknown
		writeD(0x01); // Unknown
		writeD(0x00); // Unknown
		
		final int size = _castle.getSiege().getDefenderWaitingClans().size() + _castle.getSiege().getDefenderClans().size() + (_castle.getOwner() != null ? 1 : 0);
		
		writeD(size);
		writeD(size);
		
		// Add owners
		final L2Clan ownerClan = _castle.getOwner();
		if (ownerClan != null)
		{
			writeD(ownerClan.getId());
			writeS(ownerClan.getName());
			writeS(ownerClan.getLeaderName());
			writeD(ownerClan.getCrestId());
			writeD(0x00); // signed time (seconds) (not storated by L2J)
			writeD(SiegeClanType.OWNER.ordinal());
			writeD(ownerClan.getAllyId());
			writeS(ownerClan.getAllyName());
			writeS(""); // AllyLeaderName
			writeD(ownerClan.getAllyCrestId());
		}
		
		// List of confirmed defenders
		for (L2SiegeClan siegeClan : _castle.getSiege().getDefenderClans())
		{
			final L2Clan defendingClan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if ((defendingClan == null) || (defendingClan == _castle.getOwner()))
			{
				continue;
			}
			
			writeD(defendingClan.getId());
			writeS(defendingClan.getName());
			writeS(defendingClan.getLeaderName());
			writeD(defendingClan.getCrestId());
			writeD(0x00); // signed time (seconds) (not storated by L2J)
			writeD(SiegeClanType.DEFENDER.ordinal());
			writeD(defendingClan.getAllyId());
			writeS(defendingClan.getAllyName());
			writeS(""); // AllyLeaderName
			writeD(defendingClan.getAllyCrestId());
		}
		
		// List of not confirmed defenders
		for (L2SiegeClan siegeClan : _castle.getSiege().getDefenderWaitingClans())
		{
			final L2Clan defendingClan = ClanTable.getInstance().getClan(siegeClan.getClanId());
			if (defendingClan == null)
			{
				continue;
			}
			
			writeD(defendingClan.getId());
			writeS(defendingClan.getName());
			writeS(defendingClan.getLeaderName());
			writeD(defendingClan.getCrestId());
			writeD(0x00); // signed time (seconds) (not storated by L2J)
			writeD(SiegeClanType.DEFENDER_PENDING.ordinal());
			writeD(defendingClan.getAllyId());
			writeS(defendingClan.getAllyName());
			writeS(""); // AllyLeaderName
			writeD(defendingClan.getAllyCrestId());
		}
	}
}
