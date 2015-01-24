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

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.data.xml.impl.SkillTreesData;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExAcquireAPSkillList;

/**
 * @author UnAfraid
 */
public class RequestAcquireAbilityList extends L2GameClientPacket
{
	private final List<SkillHolder> _skills = new ArrayList<>();
	
	@Override
	protected void readImpl()
	{
		readD(); // Total size
		for (int i = 0; i < 3; i++)
		{
			int size = readD();
			for (int j = 0; j < size; j++)
			{
				_skills.add(new SkillHolder(readD(), readD()));
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if ((activeChar.getAbilityPoints() == 0) || (activeChar.getAbilityPoints() == activeChar.getAbilityPointsUsed()))
		{
			_log.warning(getClass().getSimpleName() + ": Player " + activeChar + " is trying to learn ability without ability points!");
			return;
		}
		
		if ((activeChar.getLevel() < 99) || !activeChar.isNoble())
		{
			activeChar.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		
		for (SkillHolder holder : _skills)
		{
			final L2SkillLearn learn = SkillTreesData.getInstance().getAbilitySkill(holder.getSkillId(), holder.getSkillLvl());
			if (learn == null)
			{
				_log.warning(getClass().getSimpleName() + ": SkillLearn " + holder.getSkillId() + "(" + holder.getSkillLvl() + ") not found!");
				sendActionFailed();
				break;
			}
			
			final Skill skill = holder.getSkill();
			if (skill == null)
			{
				_log.warning(getClass().getSimpleName() + ": SkillLearn " + holder.getSkillId() + "(" + holder.getSkillLvl() + ") not found!");
				sendActionFailed();
				break;
			}
			final int points;
			final int knownLevel = activeChar.getSkillLevel(holder.getSkillId());
			if (knownLevel == -1) // player didn't knew it at all!
			{
				points = holder.getSkillLvl();
			}
			else
			{
				points = holder.getSkillLvl() - knownLevel;
			}
			
			if ((activeChar.getAbilityPoints() - activeChar.getAbilityPointsUsed()) < points)
			{
				_log.warning(getClass().getSimpleName() + ": Player " + activeChar + " is trying to learn ability without ability points!");
				sendActionFailed();
				return;
			}
			
			activeChar.addSkill(skill, true);
			activeChar.setAbilityPointsUsed(activeChar.getAbilityPointsUsed() + points);
		}
		activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}