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

import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Cubic;
import org.l2jmobius.gameserver.model.skill.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.StatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SkillDrain extends Skill
{
	private final float _absorbPart;
	private final int _absorbAbs;
	
	public SkillDrain(StatSet set)
	{
		super(set);
		
		_absorbPart = set.getFloat("absorbPart", 0.f);
		_absorbAbs = set.getInt("absorbAbs", 0);
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
			if (creature.isPlayable() && target.isInvul())
			{
				continue;
			}
			
			if (target.isAlikeDead() && (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				continue;
			}
			
			// Like L2OFF no effect on invul object except Npcs
			if ((creature != target) && (target.isInvul() && !target.isNpc()))
			{
				continue; // No effect on invulnerable chars unless they cast it themselves.
			}
			
			final boolean mcrit = Formulas.calcMCrit(creature.getMCriticalHit(target, this));
			final int damage = (int) Formulas.calcMagicDam(creature, target, this, sps, bss, mcrit);
			int drain = 0;
			final int currentCp = (int) target.getStatus().getCurrentCp();
			final int currentHp = (int) target.getStatus().getCurrentHp();
			if (currentCp > 0)
			{
				if (damage < currentCp)
				{
					drain = 0;
				}
				else
				{
					drain = damage - currentCp;
				}
			}
			else if (damage > currentHp)
			{
				drain = currentHp;
			}
			else
			{
				drain = damage;
			}
			
			final double hpAdd = _absorbAbs + (_absorbPart * drain);
			final double hp = (creature.getCurrentHp() + hpAdd) > creature.getMaxHp() ? creature.getMaxHp() : creature.getCurrentHp() + hpAdd;
			creature.setCurrentHp(hp);
			
			final StatusUpdate suhp = new StatusUpdate(creature.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			creature.sendPacket(suhp);
			
			// Check to see if we should damage the target
			if ((damage > 0) && (!target.isDead() || (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)))
			{
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				
				creature.sendDamageMessage(target, damage, mcrit, false, false);
				if (hasEffects() && (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
				{
					if (target.reflectSkill(this))
					{
						creature.stopSkillEffects(getId());
						applyEffects(null, creature, false, sps, bss);
						final SystemMessage sm = new SystemMessage(SystemMessageId.THE_EFFECTS_OF_S1_FLOW_THROUGH_YOU);
						sm.addSkillName(getId());
						creature.sendPacket(sm);
					}
					else
					{
						// activate attacked effects, if any
						target.stopSkillEffects(getId());
						if (Formulas.getInstance().calcSkillSuccess(creature, target, this, false, sps, bss))
						{
							applyEffects(creature, target, false, sps, bss);
						}
						else
						{
							final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_RESISTED_YOUR_S2);
							sm.addString(target.getName());
							sm.addSkillName(getDisplayId());
							creature.sendPacket(sm);
						}
					}
				}
				
				target.reduceCurrentHp(damage, creature);
			}
			
			// Check to see if we should do the decay right after the cast
			if (target.isDead() && (getTargetType() == SkillTargetType.TARGET_CORPSE_MOB) && (target instanceof Npc))
			{
				((Npc) target).endDecayTask();
			}
		}
		
		if (bss)
		{
			creature.removeBss();
		}
		else if (sps)
		{
			creature.removeSps();
		}
		
		// effect self :]
		final Effect effect = creature.getFirstEffect(getId());
		if ((effect != null) && effect.isSelfEffect())
		{
			// Replace old effect with new one.
			effect.exit(false);
		}
		// cast self effect if any
		applySelfEffects(creature);
	}
	
	public void useCubicSkill(Cubic activeCubic, List<Creature> targets)
	{
		for (Creature target : targets)
		{
			if (target.isAlikeDead() && (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB))
			{
				continue;
			}
			
			final boolean mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(target, this));
			final int damage = (int) Formulas.calcMagicDam(activeCubic, target, this, mcrit);
			final double hpAdd = _absorbAbs + (_absorbPart * damage);
			final Player owner = activeCubic.getOwner();
			final double hp = ((owner.getCurrentHp() + hpAdd) > owner.getMaxHp() ? owner.getMaxHp() : (owner.getCurrentHp() + hpAdd));
			owner.setCurrentHp(hp);
			
			final StatusUpdate suhp = new StatusUpdate(owner.getObjectId());
			suhp.addAttribute(StatusUpdate.CUR_HP, (int) hp);
			owner.sendPacket(suhp);
			
			// Check to see if we should damage the target
			if ((damage > 0) && (!target.isDead() || (getTargetType() != SkillTargetType.TARGET_CORPSE_MOB)))
			{
				target.reduceCurrentHp(damage, activeCubic.getOwner());
				
				// Manage attack or cast break of the target (calculating rate, sending message...)
				if (!target.isRaid() && Formulas.calcAtkBreak(target, damage))
				{
					target.breakAttack();
					target.breakCast();
				}
				owner.sendDamageMessage(target, damage, mcrit, false, false);
			}
		}
	}
}