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

import org.l2jmobius.gameserver.enums.EffectCalculationType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.stat.CreatureStat;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.model.stats.functions.FuncAdd;
import org.l2jmobius.gameserver.model.stats.functions.FuncMul;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Zealar
 */
public class MaxHp extends AbstractEffect
{
	private final double _power;
	private final EffectCalculationType _type;
	private final boolean _heal;
	
	public MaxHp(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_type = params.getEnum("type", EffectCalculationType.class, EffectCalculationType.DIFF);
		switch (_type)
		{
			case DIFF:
			{
				_power = params.getInt("power", 0);
				break;
			}
			default:
			{
				_power = 1 + (params.getInt("power", 0) / 100.0);
			}
		}
		_heal = params.getBoolean("heal", false);
		if (params.isEmpty())
		{
			LOGGER.warning(getClass().getSimpleName() + ": must have parameters.");
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Creature effected = info.getEffected();
		final CreatureStat charStat = effected.getStat();
		final double currentHp = effected.getCurrentHp();
		double amount = _power;
		
		synchronized (charStat)
		{
			switch (_type)
			{
				case DIFF:
				{
					charStat.getActiveChar().addStatFunc(new FuncAdd(Stat.MAX_HP, 1, this, _power, null));
					if (_heal)
					{
						effected.setCurrentHp((currentHp + _power));
					}
					break;
				}
				case PER:
				{
					final double maxHp = effected.getMaxHp();
					charStat.getActiveChar().addStatFunc(new FuncMul(Stat.MAX_HP, 1, this, _power, null));
					if (_heal)
					{
						amount = (_power - 1) * maxHp;
						effected.setCurrentHp(currentHp + amount);
					}
					break;
				}
			}
		}
		if (_heal)
		{
			final Creature caster = info.getEffector();
			if ((caster != null) && (caster != effected))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S2_HP_HAS_BEEN_RESTORED_BY_C1);
				sm.addString(caster.getName());
				sm.addInt((int) amount);
				effected.sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HP_HAS_BEEN_RESTORED);
				sm.addInt((int) amount);
				effected.sendPacket(sm);
			}
		}
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final CreatureStat charStat = info.getEffected().getStat();
		synchronized (charStat)
		{
			charStat.getActiveChar().removeStatsOwner(this);
		}
	}
}
