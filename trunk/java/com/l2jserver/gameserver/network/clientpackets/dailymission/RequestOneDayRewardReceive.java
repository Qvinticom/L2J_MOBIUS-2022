/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.clientpackets.dailymission;

import com.l2jserver.gameserver.data.xml.impl.DailyMissionData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;

/**
 * @author Mobius
 */
public class RequestOneDayRewardReceive extends L2GameClientPacket
{
	private int _reward;
	
	@Override
	protected void readImpl()
	{
		_reward = readC();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		DailyMissionData.getInstance().rewardPlayer(_reward, player);
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
