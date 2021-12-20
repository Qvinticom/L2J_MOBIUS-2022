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
package org.l2jmobius.gameserver.network.serverpackets.pledgeV2;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.clan.ClanMember;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExPledgeContributionRank implements IClientOutgoingPacket
{
	private final Clan _clan;
	private final int _cycle;
	
	public ExPledgeContributionRank(Clan clan, int cycle)
	{
		_clan = clan;
		_cycle = cycle;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_clan == null)
		{
			return false;
		}
		OutgoingPackets.EX_PLEDGE_CONTRIBUTION_RANK.writeId(packet);
		packet.writeC(_cycle);
		packet.writeD(_clan.getMembersCount());
		int order = 1;
		for (ClanMember member : _clan.getMembers())
		{
			if (member.isOnline())
			{
				final Player player = member.getPlayer();
				packet.writeD(order++); // Order?
				packet.writeS(String.format("%1$-" + 24 + "s", player.getName()));
				packet.writeD(player.getPledgeType());
				packet.writeD(player.getClanContribution());
				packet.writeD(player.getClanContributionTotal());
			}
			else
			{
				packet.writeD(order++); // Order?
				packet.writeS(String.format("%1$-" + 24 + "s", member.getName()));
				packet.writeD(member.getPledgeType());
				packet.writeD(member.getClanContribution());
				packet.writeD(member.getClanContributionTotal());
			}
		}
		return true;
	}
}
