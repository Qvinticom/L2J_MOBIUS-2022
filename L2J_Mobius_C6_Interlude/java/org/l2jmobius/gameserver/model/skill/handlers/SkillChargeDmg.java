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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.effects.Effect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.item.type.WeaponType;
import org.l2jmobius.gameserver.model.skill.BaseStat;
import org.l2jmobius.gameserver.model.skill.Formulas;
import org.l2jmobius.gameserver.model.skill.SkillTargetType;
import org.l2jmobius.gameserver.model.skill.effects.EffectCharge;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SkillChargeDmg extends Skill
{
	final int chargeSkillId;
	
	public SkillChargeDmg(StatSet set)
	{
		super(set);
		chargeSkillId = set.getInt("charge_skill_id");
	}
	
	@Override
	public boolean checkCondition(Creature creature, WorldObject target, boolean itemOrWeapon)
	{
		if (creature instanceof Player)
		{
			final Player player = (Player) creature;
			final EffectCharge e = (EffectCharge) player.getFirstEffect(chargeSkillId);
			if ((e == null) || (e.numCharges < getNumCharges()))
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
				sm.addSkillName(getId());
				creature.sendPacket(sm);
				return false;
			}
		}
		return super.checkCondition(creature, target, itemOrWeapon);
	}
	
	@Override
	public void useSkill(Creature caster, List<Creature> targets)
	{
		if (caster.isAlikeDead())
		{
			return;
		}
		
		// get the effect
		final EffectCharge effect = (EffectCharge) caster.getFirstEffect(chargeSkillId);
		if ((effect == null) || (effect.numCharges < getNumCharges()))
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addSkillName(getId());
			caster.sendPacket(sm);
			return;
		}
		
		double modifier = 0;
		modifier = (effect.getLevel() - getNumCharges()) * 0.33;
		if ((getTargetType() != SkillTargetType.AREA) && (getTargetType() != SkillTargetType.MULTIFACE))
		{
			effect.numCharges -= getNumCharges();
		}
		
		if (caster instanceof Player)
		{
			caster.sendPacket(new EtcStatusUpdate((Player) caster));
		}
		
		if (effect.numCharges == 0)
		{
			effect.exit(false);
		}
		
		final boolean ss = caster.checkSs();
		for (WorldObject target2 : targets)
		{
			final Item weapon = caster.getActiveWeaponInstance();
			final Creature target = (Creature) target2;
			if (target.isAlikeDead())
			{
				continue;
			}
			
			// TODO: should we use dual or not?
			// because if so, damage are lowered but we dont do anything special with dual then
			// like in doAttackHitByDual which in fact does the calcPhysDam call twice
			
			// boolean dual = caster.isUsingDualWeapon();
			final boolean shld = Formulas.calcShldUse(caster, target);
			final boolean soul = ((weapon != null) && (weapon.getChargedSoulshot() == Item.CHARGED_SOULSHOT) && (weapon.getItemType() != WeaponType.DAGGER));
			boolean crit = false;
			if (getBaseCritRate() > 0)
			{
				crit = Formulas.calcCrit(getBaseCritRate() * 10 * BaseStat.STR.calcBonus(caster));
			}
			
			// damage calculation
			int damage = (int) Formulas.calcPhysDam(caster, target, this, shld, false, false, soul);
			
			// Like L2OFF damage calculation crit is static 2x
			if (crit)
			{
				damage *= 2;
			}
			
			if (damage > 0)
			{
				double finalDamage = damage;
				finalDamage = finalDamage + (modifier * finalDamage);
				target.reduceCurrentHp(finalDamage, caster);
				caster.sendDamageMessage(target, (int) finalDamage, false, crit, false);
			}
			else
			{
				caster.sendDamageMessage(target, 0, false, false, true);
			}
		}
		
		if (ss)
		{
			caster.removeSs();
		}
		
		// effect self :]
		final Effect seffect = caster.getFirstEffect(getId());
		if ((seffect != null) && seffect.isSelfEffect())
		{
			// Replace old effect with new one.
			seffect.exit(false);
		}
		// cast self effect if any
		applySelfEffects(caster);
	}
}