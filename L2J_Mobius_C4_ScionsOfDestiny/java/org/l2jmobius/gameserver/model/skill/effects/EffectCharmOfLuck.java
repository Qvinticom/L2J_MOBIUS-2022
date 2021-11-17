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
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.skill.Env;

/**
 * @author kerberos_20
 */
public class EffectCharmOfLuck extends Effect
{
	public EffectCharmOfLuck(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.CHARM_OF_LUCK;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected() instanceof Playable)
		{
			((Playable) getEffected()).startCharmOfLuck(this);
		}
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof Playable)
		{
			((Playable) getEffected()).stopCharmOfLuck(this);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		// just stop this effect
		return false;
	}
}
