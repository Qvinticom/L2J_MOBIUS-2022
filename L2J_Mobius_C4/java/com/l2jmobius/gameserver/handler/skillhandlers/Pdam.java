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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.handler.ISkillHandler;
import com.l2jmobius.gameserver.handler.SkillHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.BaseStats;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.util.Log;

/**
 * This class ...
 * @version $Revision: 1.1.2.7.2.16 $ $Date: 2005/04/06 16:13:49 $
 */
public class Pdam implements ISkillHandler
{
	// all the items ids that this handler knowns
	private static Logger _log = Logger.getLogger(Pdam.class.getName());
	
	private static SkillType[] _skillIds =
	{
		SkillType.PDAM,
		SkillType.FATAL
	};
	
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean crit)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("Begin Skill processing in Pdam.java " + skill.getSkillType());
		}
		
		final L2ItemInstance weapon = activeChar.getActiveWeaponInstance();
		final boolean soul = ((weapon != null) && (weapon.getChargedSoulshot() == L2ItemInstance.CHARGED_SOULSHOT));
		
		for (final L2Object target2 : targets)
		{
			L2Character target = (L2Character) target2;
			
			if ((activeChar instanceof L2PcInstance) && (target instanceof L2PcInstance) && target.isFakeDeath())
			{
				target.stopFakeDeath(null);
			}
			else if (target.isDead())
			{
				continue;
			}
			
			final boolean dual = activeChar.isUsingDualWeapon();
			final boolean shld = Formulas.getInstance().calcShldUse(activeChar, target);
			crit = false;
			if (skill.getBaseCritRate() > 0)
			{
				crit = Formulas.getInstance().calcCrit(skill.getBaseCritRate() * 10 * BaseStats.STR.calcBonus(activeChar));
			}
			
			double damage = Formulas.getInstance().calcPhysDam(activeChar, target, skill, shld, false, dual, soul);
			
			if (!crit && ((skill.getCondition() & L2Skill.COND_CRIT) != 0))
			{
				damage = 0;
			}
			
			if (crit)
			{
				damage *= 2;
			}
			
			if ((damage > 5000) && (activeChar instanceof L2PcInstance))
			{
				String name = "";
				if (target instanceof L2RaidBossInstance)
				{
					name = "RaidBoss ";
				}
				if (target instanceof L2NpcInstance)
				{
					name += target.getName() + "(" + ((L2NpcInstance) target).getTemplate().npcId + ")";
				}
				if (target instanceof L2PcInstance)
				{
					name = target.getName() + "(" + target.getObjectId() + ") ";
				}
				name += target.getLevel() + " lvl";
				Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_pdam");
			}
			
			if (damage > 0)
			{
				activeChar.sendDamageMessage(target, (int) damage, false, crit, false);
				
				if (skill.hasEffects())
				{
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, soul, false, false))
					
					{
						if (Formulas.getInstance().calculateSkillReflect(skill, activeChar, target))
						{
							target = activeChar;
						}
						
						// activate attacked effects, if any
						target.stopEffect(skill.getId());
						if (target.getFirstEffect(skill.getId()) != null)
						{
							target.removeEffect(target.getFirstEffect(skill.getId()));
						}
						
						skill.getEffects(activeChar, target);
						
					}
					
					else
					{
						final SystemMessage sm = new SystemMessage(SystemMessage.S1_WAS_UNAFFECTED_BY_S2);
						sm.addString(target.getName());
						sm.addSkillName(skill.getDisplayId());
						activeChar.sendPacket(sm);
					}
				}
				
				target.reduceCurrentHp(damage, activeChar);
			}
			else
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.ATTACK_FAILED));
			}
			
			// Sonic Rage & Raging Force
			
			if (skill.getMaxCharges() > 0)
			{
				
				if (activeChar instanceof L2PcInstance)
				{
					
					if (((L2PcInstance) activeChar).getCharges() < skill.getMaxCharges())
					{
						((L2PcInstance) activeChar).addCharge(1);
					}
				}
			}
			
			if (skill.getId() == 343)
			{
				Formulas.getInstance().calcLethalStrike(activeChar, target, skill.getMagicLevel());
			}
			
			if (skill.getId() == 348)
			{
				// check for other effects
				try
				{
					final ISkillHandler handler = SkillHandler.getInstance().getSkillHandler(SkillType.SPOIL);
					
					if (handler != null)
					{
						handler.useSkill(activeChar, skill, targets, crit);
					}
				}
				catch (final Exception e)
				{
				}
			}
			
			final L2Effect effect = activeChar.getFirstEffect(skill.getId());
			if ((effect != null) && effect.isSelfEffect())
			{
				effect.exit();
			}
			
			skill.getEffectsSelf(activeChar);
			
		}
		
		if (skill.isSuicideAttack())
		{
			activeChar.doDie(null);
		}
	}
	
	@Override
	public SkillType[] getSkillIds()
	{
		return _skillIds;
	}
}
