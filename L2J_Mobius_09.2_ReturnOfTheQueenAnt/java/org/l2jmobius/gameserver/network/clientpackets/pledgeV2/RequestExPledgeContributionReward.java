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
package org.l2jmobius.gameserver.network.clientpackets.pledgeV2;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.variables.PlayerVariables;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;

/**
 * @author Mobius
 */
public class RequestExPledgeContributionReward implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if ((player == null) || (player.getClan() == null))
		{
			return;
		}
		
		if (player.getClanContribution() < Config.CLAN_CONTRIBUTION_REQUIRED)
		{
			return;
		}
		
		if (player.getVariables().getBoolean(PlayerVariables.CLAN_CONTRIBUTION_REWARDED, false))
		{
			player.sendMessage("You have already been rewarded for this cycle.");
			return;
		}
		player.getVariables().set(PlayerVariables.CLAN_CONTRIBUTION_REWARDED, true);
		
		player.setFame(player.getFame() + Config.CLAN_CONTRIBUTION_FAME_REWARD);
		player.sendMessage("You have been rewarded with " + Config.CLAN_CONTRIBUTION_FAME_REWARD + " fame points.");
	}
}