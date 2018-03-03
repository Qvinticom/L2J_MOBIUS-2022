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
package com.l2jmobius.gameserver.handler.skillhandlers;

import java.util.logging.Logger;

import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Julian
 */
public class DeluxeKey implements ISkillHandler
{
	private static Logger LOGGER = Logger.getLogger(BeastFeed.class.getName());
	
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.DELUXE_KEY_UNLOCK
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets)
	{
		if (!(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		L2Object[] targetList = skill.getTargetList(activeChar);
		
		if (targetList == null)
		{
			return;
		}
		
		LOGGER.info("Delux key casting succeded.");
		
		// This is just a dummy skill handler for the golden food and crystal food skills,
		// since the AI responce onSkillUse handles the rest.
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}
