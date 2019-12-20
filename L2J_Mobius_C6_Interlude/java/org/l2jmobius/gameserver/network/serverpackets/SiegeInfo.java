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

import java.util.Calendar;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.datatables.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.entity.siege.Castle;

/**
 * Shows the Siege Info<BR>
 * <BR>
 * packet type id 0xc9<BR>
 * format: cdddSSdSdd<BR>
 * <BR>
 * c = c9<BR>
 * d = CastleID<BR>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<BR>
 * d = Owner ClanID<BR>
 * S = Owner ClanName<BR>
 * S = Owner Clan LeaderName<BR>
 * d = Owner AllyID<BR>
 * S = Owner AllyName<BR>
 * d = current time (seconds)<BR>
 * d = Siege time (seconds) (0 for selectable)<BR>
 * d = (UNKNOW) Siege Time Select Related?
 * @author KenM
 */
public class SiegeInfo extends GameServerPacket
{
	private static final Logger LOGGER = Logger.getLogger(SiegeInfo.class.getName());
	private final Castle _castle;
	
	public SiegeInfo(Castle castle)
	{
		_castle = castle;
	}
	
	@Override
	protected final void writeImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
		if (player == null)
		{
			return;
		}
		
		writeC(0xc9);
		writeD(_castle.getCastleId());
		writeD((_castle.getOwnerId() == player.getClanId()) && player.isClanLeader() ? 0x01 : 0x00);
		writeD(_castle.getOwnerId());
		if (_castle.getOwnerId() > 0)
		{
			final Clan owner = ClanTable.getInstance().getClan(_castle.getOwnerId());
			if (owner != null)
			{
				writeS(owner.getName()); // Clan Name
				writeS(owner.getLeaderName()); // Clan Leader Name
				writeD(owner.getAllyId()); // Ally ID
				writeS(owner.getAllyName()); // Ally Name
			}
			else
			{
				LOGGER.warning("Null owner for castle: " + _castle.getName());
			}
		}
		else
		{
			writeS("NPC"); // Clan Name
			writeS(""); // Clan Leader Name
			writeD(0); // Ally ID
			writeS(""); // Ally Name
		}
		
		writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		writeD((int) (_castle.getSiege().getSiegeDate().getTimeInMillis() / 1000));
		writeD(0x00); // number of choices?
	}
}
