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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.ClanMasteryData;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.holders.ClanMasteryHolder;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.pledgeV2.ExPledgeMasteryInfo;

/**
 * @author Mobius
 */
public class RequestExPledgeMasterySet implements IClientIncomingPacket
{
	private int _masteryId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_masteryId = packet.readD();
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
		final Clan clan = player.getClan();
		if (clan == null)
		{
			return;
		}
		if (player.getObjectId() != clan.getLeaderId())
		{
			player.sendMessage("You do not have enough privileges to take this action.");
			return;
		}
		
		// Check if already enabled.
		if (clan.hasMastery(_masteryId))
		{
			player.sendMessage("This mastery is already available.");
			return;
		}
		
		// Check if it can be learned.
		if (clan.getTotalDevelopmentPoints() <= clan.getUsedDevelopmentPoints())
		{
			player.sendMessage("Your clan develpment points are not sufficient.");
			return;
		}
		final ClanMasteryHolder mastery = ClanMasteryData.getInstance().getClanMastery(_masteryId);
		if (clan.getLevel() < mastery.getClanLevel())
		{
			player.sendMessage("Your clan level is lower than the requirement.");
			return;
		}
		if (clan.getReputationScore() < mastery.getClanReputation())
		{
			player.sendMessage("Your clan reputation is lower than the requirement.");
			return;
		}
		final int previous = mastery.getPreviousMastery();
		final int previousAlt = mastery.getPreviousMasteryAlt();
		if (previousAlt > 0)
		{
			if (!clan.hasMastery(previous) && !clan.hasMastery(previousAlt))
			{
				player.sendMessage("You need to learn a previous mastery.");
				return;
			}
		}
		else if ((previous > 0) && !clan.hasMastery(previous))
		{
			player.sendMessage("You need to learn the previous mastery.");
			return;
		}
		
		// Learn.
		clan.takeReputationScore(mastery.getClanReputation());
		clan.addMastery(mastery.getId());
		clan.setDevelopmentPoints(clan.getUsedDevelopmentPoints() + 1);
		for (Skill skill : mastery.getSkills())
		{
			clan.addNewSkill(skill);
		}
		player.sendPacket(new ExPledgeMasteryInfo(player));
	}
}