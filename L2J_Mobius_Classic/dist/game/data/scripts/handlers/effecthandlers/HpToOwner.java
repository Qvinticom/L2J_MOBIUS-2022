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

import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.L2EffectType;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.stats.Formulas;

/**
 * HpToOwner effect implementation.
 * @author Sdw
 */
public final class HpToOwner extends AbstractEffect
{
	private final double _power;
	private final int _stealAmount;
	
	public HpToOwner(StatsSet params)
	{
		_power = params.getDouble("power");
		_stealAmount = params.getInt("stealAmount");
		setTicks(params.getInt("ticks"));
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (!info.getSkill().isToggle() && info.getSkill().isMagic())
		{
			// TODO: M.Crit can occur even if this skill is resisted. Only then m.crit damage is applied and not debuff
			final boolean mcrit = Formulas.calcCrit(info.getSkill().getMagicCriticalRate(), info.getEffector(), info.getEffected(), info.getSkill());
			if (mcrit)
			{
				final double damage = _power * 10; // Tests show that 10 times HP DOT is taken during magic critical.
				info.getEffected().reduceCurrentHp(damage, info.getEffector(), info.getSkill(), true, false, true, false);
			}
		}
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.DMG_OVER_TIME;
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (info.getEffected().isDead())
		{
			return false;
		}
		
		final double damage = _power * getTicksMultiplier();
		
		info.getEffected().reduceCurrentHp(damage, info.getEffector(), info.getSkill(), true, false, false, false);
		if (_stealAmount > 0)
		{
			final double amount = (damage * _stealAmount) / 100;
			info.getEffector().setCurrentHp(info.getEffector().getCurrentHp() + amount);
			info.getEffector().setCurrentMp(info.getEffector().getCurrentMp() + amount);
		}
		return info.getSkill().isToggle();
	}
}