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
package org.l2jmobius.gameserver.network.serverpackets.pledgebonus;

import java.util.Comparator;
import java.util.logging.Logger;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.ClanRewardData;
import org.l2jmobius.gameserver.enums.ClanRewardType;
import org.l2jmobius.gameserver.model.clan.ClanRewardBonus;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author UnAfraid
 */
public class ExPledgeBonusList implements IClientOutgoingPacket
{
	private static final Logger LOGGER = Logger.getLogger(ExPledgeBonusList.class.getName());
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_PLEDGE_BONUS_LIST.writeId(packet);
		for (ClanRewardType type : ClanRewardType.values())
		{
			ClanRewardData.getInstance().getClanRewardBonuses(type).stream().sorted(Comparator.comparingInt(ClanRewardBonus::getLevel)).forEach(bonus ->
			{
				switch (type)
				{
					case MEMBERS_ONLINE:
					{
						if (bonus.getSkillReward() == null)
						{
							LOGGER.warning("Missing clan reward skill for reward level: " + bonus.getLevel());
							packet.writeD(0);
							return;
						}
						packet.writeD(bonus.getSkillReward().getSkillId());
						break;
					}
					case HUNTING_MONSTERS:
					{
						if (bonus.getItemReward() == null)
						{
							LOGGER.warning("Missing clan reward skill for reward level: " + bonus.getLevel());
							packet.writeD(0);
							return;
						}
						packet.writeD(bonus.getItemReward().getId());
						break;
					}
				}
			});
		}
		return true;
	}
}
