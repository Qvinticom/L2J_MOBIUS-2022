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

import java.util.List;

import com.l2jmobius.gameserver.enums.ShotType;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.stats.Formulas;
import com.l2jmobius.gameserver.model.stats.Stats;
import com.l2jmobius.util.Rnd;

/**
 * damage for each mark on the target
 * @author hitnar
 */
public final class MarkRetriever extends AbstractEffect
{
	private final double _power;
	
	public MarkRetriever(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2Character effected = info.getEffected();
		final L2Character activeChar = info.getEffector();
		
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = info.getSkill().useSpiritShot() && activeChar.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = info.getSkill().useSpiritShot() && activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final boolean mcrit = Formulas.calcMCrit(activeChar.getMCriticalHit(effected, info.getSkill()));
		final byte shld = Formulas.calcShldUse(activeChar, effected, info.getSkill());
		double damage = Formulas.calcMagicDam(activeChar, effected, info.getSkill(), shld, sps, bss, mcrit);
		int count = 0;
		
		final List<BuffInfo> effects = effected.getEffectList().getEffects();
		for (BuffInfo buff : effects)
		{
			if ((buff.getSkill().getId() == 11259) || (buff.getSkill().getId() == 11261) || (buff.getSkill().getId() == 11262))
			{
				count++;
			}
		}
		
		damage = damage * _power * count;
		
		if (damage > 0)
		{
			// reduce damage if target has maxdamage buff
			final double maxDamage = (effected.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null));
			if (maxDamage > 0)
			{
				damage = maxDamage;
			}
			
			// Manage attack or cast break of the target (calculating rate, sending message...)
			if (!effected.isRaid() && Formulas.calcAtkBreak(effected, damage))
			{
				effected.breakAttack();
				effected.breakCast();
			}
			
			// Shield Deflect Magic: Reflect all damage on caster.
			if (effected.getStat().calcStat(Stats.VENGEANCE_SKILL_MAGIC_DAMAGE, 0, effected, info.getSkill()) > Rnd.get(100))
			{
				activeChar.reduceCurrentHp((int) damage, effected, info.getSkill());
				activeChar.notifyDamageReceived((int) damage, effected, info.getSkill(), false, false);
			}
			else
			{
				effected.reduceCurrentHp((int) damage, activeChar, info.getSkill());
				effected.notifyDamageReceived((int) damage, activeChar, info.getSkill(), false, false);
				activeChar.sendDamageMessage(effected, (int) damage, false, false, false);
			}
			
			effected.getEffectList().stopSkillEffects(true, 11259);
			effected.getEffectList().stopSkillEffects(true, 11261);
			effected.getEffectList().stopSkillEffects(true, 11262);
		}
	}
}
