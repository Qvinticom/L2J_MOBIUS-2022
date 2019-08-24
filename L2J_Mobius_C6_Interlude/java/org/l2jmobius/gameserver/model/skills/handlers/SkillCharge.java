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
package org.l2jmobius.gameserver.model.skills.handlers;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatsSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.skills.effects.EffectCharge;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SkillCharge extends Skill
{
	
	public SkillCharge(StatsSet set)
	{
		super(set);
	}
	
	@Override
	public boolean checkCondition(Creature creature, WorldObject target, boolean itemOrWeapon)
	{
		if (creature instanceof PlayerInstance)
		{
			final EffectCharge e = (EffectCharge) creature.getFirstEffect(this);
			if ((e != null) && (e.numCharges >= getNumCharges()))
			{
				creature.sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
				sm.addSkillName(getId());
				creature.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(creature, target, itemOrWeapon);
	}
	
	@Override
	public void useSkill(Creature caster, WorldObject[] targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		// get the effect
		EffectCharge effect = null;
		if (caster instanceof PlayerInstance)
		{
			effect = ((PlayerInstance) caster).getChargeEffect();
		}
		else
		{
			effect = (EffectCharge) caster.getFirstEffect(this);
		}
		
		if (effect != null)
		{
			if (effect.numCharges < getNumCharges())
			{
				effect.numCharges++;
				if (caster instanceof PlayerInstance)
				{
					caster.sendPacket(new EtcStatusUpdate((PlayerInstance) caster));
					final SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
					sm.addNumber(effect.numCharges);
					caster.sendPacket(sm);
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_MAXIMUM);
				caster.sendPacket(sm);
			}
			return;
		}
		getEffects(caster, caster, false, false, false);
		
		// cast self effect if any
		getEffectsSelf(caster);
	}
	
}
