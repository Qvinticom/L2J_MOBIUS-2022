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
package handlers.effecthandlers;

import com.l2jmobius.gameserver.enums.ShotType;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.L2EffectType;
import com.l2jmobius.gameserver.model.items.L2Weapon;
import com.l2jmobius.gameserver.model.items.type.WeaponType;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.BaseStats;
import com.l2jmobius.gameserver.model.stats.Formulas;
import com.l2jmobius.gameserver.model.stats.Stats;
import com.l2jmobius.util.Rnd;

/**
 * Energy Attack effect implementation.
 * @author NosBit
 */
public final class EnergyAttack extends AbstractEffect
{
	private final double _power;
	private final int _criticalChance;
	private final boolean _ignoreShieldDefence;
	
	public EnergyAttack(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
		_criticalChance = params.getInt("criticalChance", 0);
		_ignoreShieldDefence = params.getBoolean("ignoreShieldDefence", false);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		// TODO: Verify this on retail
		return !Formulas.calcPhysicalSkillEvasion(info.getEffector(), info.getEffected(), info.getSkill());
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PHYSICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final L2PcInstance attacker = info.getEffector() instanceof L2PcInstance ? (L2PcInstance) info.getEffector() : null;
		if (attacker == null)
		{
			return;
		}
		
		final L2Character target = info.getEffected();
		final Skill skill = info.getSkill();
		
		double attack = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		
		if (!_ignoreShieldDefence)
		{
			switch (Formulas.calcShldUse(attacker, target, skill, true))
			{
				case Formulas.SHIELD_DEFENSE_FAILED:
				{
					break;
				}
				case Formulas.SHIELD_DEFENSE_SUCCEED:
				{
					defence += target.getShldDef();
					break;
				}
				case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK:
				{
					defence = -1;
					break;
				}
			}
		}
		
		double damage = 1;
		boolean critical = false;
		
		if (defence != -1)
		{
			final double damageMultiplier = Formulas.calcWeaponTraitBonus(attacker, target) * Formulas.calcAttributeBonus(attacker, target, skill) * Formulas.calcGeneralTraitBonus(attacker, target, skill.getTraitType(), true);
			
			final boolean ss = info.getSkill().useSoulShot() && attacker.isChargedShot(ShotType.SOULSHOTS);
			final double ssBoost = ss ? 2 : 1.0;
			
			double weaponTypeBoost;
			final L2Weapon weapon = attacker.getActiveWeaponItem();
			if ((weapon != null) && ((weapon.getItemType() == WeaponType.BOW) || (weapon.getItemType() == WeaponType.CROSSBOW)))
			{
				weaponTypeBoost = 70;
			}
			else
			{
				weaponTypeBoost = 77;
			}
			
			double energyChargesBoost = 1;
			if (attacker.getCharges() == 1)
			{
				energyChargesBoost = 1.1;
				attacker.decreaseCharges(1);
			}
			else if (attacker.getCharges() == 2)
			{
				energyChargesBoost = 1.2;
				attacker.decreaseCharges(2);
			}
			else if (attacker.getCharges() >= 3)
			{
				energyChargesBoost = 1.3;
				attacker.decreaseCharges(3);
			}
			
			final double addPower = attacker.getStat().calcStat(Stats.MOMENTUM_SKILL_POWER, 1, null, null);
			
			attack += _power;
			attack *= addPower;
			attack *= ssBoost;
			attack *= energyChargesBoost;
			attack *= weaponTypeBoost;
			
			damage = attack / defence;
			damage *= damageMultiplier;
			if (target instanceof L2PcInstance)
			{
				damage *= attacker.getStat().calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0);
				damage *= target.getStat().calcStat(Stats.PVP_PHYS_SKILL_DEF, 1.0);
				damage = attacker.getStat().calcStat(Stats.PHYSICAL_SKILL_POWER, damage);
			}
			
			critical = (BaseStats.STR.calcBonus(attacker) * _criticalChance) > (Rnd.nextDouble() * 100);
			if (critical)
			{
				damage *= 2;
			}
		}
		
		if (damage > 0)
		{
			// reduce damage if target has maxdamage buff
			final double maxDamage = target.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null);
			if (maxDamage > 0)
			{
				damage = (int) maxDamage;
			}
			
			attacker.sendDamageMessage(target, (int) damage, false, critical, false);
			target.reduceCurrentHp(damage, attacker, skill);
			target.notifyDamageReceived(damage, attacker, skill, critical, false);
			
			// Check if damage should be reflected
			Formulas.calcDamageReflected(attacker, target, skill, critical);
		}
	}
}
