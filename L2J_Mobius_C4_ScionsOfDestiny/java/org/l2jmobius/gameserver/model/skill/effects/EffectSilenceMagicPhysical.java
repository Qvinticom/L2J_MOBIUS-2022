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
import org.l2jmobius.gameserver.model.skill.Env;

public class EffectSilenceMagicPhysical extends Effect
{
	public EffectSilenceMagicPhysical(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return Effect.EffectType.SILENCE_MAGIC_PHYSICAL;
	}
	
	@Override
	public void onStart()
	{
		getEffected().startMuted();
		getEffected().startPsychicalMuted();
	}
	
	@Override
	public boolean onActionTime()
	{
		getEffected().stopMuted(this);
		getEffected().stopPsychicalMuted(this);
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopMuted(this);
		getEffected().stopPsychicalMuted(this);
	}
}
