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
import com.l2jmobius.gameserver.model.L2Skill.SkillType;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Env;

class EffectChameleonRest extends L2Effect
{
	public EffectChameleonRest(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setRelax(true);
			((L2PcInstance) getEffected()).setSilentMoving(true);
			((L2PcInstance) getEffected()).sitDown();
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
		
		super.onStart();
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	{
		
		if (getEffected() instanceof L2PcInstance)
		{
			((L2PcInstance) getEffected()).setRelax(false);
			((L2PcInstance) getEffected()).setSilentMoving(false);
		}
		
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		boolean retval = true;
		final L2PcInstance effected = (L2PcInstance) getEffected();
		
		if (getEffected().isDead())
		{
			retval = false;
		}
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != SkillType.CONT)
		{
			return false;
		}
		
		if (!effected.isSitting())
		{
			retval = false;
		}
		
		final double manaDam = calc();
		if (manaDam > effected.getCurrentMp())
		{
			final SystemMessage sm = new SystemMessage(140);
			effected.sendPacket(sm);
			return false;
		}
		
		if (!retval)
		{
			effected.setRelax(retval);
		}
		else
		{
			effected.reduceCurrentMp(manaDam);
		}
		
		return retval;
	}
}