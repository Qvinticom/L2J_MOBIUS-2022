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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.siege.Castle;
import org.l2jmobius.gameserver.model.siege.Fort;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.PacketLogger;

/**
 * Shows the Siege Info<br>
 * <br>
 * packet type id 0xc9<br>
 * format: cdddSSdSdd<br>
 * <br>
 * c = c9<br>
 * d = CastleID<br>
 * d = Show Owner Controls (0x00 default || >=0x02(mask?) owner)<br>
 * d = Owner ClanID<br>
 * S = Owner ClanName<br>
 * S = Owner Clan LeaderName<br>
 * d = Owner AllyID<br>
 * S = Owner AllyName<br>
 * d = current time (seconds)<br>
 * d = Siege time (seconds) (0 for selectable)<br>
 * d = (UNKNOW) Siege Time Select Related?
 * @author KenM
 */
public class SiegeInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _residenceId;
	private final int _ownerId;
	private final long _siegeDate;
	private final String _name;
	
	public SiegeInfo(Castle castle, Player player)
	{
		_player = player;
		_residenceId = castle.getCastleId();
		_ownerId = castle.getOwnerId();
		_siegeDate = castle.getSiege().getSiegeDate().getTimeInMillis() / 1000;
		_name = castle.getName();
	}
	
	public SiegeInfo(Fort fort, Player player)
	{
		_player = player;
		_residenceId = fort.getFortId();
		_ownerId = fort.getOwnerId();
		_siegeDate = fort.getSiege().getSiegeDate().getTimeInMillis() / 1000;
		_name = fort.getName();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SIEGE_INFO.writeId(packet);
		packet.writeD(_residenceId);
		packet.writeD((_ownerId == _player.getClanId()) && _player.isClanLeader() ? 0x01 : 0x00);
		packet.writeD(_ownerId);
		if (_ownerId > 0)
		{
			final Clan owner = ClanTable.getInstance().getClan(_ownerId);
			if (owner != null)
			{
				packet.writeS(owner.getName()); // Clan Name
				packet.writeS(owner.getLeaderName()); // Clan Leader Name
				packet.writeD(owner.getAllyId()); // Ally ID
				packet.writeS(owner.getAllyName()); // Ally Name
			}
			else
			{
				PacketLogger.warning("Null owner for castle: " + _name);
			}
		}
		else
		{
			packet.writeS("NPC"); // Clan Name
			packet.writeS(""); // Clan Leader Name
			packet.writeD(0); // Ally ID
			packet.writeS(""); // Ally Name
		}
		
		packet.writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000));
		packet.writeD((int) _siegeDate);
		packet.writeD(0x00); // number of choices?
		return true;
	}
}
