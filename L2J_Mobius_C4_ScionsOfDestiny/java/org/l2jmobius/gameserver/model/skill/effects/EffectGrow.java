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

import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.skill.Env;

public class EffectGrow extends Effect
{
	public EffectGrow(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BUFF;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected() instanceof Npc)
		{
			final Npc npc = (Npc) getEffected();
			npc.setCollisionHeight((int) (npc.getCollisionHeight() * 1.24));
			npc.setCollisionRadius((int) (npc.getCollisionRadius() * 1.19));
			getEffected().startAbnormalEffect(Creature.ABNORMAL_EFFECT_GROW);
		}
	}
	
	@Override
	public boolean onActionTime()
	{
		if (getEffected() instanceof Npc)
		{
			final Npc npc = (Npc) getEffected();
			npc.setCollisionHeight(npc.getTemplate().getCollisionHeight());
			npc.setCollisionRadius(npc.getTemplate().getCollisionRadius());
			
			getEffected().stopAbnormalEffect(Creature.ABNORMAL_EFFECT_GROW);
		}
		return false;
	}
	
	@Override
	public void onExit()
	{
		if (getEffected() instanceof Npc)
		{
			final Npc npc = (Npc) getEffected();
			npc.setCollisionHeight(npc.getTemplate().getCollisionHeight());
			npc.setCollisionRadius(npc.getTemplate().getCollisionRadius());
			
			getEffected().stopAbnormalEffect(Creature.ABNORMAL_EFFECT_GROW);
		}
	}
}
