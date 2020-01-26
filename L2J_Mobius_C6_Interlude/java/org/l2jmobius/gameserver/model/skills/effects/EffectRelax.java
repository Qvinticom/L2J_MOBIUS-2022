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
package org.l2jmobius.gameserver.model.skills.effects;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.skills.Env;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

class EffectRelax extends Effect
{
	public EffectRelax(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.RELAXING;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected() instanceof PlayerInstance)
		{
			setRelax(true);
			((PlayerInstance) getEffected()).sitDown();
		}
		else
		{
			getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
		super.onStart();
	}
	
	@Override
	public void onExit()
	{
		setRelax(false);
		super.onExit();
	}
	
	@Override
	public boolean onActionTime()
	{
		boolean retval = true;
		if (getEffected().isDead())
		{
			retval = false;
		}
		
		if ((getEffected() instanceof PlayerInstance) && !((PlayerInstance) getEffected()).isSitting())
		{
			retval = false;
		}
		
		if (((getEffected().getCurrentHp() + 1) > getEffected().getMaxHp()) && getSkill().isToggle())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Fully rested. Effect of " + getSkill().getName() + " has been removed.");
			getEffected().sendPacket(sm);
			retval = false;
		}
		
		final double manaDam = calc();
		
		if ((manaDam > getEffected().getCurrentMp()) && getSkill().isToggle())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			getEffected().sendPacket(sm);
			retval = false;
		}
		
		if (!retval)
		{
			setRelax(retval);
		}
		else
		{
			getEffected().reduceCurrentMp(manaDam);
		}
		
		return retval;
	}
	
	private void setRelax(boolean value)
	{
		if (getEffected() instanceof PlayerInstance)
		{
			((PlayerInstance) getEffected()).setRelax(value);
		}
	}
}
