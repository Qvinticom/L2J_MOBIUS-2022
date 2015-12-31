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

import java.util.List;

import com.l2jmobius.gameserver.data.xml.impl.DailyMissionData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.DailyMissionHolder;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Mobius
 */
public class ExOneDayReceiveRewardList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _classId;
	private final List<DailyMissionHolder> _availableMissions;
	
	public ExOneDayReceiveRewardList(L2PcInstance activeChar)
	{
		_activeChar = activeChar;
		_classId = activeChar.getBaseClassId();
		_availableMissions = DailyMissionData.getInstance().getDailyLevelUpMissions(_classId);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x188);
		writeD(_classId);
		writeD(0x00); // Day
		writeD(_availableMissions.size()); // TODO: Implement all missions.
		for (DailyMissionHolder mission : _availableMissions)
		{
			writeH(mission.getClientId()); // Reward
			writeC(DailyMissionData.getInstance().RewardStatus(mission.getId(), _activeChar)); // 1 Available, 2 Not Available, 3 Complete
			writeC(0x00); // Requires multiple completion - YesOrNo
			writeD(0x00); // Current progress
			writeD(0x00); // Required total
		}
	}
}
