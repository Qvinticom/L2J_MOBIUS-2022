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
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.skills.Formulas;

/**
 * @author _tomciaaa_
 */
public class StrSiegeAssault implements ISkillHandler
{
	protected SkillType[] _skillIds =
	{
		SkillType.STRSIEGEASSAULT
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		
		if ((activeChar == null) || !(activeChar instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance player = (L2PcInstance) activeChar;
		
		if (!activeChar.isRiding())
		{
			return;
		}
		
		try
		{
			// damage calculation
			int damage = 0;
			
			for (final L2Object target2 : targets)
			{
				final L2Character target = (L2Character) target2;
				final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				
				if (!(target instanceof L2DoorInstance))
				{
					return;
				}
				
				final boolean dual = activeChar.isUsingDualWeapon();
				final boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
				crit = Formulas.getInstance().calcCrit(activeChar.getCriticalHit(target, skill));
				final boolean soul = ((weapon != null) && (weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT));
				
				damage = (int) Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, crit, dual, soul);
				
				if (damage > 0)
				{
					target.reduceCurrentHp(damage, activeChar);
					activeChar.sendDamageMessage(target, damage, false, false, false);
				}
				
			}
			
			final L2ItemInstance itemToTake = player.getInventory().getItemByItemId(skill.getItemConsumeId());
			if (!player.destroyItem("Consume", itemToTake.getObjectId(), skill.getItemConsume(), null, true))
			{
				return;
			}
		}
		catch (final Exception e)
		{
			player.sendMessage("Error using siege assault:" + e);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}