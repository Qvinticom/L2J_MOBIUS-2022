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
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.skills.Env;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class EffectChameleonRest extends Effect
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
	
	@Override
	public void onStart()
	{
		final Creature effected = getEffected();
		if (effected instanceof PlayerInstance)
		{
			setChameleon(true);
			((PlayerInstance) effected).setSilentMoving(true);
			((PlayerInstance) effected).sitDown();
		}
		else
		{
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	@Override
	public void onExit()
	{
		setChameleon(false);
		
		final Creature effected = getEffected();
		if (effected instanceof PlayerInstance)
		{
			((PlayerInstance) effected).setSilentMoving(false);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		final Creature effected = getEffected();
		boolean retval = true;
		
		if (effected.isDead())
		{
			retval = false;
		}
		
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != SkillType.CONT)
		{
			return false;
		}
		
		if (effected instanceof PlayerInstance)
		{
			if (!((PlayerInstance) effected).isSitting())
			{
				retval = false;
			}
		}
		
		final double manaDam = calc();
		
		if (manaDam > effected.getStatus().getCurrentMp())
		{
			effected.sendPacket(new SystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP));
			return false;
		}
		
		if (!retval)
		{
			setChameleon(retval);
		}
		else
		{
			effected.reduceCurrentMp(manaDam);
		}
		
		return retval;
	}
	
	private void setChameleon(boolean val)
	{
		final Creature effected = getEffected();
		if (effected instanceof PlayerInstance)
		{
			((PlayerInstance) effected).setRelax(val);
		}
	}
}
