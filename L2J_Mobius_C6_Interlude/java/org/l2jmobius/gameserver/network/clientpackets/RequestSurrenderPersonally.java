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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.sql.ClanTable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestSurrenderPersonally implements IClientIncomingPacket
{
	private String _pledgeName;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_pledgeName = packet.readS();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		PacketLogger.info("RequestSurrenderPersonally by " + player.getName() + " with " + _pledgeName);
		final Clan playerClan = player.getClan();
		final Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if (playerClan == null)
		{
			return;
		}
		
		if (clan == null)
		{
			player.sendMessage("No such clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (!playerClan.isAtWarWith(clan.getClanId()) || (player.getWantsPeace() == 1))
		{
			player.sendMessage("You aren't at war with this clan.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.setWantsPeace(1);
		player.deathPenalty(false);
		final SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_PERSONALLY_SURRENDERED_TO_THE_S1_CLAN_YOU_ARE_NO_LONGER_PARTICIPATING_IN_THIS_CLAN_WAR);
		msg.addString(_pledgeName);
		player.sendPacket(msg);
		ClanTable.getInstance().checkSurrender(playerClan, clan);
	}
}
