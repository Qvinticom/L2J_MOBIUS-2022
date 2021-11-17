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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlEvent;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectFlag;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.stats.Formulas;

/**
 * Confuse effect implementation.
 * @author littlecrow
 */
public class Confuse extends AbstractEffect
{
	private final int _chance;
	
	public Confuse(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_chance = params.getInt("chance", 100);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		return Formulas.calcProbability(_chance, info.getEffector(), info.getEffected(), info.getSkill());
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.CONFUSED.getMask();
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (!info.getEffected().isPlayer())
		{
			info.getEffected().getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		info.getEffected().getAI().notifyEvent(CtrlEvent.EVT_CONFUSED);
		
		final List<Creature> targetList = new ArrayList<>();
		// Getting the possible targets
		World.getInstance().forEachVisibleObject(info.getEffected(), Creature.class, targetList::add);
		
		// if there is no target, exit function
		if (!targetList.isEmpty())
		{
			// Choosing randomly a new target
			final Creature target = targetList.get(Rnd.get(targetList.size()));
			// Attacking the target
			info.getEffected().setTarget(target);
			info.getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		}
	}
}
