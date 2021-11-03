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
package org.l2jmobius.gameserver.network.clientpackets.pledgebonus;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.pledgeV3.ExPledgeClassicRaidInfo;
import org.l2jmobius.gameserver.network.serverpackets.pledgebonus.ExPledgeBonusOpen;
import org.l2jmobius.gameserver.network.serverpackets.pledgedonation.ExPledgeDonationInfo;

/**
 * @author UnAfraid
 */
public class RequestPledgeBonusOpen implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if ((player == null) || (player.getClan() == null))
		{
			return;
		}
		
		player.sendPacket(new ExPledgeBonusOpen(player));
		player.sendPacket(new ExPledgeClassicRaidInfo());
		final long joinedTime = (player.getClanJoinExpiryTime() - (Config.ALT_CLAN_JOIN_DAYS * 60000));
		player.sendPacket(new ExPledgeDonationInfo(player.getClanDonationPoints(), (joinedTime + 86400000) < System.currentTimeMillis()));
	}
}
