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
package com.l2jmobius.gameserver.skills.l2skills;

import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.gameserver.templates.StatsSet;

public class L2SkillDrain extends L2Skill
{
	private final float absorbPart;
	private final int absorbAbs;
	
	public L2SkillDrain(StatsSet set)
	{
		super(set);
		
		absorbPart = set.getFloat("absorbPart", 0.f);
		absorbAbs = set.getInteger("absorbAbs", 0);
	}
	
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
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
			final L2Character target = (L2Character) target2;
			if (target.isAlikeDead() && (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				continue;
			}
			
			// No effect on invulnerable chars unless they cast it themselves.
			if ((activeChar != target) && target.isInvul())
			{
				continue;
			}
			
			final boolean mcrit = Formulas.getInstance().calcMCrit(activeChar.getMCriticalHit(target, this));
			final int damage = (int) Formulas.getInstance().calcMagicDam(activeChar, target, this, ss, bss, mcrit);
			
			int _drain = 0;
			final int _cp = (int) target.getCurrentCp();
			final int _hp = (int) target.getCurrentHp();
			
			if (_cp > 0)
			{
				if (damage < _cp)
				{
					_drain = 0;
				}
				else
				{
					_drain = damage - _cp;
				}
			}
			
			else if (damage > _hp)
			{
				_drain = _hp;
			}
			else
			{
				_drain = damage;
			}
			
			final double hpAdd = absorbAbs + (absorbPart * _drain);
			final double hp = ((activeChar.getCurrentHp() + hpAdd) > activeChar.getMaxHp() ? activeChar.getMaxHp() : (activeChar.getCurrentHp() + hpAdd));
			
			activeChar.setCurrentHp(hp);
			
			final StatusUpdate suhp = new StatusUpdate(activeChar.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			activeChar.sendPacket(suhp);
			
			// Check to see if we should damage the target
			if ((damage > 0) && (!target.isDead() || (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)))
			{
				target.reduceCurrentHp(damage, activeChar);
				
				if (Formulas.getInstance().calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				activeChar.sendDamageMessage(target, damage, mcrit, false, false);
				
			}
			
			// Check to see if we should do the decay right after the cast
			if (target.isDead() && (getTargetType() == SkillTargetType.TARGET_CORPSE_MOB) && (target instanceof L2NpcInstance))
			{
				((L2NpcInstance) target).endDecayTask();
			}
		}
	}
}