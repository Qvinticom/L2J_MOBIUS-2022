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

import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.handler.SkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;

/**
 * This class ...
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */

public class BalanceLife implements ISkillHandler
{
	private static SkillType[] _skillIds =
	{
		SkillType.BALANCE_LIFE
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		
		// check for other effects
		try
		{
			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			
			if (handler != null)
			{
				handler.useSkill(activeChar, skill, targets, crit);
			}
		}
		catch (final Exception e)
		{
		}
		
		double fullHP = 0;
		double currentHPs = 0;
		
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			
			// We should not heal if char is dead
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}
		
		final double percentHP = currentHPs / fullHP;
		
		for (final L2Object target2 : targets)
		{
			final L2Character target = (L2Character) target2;
			
			final double newHP = target.getMaxHp() * percentHP;
			final double totalHeal = newHP - target.getCurrentHp();
			
			target.setCurrentHp(newHP);
			
			if (totalHeal > 0)
			{
				target.setLastHealAmount((int) totalHeal);
			}
			
			final StatusUpdate su = new StatusUpdate(target.getObjectId());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			target.sendPacket(su);
			
			target.sendMessage("HP of the party has been balanced.");
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}