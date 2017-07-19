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
package com.l2jmobius.gameserver.skills.effects;

import java.util.List;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.util.Util;
import com.l2jmobius.util.Rnd;

import javolution.util.FastList;

/**
 * @author littlecrow Implementation of the Confusion Effect
 */
final class EffectConfuseMob extends L2Effect
{
	public EffectConfuseMob(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CONFUSE_MOB_ONLY;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		getEffected().startConfused();
		onActionTime();
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		getEffected().stopConfused(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		final List<L2Character> targetList = new FastList<>();
		
		// Getting the possible targets
		for (final L2Object obj : getEffected().getKnownList().getKnownObjects().values())
		{
			
			if ((obj instanceof L2Attackable) && (obj != getEffected()) && Util.checkIfInShortRadius(600, getEffected(), obj, true))
			{
				targetList.add((L2Character) obj);
			}
		}
		
		// if there is no target, exit function
		if (targetList.size() == 0)
		{
			return true;
		}
		
		// Choosing randomly a new target
		final int nextTargetIdx = Rnd.nextInt(targetList.size());
		final L2Object target = targetList.get(nextTargetIdx);
		
		// Attacking the target
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
		return true;
	}
}