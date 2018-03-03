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

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.skills.Env;

/**
 * @author decad
 */
final class EffectBetray extends L2Effect
{
	public EffectBetray(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BETRAY;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if ((getEffected() != null) && (getEffector() instanceof L2PcInstance) && (getEffected() instanceof L2Summon))
		{
			L2PcInstance targetOwner = null;
			targetOwner = ((L2Summon) getEffected()).getOwner();
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, targetOwner);
			targetOwner.setIsBetrayed(true);
			onActionTime();
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		if ((getEffected() != null) && (getEffector() instanceof L2PcInstance) && (getEffected() instanceof L2Summon))
		{
			L2PcInstance targetOwner = null;
			targetOwner = ((L2Summon) getEffected()).getOwner();
			targetOwner.setIsBetrayed(false);
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		L2PcInstance targetOwner = null;
		targetOwner = ((L2Summon) getEffected()).getOwner();
		targetOwner.setIsBetrayed(true);
		return false;
	}
}
