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
package org.l2jmobius.gameserver.model.skill.effects;

import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.skill.Env;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Java
 */
public class EffectDeflectBuff extends Effect
{
	public EffectDeflectBuff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.PREVENT_BUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		// Only cont skills shouldn't end
		if (getSkill().getSkillType() != SkillType.CONT)
		{
			return false;
		}
		
		final double manaDam = calc();
		if (manaDam > getEffected().getCurrentMp())
		{
			getEffected().sendPacket(new SystemMessage(SystemMessageId.YOUR_SKILL_WAS_REMOVED_DUE_TO_A_LACK_OF_MP));
			return false;
		}
		
		getEffected().reduceCurrentMp(manaDam);
		return true;
	}
	
	@Override
	public void onStart()
	{
		getEffected().setBuffProtected(true);
	}
	
	@Override
	public void onExit()
	{
		getEffected().setBuffProtected(false);
	}
}
