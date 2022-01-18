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
package org.l2jmobius.gameserver.model.skill.handlers;

import java.util.List;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.skill.Formulas;

public class SkillElemental extends Skill
{
	private final int[] _seeds;
	private final boolean _seedAny;
	
	public SkillElemental(StatSet set)
	{
		super(set);
		
		_seeds = new int[3];
		_seeds[0] = set.getInt("seed1", 0);
		_seeds[1] = set.getInt("seed2", 0);
		_seeds[2] = set.getInt("seed3", 0);
		if (set.getInt("seed_any", 0) == 1)
		{
			_seedAny = true;
		}
		else
		{
			_seedAny = false;
		}
	}
	
	@Override
	public void useSkill(Creature creature, List<Creature> targets)
	{
		if (creature.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = creature.checkSps();
		final boolean bss = creature.checkBss();
		for (WorldObject target2 : targets)
		{
			final Creature target = (Creature) target2;
			if (target.isAlikeDead())
			{
				continue;
			}
			
			boolean charged = true;
			if (!_seedAny)
			{
				for (int seed : _seeds)
				{
					if (seed != 0)
					{
						final Effect e = target.getFirstEffect(seed);
						if ((e == null) || !e.getInUse())
						{
							charged = false;
							break;
						}
					}
				}
			}
			else
			{
				charged = false;
				for (int seed : _seeds)
				{
					if (seed != 0)
					{
						final Effect e = target.getFirstEffect(seed);
						if ((e != null) && e.getInUse())
						{
							charged = true;
							break;
						}
					}
				}
			}
			if (!charged)
			{
				creature.sendMessage("Target is not charged by elements.");
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(creature.getMCriticalHit(target, this));
			final int damage = (int) Formulas.calcMagicDam(creature, target, this, sps, bss, mcrit);
			if (damage > 0)
			{
				target.reduceCurrentHp(damage, creature);
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				creature.sendDamageMessage(target, damage, false, false, false);
			}
			
			// activate attacked effects, if any
			target.stopSkillEffects(getId());
			applyEffects(creature, target, false, sps, bss);
		}
		
		if (bss)
		{
			creature.removeBss();
		}
		else if (sps)
		{
			creature.removeSps();
		}
	}
}
