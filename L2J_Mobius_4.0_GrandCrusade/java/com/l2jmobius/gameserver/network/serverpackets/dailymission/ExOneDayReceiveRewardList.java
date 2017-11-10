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
package com.l2jmobius.gameserver.network.serverpackets.dailymission;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Collection;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.data.xml.impl.DailyMissionData;
import com.l2jmobius.gameserver.model.DailyMissionDataHolder;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;
import com.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Sdw
 */
public class ExOneDayReceiveRewardList implements IClientOutgoingPacket
{
	final L2PcInstance _player;
	private final Collection<DailyMissionDataHolder> _rewards;
	
	public ExOneDayReceiveRewardList(L2PcInstance player, boolean showAllLevels)
	{
		_player = player;
		_rewards = DailyMissionData.getInstance().getDailyMissionData(player);
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ONE_DAY_RECEIVE_REWARD_LIST.writeId(packet);
		
		Calendar calendar = Calendar.getInstance();
		long currentTimeMillis = calendar.getTimeInMillis();
		int timeRemaining = 0;
		
		calendar.add(Calendar.HOUR, 24);
		calendar.set(Calendar.HOUR, 6);
		calendar.set(Calendar.MINUTE, 30);
		timeRemaining = (int) (((calendar.getTimeInMillis() - currentTimeMillis) / 1000) / 60); // minutes
		packet.writeD(timeRemaining); // Until 06:30 UTC
		
		calendar.add(Calendar.WEEK_OF_MONTH, 1);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		timeRemaining = (int) (((calendar.getTimeInMillis() - currentTimeMillis) / 1000) / 60); // minutes
		packet.writeD(timeRemaining); // Until Monday 06:30 UTC
		
		calendar.add(Calendar.MONTH, 1);
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		timeRemaining = (int) (((calendar.getTimeInMillis() - currentTimeMillis) / 1000) / 60); // minutes
		packet.writeD(timeRemaining); // Until 1st of month 06:30 UTC
		
		packet.writeC(0x17);
		packet.writeD(_player.getClassId().getId());
		packet.writeD(LocalDate.now().getDayOfWeek().ordinal()); // Day of week
		packet.writeD(_rewards.size());
		for (DailyMissionDataHolder reward : _rewards)
		{
			packet.writeH(reward.getId());
			packet.writeC(reward.getStatus(_player));
			packet.writeC(reward.getRequiredCompletions() > 1 ? 0x01 : 0x00);
			packet.writeD(Math.min(reward.getProgress(_player), _player.getLevel()));
			packet.writeD(reward.getRequiredCompletions());
		}
		return true;
	}
}
