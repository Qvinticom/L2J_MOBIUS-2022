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
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Mana Heal effect implementation.
 * @author UnAfraid
 */
public class ManaHeal extends AbstractEffect
{
	private final double _power;
	
	public ManaHeal(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
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
		final Creature target = info.getEffected();
		if ((target == null) || target.isDead() || target.isDoor() || target.isInvul())
		{
			return;
		}
		
		double amount = _power;
		if (!info.getSkill().isStatic())
		{
			amount = target.calcStat(Stat.MANA_CHARGE, amount, null, null);
		}
		
		// Prevents overheal and negative amount
		amount = Math.max(Math.min(amount, target.getMaxRecoverableMp() - target.getCurrentMp()), 0);
		if (amount != 0)
		{
			target.setCurrentMp(amount + target.getCurrentMp());
		}
		SystemMessage sm;
		if (info.getEffector().getObjectId() != target.getObjectId())
		{
			sm = new SystemMessage(SystemMessageId.S2_MP_HAS_BEEN_RESTORED_BY_C1);
			sm.addString(info.getEffector().getName());
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.S1_MP_HAS_BEEN_RESTORED);
		}
		sm.addInt((int) amount);
		target.sendPacket(sm);
	}
}
