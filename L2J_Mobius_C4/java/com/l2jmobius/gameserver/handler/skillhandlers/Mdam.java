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
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.util.Log;

/**
 * This class ...
 * @version $Revision: 1.1.2.8.2.9 $ $Date: 2005/04/05 19:41:23 $
 */
public class Mdam implements ISkillHandler
{
	// private static Logger _log = Logger.getLogger(Mdam.class.getName());
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	private static SkillType[] _skillIds =
	{
		SkillType.MDAM,
		SkillType.DEATHLINK
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets, boolean mcrit)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		boolean ss = false;
		boolean bss = false;
		
		final L2ItemInstance weaponInst = activeChar.getActiveWeaponInstance();
		
		if (weaponInst != null)
		{
			if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
			}
			else if (weaponInst.getChargedSpiritshot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
			}
		}
		// If there is no weapon equipped, check for an active summon.
		else if (activeChar instanceof L2Summon)
		{
			final L2Summon activeSummon = (L2Summon) activeChar;
			
			if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_BLESSED_SPIRITSHOT)
			{
				bss = true;
			}
			else if (activeSummon.getChargedSpiritShot() == L2ItemInstance.CHARGED_SPIRITSHOT)
			{
				ss = true;
			}
		}
		else if (activeChar instanceof L2NpcInstance)
		{
			bss = ((L2NpcInstance) activeChar).isUsingShot(false);
			ss = ((L2NpcInstance) activeChar).isUsingShot(true);
		}
		
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
			
			mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, skill));
			
			final int damage = (int) Formulas.getInstance().calcMagicDam(activeChar, target, skill, ss, bss, mcrit);
			
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
				Log.add(activeChar.getName() + "(" + activeChar.getObjectId() + ") " + activeChar.getLevel() + " lvl did damage " + damage + " with skill " + skill.getName() + "(" + skill.getId() + ") to " + name, "damage_mdam");
			}
			
			// Why are we trying to reduce the current target HP here?
			// Why not inside the below "if" condition, after the effects processing as it should be?
			// It doesn't seem to make sense for me. I'm moving this line inside the "if" condition, right after the effects processing...
			// [changed by nexus - 2006-08-15]
			// target.reduceCurrentHp(damage, activeChar);
			
			if (damage > 0)
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				
				if (skill.hasEffects())
				{
					
					if (Formulas.getInstance().calcSkillSuccess(activeChar, target, skill, false, ss, bss))
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
		}
		
		final L2Effect effect = activeChar.getFirstEffect(skill.getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			effect.exit();
		}
		
		skill.getEffectsSelf(activeChar);
		
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