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

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.skill.Env;
import org.l2jmobius.gameserver.model.skill.SkillType;

/**
 * Infinity Spear effect like L2OFF Interlude (lineage2.com 2007 year)
 * @author Souverain, OnePaTuBHuK
 */
public class EffectHeroCancel extends Effect
{
	public EffectHeroCancel(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.HERO_CANCEL;
	}
	
	@Override
	public void onStart()
	{
		int maxdisp = (int) getSkill().getNegatePower();
		for (Effect e : getEffected().getAllEffects())
		{
			switch (e.getEffectType())
			{
				case SIGNET_GROUND:
				case SIGNET_EFFECT:
				{
					continue;
				}
			}
			
			if ((e.getSkill().getId() != 4082) && (e.getSkill().getId() != 4215) && (e.getSkill().getId() != 5182) && (e.getSkill().getId() != 4515) && (e.getSkill().getId() != 110) && (e.getSkill().getId() != 111) && (e.getSkill().getId() != 1323) && (e.getSkill().getId() != 1325))
			{
				if (e.getSkill().getSkillType() == SkillType.BUFF)
				{
					int rate = 10;
					
					if (Rnd.get(100) < rate)
					{
						e.exit(true);
						maxdisp--;
						if (maxdisp == 0)
						{
							break;
						}
					}
				}
			}
		}
	}
	
	@Override
	public void onExit()
	{
		// Do nothing.
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
