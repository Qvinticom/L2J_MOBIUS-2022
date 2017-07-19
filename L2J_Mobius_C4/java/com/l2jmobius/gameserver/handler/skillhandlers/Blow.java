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

import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;

/**
 * @author Steuf
 */
public class Blow implements ISkillHandler
{
	private static final SkillType[] SKILL_IDS =
	{
		SkillType.BLOW
	};
	
	public final static byte FRONT = 50;
	public final static byte SIDE = 60;
	public final static byte BEHIND = 70;
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean success)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		for (int index = 0; index < targets.length; index++)
		{
			final L2Character target = (L2Character) targets[index];
			
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// For the first target, it's already calculated
			if (index != 0)
			{
				byte _successChance = SIDE;
				
				if (activeChar.isBehindTarget())
				{
					_successChance = BEHIND;
				}
				else if (activeChar.isInFrontOfTarget())
				{
					_successChance = FRONT;
				}
				
				// If skill requires critical or skill requires behind,
				// calculate chance based on DEX, Position and on self BUFF
				success = true;
				if ((skill.getCondition() & L2Skill.COND_BEHIND) != 0)
				{
					success = (_successChance == BEHIND);
				}
				if ((skill.getCondition() & L2Skill.COND_CRIT) != 0)
				{
					success = (success && Formulas.getInstance().calcBlow(activeChar, target, _successChance));
				}
			}
			
			if (success)
			{
				final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
				final boolean soul = ((weapon != null) && (weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT));
				final boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
				
				final double damage = (int) Formulas.getInstance().calcBlowDamage(activeChar, target, skill, shld, soul);
				if (damage > 0)
				{
					// Manage attack or cast break of the target (calculating rate, sending message...)
					if (Formulas.getInstance().calcAtkBreak(target, damage))
					{
						target.breakAttack();
						target.breakCast();
					}
				}
				
				target.reduceCurrentHp(damage, activeChar);
				
				if (activeChar instanceof L2PcInstance)
				
				{
					if (((L2PcInstance) activeChar).isInOlympiadMode() && (target instanceof L2PcInstance))
					{
						((L2PcInstance) activeChar).dmgDealt += damage;
					}
					
					activeChar.sendPacket(new SystemMessage(SystemMessage.CRITICAL_HIT));
					final SystemMessage sm = new SystemMessage(SystemMessage.YOU_DID_S1_DMG);
					sm.addNumber((int) damage);
					activeChar.sendPacket(sm);
				}
				
			}
			
			if (skill.getId() == 344)
			{
				Formulas.getInstance().calcLethalStrike(activeChar, target, skill.getMagicLevel());
			}
			
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			
			// Self Effect
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit();
			}
			
			skill.getEffectsSelf(activeChar);
			
			// notify the AI that it is attacked
			target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar);
			
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return SKILL_IDS;
	}
}