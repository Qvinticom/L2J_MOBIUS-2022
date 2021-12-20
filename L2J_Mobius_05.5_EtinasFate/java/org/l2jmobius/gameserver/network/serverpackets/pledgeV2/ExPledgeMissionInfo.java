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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.DailyMissionData;
import org.l2jmobius.gameserver.model.DailyMissionDataHolder;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExPledgeMissionInfo implements IClientOutgoingPacket
{
	private final Player _player;
	private final Collection<DailyMissionDataHolder> _rewards;
	
	public ExPledgeMissionInfo(Player player)
	{
		_player = player;
		_rewards = DailyMissionData.getInstance().getDailyMissionData(player);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (!DailyMissionData.getInstance().isAvailable() || (_player.getClan() == null))
		{
			return true;
		}
		OutgoingPackets.EX_PLEDGE_MISSION_INFO.writeId(packet);
		packet.writeD(_rewards.size());
		for (DailyMissionDataHolder reward : _rewards)
		{
			int progress = reward.getProgress(_player);
			int status = reward.getStatus(_player);
			// TODO: Figure out this.
			if (reward.isLevelUpMission())
			{
				progress = 1;
				if (status == 2)
				{
					status = reward.getRequiredCompletions() > _player.getLevel() ? 1 : 3;
				}
				else
				{
					status = reward.isRecentlyCompleted(_player) ? 0 : 3;
				}
			}
			else if (status == 1)
			{
				status = 3;
			}
			else if (status == 3)
			{
				status = 2;
			}
			packet.writeD(reward.getId());
			packet.writeD(progress);
			packet.writeC(status);
		}
		return true;
	}
}
