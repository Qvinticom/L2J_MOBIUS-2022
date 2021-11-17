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

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Cp Heal effect implementation.
 * @author UnAfraid
 */
public class CpHeal extends AbstractEffect
{
	private final double _power;
	
	public CpHeal(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CPHEAL;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Creature target = info.getEffected();
		if ((target == null) || target.isDead() || target.isDoor())
		{
			return;
		}
		
		double amount = _power;
		
		// Prevents overheal and negative amount
		amount = Math.max(Math.min(amount, target.getMaxRecoverableCp() - target.getCurrentCp()), 0);
		if (amount != 0)
		{
			target.setCurrentCp(amount + target.getCurrentCp());
		}
		
		final Creature caster = info.getEffector();
		if ((caster != null) && (caster != target))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S2_CP_HAS_BEEN_RESTORED_BY_C1);
			sm.addString(caster.getName());
			sm.addInt((int) amount);
			target.sendPacket(sm);
		}
		else
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CP_HAS_BEEN_RESTORED);
			sm.addInt((int) amount);
			target.sendPacket(sm);
		}
	}
}
