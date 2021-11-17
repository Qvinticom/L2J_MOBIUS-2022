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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.skill.Env;
import org.l2jmobius.gameserver.util.Util;

/**
 * @author kombat
 */
public class EffectForce extends Effect
{
	protected static final Logger LOGGER = Logger.getLogger(EffectForce.class.getName());
	
	public int forces = 0;
	private int _range = -1;
	
	public EffectForce(Env env, EffectTemplate template)
	{
		super(env, template);
		forces = getSkill().getLevel();
		_range = getSkill().getCastRange();
	}
	
	@Override
	public boolean onActionTime()
	{
		return Util.checkIfInRange(_range, getEffector(), getEffected(), true);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	public void increaseForce()
	{
		forces++;
		updateBuff();
	}
	
	public void decreaseForce()
	{
		forces--;
		if (forces < 1)
		{
			exit(false);
		}
		else
		{
			updateBuff();
		}
	}
	
	public void updateBuff()
	{
		exit(false);
		final Skill newSkill = SkillTable.getInstance().getSkill(getSkill().getId(), forces);
		if (newSkill != null)
		{
			newSkill.applyEffects(getEffector(), getEffected(), false, false, false);
		}
	}
	
	@Override
	public void onExit()
	{
	}
}
