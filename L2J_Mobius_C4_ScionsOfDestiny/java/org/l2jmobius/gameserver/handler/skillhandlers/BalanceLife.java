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
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.handler.SkillHandler;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.skill.SkillType;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;

/**
 * @author earendil
 * @version $Revision: 1.1.2.2.2.4 $ $Date: 2005/04/06 16:13:48 $
 */
public class BalanceLife implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.BALANCE_LIFE
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		// Check for other effects.
		try
		{
			final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.BUFF);
			if (handler != null)
			{
				handler.useSkill(creature, skill, targets);
			}
		}
		catch (Exception e)
		{
		}
		
		Creature target = null;
		double fullHP = 0;
		double currentHPs = 0;
		for (WorldObject target2 : targets)
		{
			target = (Creature) target2;
			
			// We should not heal if char is dead.
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
			// Avoid characters heal inside Baium lair from outside.
			if (((GrandBossManager.getInstance().getZone(creature) == null) && (GrandBossManager.getInstance().getZone(target) != null)) || ((GrandBossManager.getInstance().getZone(target) == null) && (GrandBossManager.getInstance().getZone(creature) != null)))
			{
				continue;
			}
			
			fullHP += target.getMaxHp();
			currentHPs += target.getCurrentHp();
		}
		
		final double percentHP = currentHPs / fullHP;
		for (WorldObject target2 : targets)
		{
			target = (Creature) target2;
			if ((target == null) || target.isDead())
			{
				continue;
			}
			
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
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
