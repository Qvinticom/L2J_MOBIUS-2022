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
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.EffectPoint;
import org.l2jmobius.gameserver.model.skill.Env;

public class EffectSignetNoise extends Effect
{
	private EffectPoint _actor;
	
	public EffectSignetNoise(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.SIGNET_GROUND;
	}
	
	@Override
	public void onStart()
	{
		_actor = (EffectPoint) getEffected();
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getCount() == (getTotalCount() - 1))
		{
			return true; // do nothing first time
		}
		
		for (Creature target : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getSkillRadius()))
		{
			if (target == null)
			{
				continue;
			}
			
			for (Effect effect : target.getAllEffects())
			{
				if (effect.getSkill().isDance())
				{
					effect.exit(true);
				}
			}
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}
