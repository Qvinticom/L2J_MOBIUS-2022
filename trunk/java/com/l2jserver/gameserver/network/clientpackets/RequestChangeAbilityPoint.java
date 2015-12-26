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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.AbilityPointsData;
import com.l2jserver.gameserver.enums.UserInfoType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExAcquireAPSkillList;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;

/**
 * @author UnAfraid
 */
public class RequestChangeAbilityPoint extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{
		// Nothing to read
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		
		else if (activeChar.getAbilityPoints() >= Config.ABILITY_MAX_POINTS)
		{
			activeChar.sendPacket(SystemMessageId.YOU_CANNOT_ACQUIRE_ANY_MORE_ABILITY_POINTS);
			return;
		}
		
		final long spRequired = AbilityPointsData.getInstance().getPrice(activeChar.getAbilityPoints());
		if (spRequired > activeChar.getSp())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_NEED_S1_SP_TO_CONVERT_TO1_ABILITY_POINT);
			sm.addLong(spRequired);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (activeChar.getStat().removeSp(spRequired))
		{
			activeChar.setAbilityPoints(activeChar.getAbilityPoints() + 1);
			final UserInfo info = new UserInfo(activeChar, false);
			info.addComponentType(UserInfoType.SLOTS, UserInfoType.CURRENT_HPMPCP_EXP_SP);
			activeChar.sendPacket(info);
			activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
