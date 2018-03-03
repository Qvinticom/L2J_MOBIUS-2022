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
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.Env;

public final class EffectChameleonRest extends L2Effect
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
		final L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			setChameleon(true);
			((L2PcInstance) effected).setSilentMoving(true);
			((L2PcInstance) effected).sitDown();
		}
		else
		{
			effected.getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.L2Effect#onExit()
	 */
	@Override
	public void onExit()
	{
		setChameleon(false);
		
		final L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setSilentMoving(false);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		final L2Character effected = getEffected();
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
		
		if (effected instanceof L2PcInstance)
		{
			if (!((L2PcInstance) effected).isSitting())
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
		final L2Character effected = getEffected();
		if (effected instanceof L2PcInstance)
		{
			((L2PcInstance) effected).setRelax(val);
		}
	}
}
