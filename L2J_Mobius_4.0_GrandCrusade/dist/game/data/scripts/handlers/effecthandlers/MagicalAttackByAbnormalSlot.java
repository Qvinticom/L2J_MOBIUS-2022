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
package handlers.effecthandlers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.enums.ShotType;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.L2EffectType;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.skills.AbnormalType;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.Formulas;
import com.l2jmobius.gameserver.model.stats.Stats;

/**
 * Magical Attack By Abnormal Slot effect implementation.
 * @author Sdw
 */
public final class MagicalAttackByAbnormalSlot extends AbstractEffect
{
	private final double _power;
	private final Set<AbnormalType> _abnormals;
	
	public MagicalAttackByAbnormalSlot(StatsSet params)
	{
		_power = params.getDouble("power", 0);
		
		final String abnormals = params.getString("abnormalType", null);
		if ((abnormals != null) && !abnormals.isEmpty())
		{
			_abnormals = new HashSet<>();
			for (String slot : abnormals.split(";"))
			{
				_abnormals.add(AbnormalType.getAbnormalType(slot));
			}
		}
		else
		{
			_abnormals = Collections.<AbnormalType> emptySet();
		}
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.MAGICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		if (effector.isAlikeDead() || _abnormals.stream().noneMatch(effected::hasAbnormalType))
		{
			return;
		}
		
		if (effected.isPlayer() && effected.getActingPlayer().isFakeDeath())
		{
			effected.stopFakeDeath(true);
		}
		
		final boolean sps = skill.useSpiritShot() && effector.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = skill.useSpiritShot() && effector.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final boolean mcrit = Formulas.calcCrit(skill.getMagicCriticalRate(), effector, effected, skill);
		final double damage = Formulas.calcMagicDam(effector, effected, skill, effector.getMAtk(), _power, effected.getMDef(), sps, bss, mcrit);
		
		// Manage attack or cast break of the target (calculating rate, sending message...)
		if (!effected.isRaid() && Formulas.calcAtkBreak(effected, damage))
		{
			effected.breakAttack();
			effected.breakCast();
		}
		
		// Shield Deflect Magic: Reflect all damage on caster.
		if (effected.getStat().getValue(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0) > Rnd.get(100))
		{
			effector.reduceCurrentHp(damage, effected, skill, false, false, mcrit, true);
		}
		else
		{
			effected.reduceCurrentHp(damage, effector, skill, false, false, mcrit, false);
			// effector.sendDamageMessage(effected, skill, (int) damage, mcrit, false);
			
			// Absorb HP from the damage inflicted
			double absorbPercent = effector.getStat().getValue(Stats.ABSORB_DAMAGE_PERCENT, 0) * effector.getStat().getValue(Stats.ABSORB_DAMAGE_DEFENCE, 1);
			if ((absorbPercent > 0) && (Rnd.nextDouble() < effector.getStat().getValue(Stats.ABSORB_DAMAGE_CHANCE)))
			{
				int absorbDamage = (int) Math.min(absorbPercent * damage, effector.getMaxRecoverableHp() - effector.getCurrentHp());
				absorbDamage = Math.min(absorbDamage, (int) effected.getCurrentHp());
				if (absorbDamage > 0)
				{
					effector.setCurrentHp(effector.getCurrentHp() + absorbDamage);
				}
			}
		}
	}
}