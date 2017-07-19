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
package com.l2jmobius.gameserver.handler;

import java.io.IOException;

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;

/**
 * an IItemHandler implementation has to be stateless
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/03 15:55:06 $
 */
public interface ISkillHandler
{
	/**
	 * this is the working method that is called when using a skill.
	 * @param activeChar
	 * @param skill
	 * @param targets
	 * @param crit
	 * @throws IOException
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit) throws IOException;
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public SkillType[] getSkillIds();
}