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
import org.l2jmobius.gameserver.model.actor.Player;
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
		if (getEffected() instanceof Player)
		{
			setRelax(true);
			((Player) getEffected()).sitDown();
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
		
		if ((getEffected() instanceof Player) && !((Player) getEffected()).isSitting())
		{
			retval = false;
		}
		
		if (((getEffected().getCurrentHp() + 1) > getEffected().getMaxHp()) && getSkill().isToggle())
		{
			getEffected().sendMessage("Fully rested. Effect of " + getSkill().getName() + " has been removed.");
			retval = false;
		}
		
		final double manaDam = calc();
		if ((manaDam > getEffected().getCurrentMp()) && getSkill().isToggle())
		{
			getEffected().sendPacket(new SystemMessage(SystemMessageId.YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP));
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
		if (getEffected() instanceof Player)
		{
			((Player) getEffected()).setRelax(value);
		}
	}
}
