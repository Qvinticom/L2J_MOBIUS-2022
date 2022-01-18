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
package org.l2jmobius.gameserver.model;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.skill.effects.EffectForce;

/**
 * @author kombat
 */
public class ForceBuff
{
	protected int _forceId;
	protected int _forceLevel;
	protected Creature _caster;
	protected Creature _target;
	
	static final Logger LOGGER = Logger.getLogger(ForceBuff.class.getName());
	
	public Creature getCaster()
	{
		return _caster;
	}
	
	public Creature getTarget()
	{
		return _target;
	}
	
	public ForceBuff(Creature caster, Creature target, Skill skill)
	{
		_caster = caster;
		_target = target;
		_forceId = skill.getTriggeredId();
		_forceLevel = skill.getTriggeredLevel();
		
		final Effect effect = _target.getFirstEffect(_forceId);
		if (effect != null)
		{
			((EffectForce) effect).increaseForce();
		}
		else
		{
			final Skill force = SkillTable.getInstance().getSkill(_forceId, _forceLevel);
			if (force != null)
			{
				force.applyEffects(_caster, _target, false, false, false);
			}
			else
			{
				LOGGER.warning("Triggered skill [" + _forceId + ";" + _forceLevel + "] not found!");
			}
		}
	}
	
	public void onCastAbort()
	{
		_caster.setForceBuff(null);
		final Effect effect = _target.getFirstEffect(_forceId);
		if (effect != null)
		{
			if (effect instanceof EffectForce)
			{
				((EffectForce) effect).decreaseForce();
			}
			else
			{
				effect.exit(false);
			}
		}
	}
}
