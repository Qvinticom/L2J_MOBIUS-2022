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
package com.l2jmobius.gameserver.model.stats;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.commons.util.CommonUtil;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.data.xml.impl.HitConditionBonusData;
import com.l2jmobius.gameserver.data.xml.impl.KarmaData;
import com.l2jmobius.gameserver.enums.AttributeType;
import com.l2jmobius.gameserver.enums.BasicProperty;
import com.l2jmobius.gameserver.enums.DispelSlotType;
import com.l2jmobius.gameserver.enums.Position;
import com.l2jmobius.gameserver.enums.ShotType;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jmobius.gameserver.model.cubic.CubicInstance;
import com.l2jmobius.gameserver.model.effects.EffectFlag;
import com.l2jmobius.gameserver.model.effects.L2EffectType;
import com.l2jmobius.gameserver.model.items.L2Armor;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.items.L2Weapon;
import com.l2jmobius.gameserver.model.items.type.ArmorType;
import com.l2jmobius.gameserver.model.items.type.WeaponType;
import com.l2jmobius.gameserver.model.skills.AbnormalType;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.skills.SkillCaster;
import com.l2jmobius.gameserver.network.Debug;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Global calculations.
 */
public final class Formulas
{
	/** Regeneration Task period. */
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
	
	public static final byte SHIELD_DEFENSE_FAILED = 0; // no shield defense
	public static final byte SHIELD_DEFENSE_SUCCEED = 1; // normal shield defense
	public static final byte SHIELD_DEFENSE_PERFECT_BLOCK = 2; // perfect block
	
	public static final int SKILL_LAUNCH_TIME = 500; // The time to pass after the skill launching until the skill to affect targets. In milliseconds
	private static final byte MELEE_ATTACK_RANGE = 40;
	
	/**
	 * Return the period between 2 regeneration task (3s for L2Character, 5 min for L2DoorInstance).
	 * @param cha
	 * @return
	 */
	public static int getRegeneratePeriod(L2Character cha)
	{
		return cha.isDoor() ? HP_REGENERATE_PERIOD * 100 : HP_REGENERATE_PERIOD;
	}
	
	public static double calcBlowDamage(L2Character attacker, L2Character target, Skill skill, boolean backstab, double power, byte shld, boolean ss)
	{
		final double distance = attacker.calculateDistance(target, true, false);
		if (distance > target.getStat().getValue(Stats.SPHERIC_BARRIER_RANGE, Integer.MAX_VALUE))
		{
			return 0;
		}
		
		double defence = target.getPDef();
		
		switch (shld)
		{
			case Formulas.SHIELD_DEFENSE_SUCCEED:
			{
				defence += target.getShldDef();
				break;
			}
			case Formulas.SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
			{
				return 1;
			}
		}
		
		// Critical
		final double criticalMod = (attacker.getStat().getValue(Stats.CRITICAL_DAMAGE, 1));
		final double criticalPositionMod = attacker.getStat().getPositionTypeValue(Stats.CRITICAL_DAMAGE, Position.getPosition(attacker, target));
		final double criticalVulnMod = (target.getStat().getValue(Stats.DEFENCE_CRITICAL_DAMAGE, 1));
		final double criticalAddMod = (attacker.getStat().getValue(Stats.CRITICAL_DAMAGE_ADD, 0));
		final double criticalAddVuln = target.getStat().getValue(Stats.DEFENCE_CRITICAL_DAMAGE_ADD, 0);
		// Trait, elements
		final double weaponTraitMod = calcWeaponTraitBonus(attacker, target);
		final double generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
		final double attributeMod = calcAttributeBonus(attacker, target, skill);
		final double randomMod = attacker.getRandomDamageMultiplier();
		final double pvpPveMod = calculatePvpPveBonus(attacker, target, skill, true);
		
		// Initial damage
		final double ssmod = ss ? (2 * attacker.getStat().getValue(Stats.SHOTS_BONUS)) : 1; // 2.04 for dual weapon?
		final double cdMult = criticalMod * (((criticalPositionMod - 1) / 2) + 1) * (((criticalVulnMod - 1) / 2) + 1);
		final double cdPatk = criticalAddMod + criticalAddVuln;
		final Position position = Position.getPosition(attacker, target);
		final double isPosition = position == Position.BACK ? 0.2 : position == Position.SIDE ? 0.05 : 0;
		
		// ........................_____________________________Initial Damage____________________________...___________Position Additional Damage___________..._CriticalAdd_
		// ATTACK CALCULATION 77 * [(skillpower+patk) * 0.666 * cdbonus * cdPosBonusHalf * cdVulnHalf * ss + isBack0.2Side0.05 * (skillpower+patk*ss) * random + 6 * cd_patk] / pdef
		// ````````````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^```^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^```^^^^^^^^^^^^
		final double baseMod = ((77 * (((power + attacker.getPAtk()) * 0.666 * ssmod * cdMult) + (isPosition * (power + (attacker.getPAtk() * ssmod)) * randomMod) + (6 * cdPatk))) / defence);
		final double damage = baseMod * weaponTraitMod * generalTraitMod * attributeMod * randomMod * pvpPveMod;
		
		if (attacker.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("skillPower", power);
			set.set("ssboost", ssmod);
			set.set("isPosition", isPosition);
			set.set("baseMod", baseMod);
			set.set("criticalMod", criticalMod);
			set.set("criticalVulnMod", criticalVulnMod);
			set.set("criticalAddMod", criticalAddMod);
			set.set("criticalAddVuln", criticalAddVuln);
			set.set("weaponTraitMod", weaponTraitMod);
			set.set("generalTraitMod", generalTraitMod);
			set.set("attributeMod", attributeMod);
			set.set("weaponMod", randomMod);
			set.set("penaltyMod", pvpPveMod);
			set.set("damage", (int) damage);
			Debug.sendSkillDebug(attacker, target, skill, set);
		}
		
		return damage;
	}
	
	public static double calcMagicDam(L2Character attacker, L2Character target, Skill skill, double mAtk, double power, double mDef, boolean sps, boolean bss, boolean mcrit)
	{
		final double distance = attacker.calculateDistance(target, true, false);
		if (distance > target.getStat().getValue(Stats.SPHERIC_BARRIER_RANGE, Integer.MAX_VALUE))
		{
			return 0;
		}
		
		// Bonus Spirit shot
		final double shotsBonus = bss ? (4 * attacker.getStat().getValue(Stats.SHOTS_BONUS)) : sps ? (2 * attacker.getStat().getValue(Stats.SHOTS_BONUS)) : 1;
		final double critMod = mcrit ? (2 * calcCritDamage(attacker, target, skill)) : 1; // TODO not really a proper way... find how it works then implement. // damage += attacker.getStat().getValue(Stats.MAGIC_CRIT_DMG_ADD, 0);
		
		// Trait, elements
		final double generalTraitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
		final double attributeMod = calcAttributeBonus(attacker, target, skill);
		final double randomMod = attacker.getRandomDamageMultiplier();
		final double pvpPveMod = calculatePvpPveBonus(attacker, target, skill, mcrit);
		
		// MDAM Formula.
		double damage = (91 * power * Math.sqrt(mAtk * shotsBonus)) / mDef;
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker.isPlayer())
			{
				if (calcMagicSuccess(attacker, target, skill) && ((target.getLevel() - attacker.getLevel()) <= 9))
				{
					if (skill.hasEffectType(L2EffectType.HP_DRAIN))
					{
						attacker.sendPacket(SystemMessageId.DRAIN_WAS_ONLY_50_PERCENT_SUCCESSFUL);
					}
					else
					{
						attacker.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
					}
					damage /= 2;
				}
				else
				{
					final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_RESISTED_YOUR_S2);
					sm.addCharName(target);
					sm.addSkillName(skill);
					attacker.sendPacket(sm);
					damage = 1;
				}
			}
			
			if (target.isPlayer())
			{
				final SystemMessage sm = (skill.hasEffectType(L2EffectType.HP_DRAIN)) ? SystemMessage.getSystemMessage(SystemMessageId.YOU_RESISTED_C1_S_DRAIN) : SystemMessage.getSystemMessage(SystemMessageId.YOU_RESISTED_C1_S_MAGIC);
				sm.addCharName(attacker);
				target.sendPacket(sm);
			}
		}
		
		damage = damage * critMod * generalTraitMod * attributeMod * randomMod * pvpPveMod;
		damage = attacker.getStat().getValue(Stats.MAGICAL_SKILL_POWER, damage);
		
		return damage;
	}
	
	public static double calcMagicDam(CubicInstance attacker, L2Character target, Skill skill, double power, boolean mcrit, byte shld)
	{
		final double mAtk = attacker.getTemplate().getPower();
		return calcMagicDam(attacker.getOwner(), target, skill, mAtk, power, shld, false, false, mcrit);
	}
	
	/**
	 * Returns true in case of critical hit
	 * @param rate
	 * @param skill
	 * @param activeChar
	 * @param target
	 * @return
	 */
	public static boolean calcCrit(double rate, L2Character activeChar, L2Character target, Skill skill)
	{
		// Skill critical rate is calculated up to the first decimal, thats why multiply by 10 and compare to 1000.
		if (skill != null)
		{
			// Magic Critical Rate
			if (skill.isMagic())
			{
				rate = activeChar.getStat().getValue(Stats.MAGIC_CRITICAL_RATE);
				if ((target == null) || !skill.isBad())
				{
					return Math.min(rate, 320) > Rnd.get(1000);
				}
				
				double finalRate = target.getStat().getValue(Stats.DEFENCE_MAGIC_CRITICAL_RATE, rate) + target.getStat().getValue(Stats.DEFENCE_MAGIC_CRITICAL_RATE_ADD, 0);
				if ((activeChar.getLevel() >= 78) && (target.getLevel() >= 78))
				{
					finalRate += Math.sqrt(activeChar.getLevel()) + ((activeChar.getLevel() - target.getLevel()) / 25);
					return Math.min(finalRate, 320) > Rnd.get(1000);
				}
				
				return Math.min(finalRate, 200) > Rnd.get(1000);
			}
			
			// Physical skill critical rate
			final double statBonus;
			final double rateBonus;
			
			// There is a chance that activeChar has altered base stat for skill critical.
			final byte skillCritRateStat = (byte) activeChar.getStat().getValue(Stats.STAT_BONUS_SKILL_CRITICAL, -1);
			if ((skillCritRateStat >= 0) && (skillCritRateStat < BaseStats.values().length))
			{
				// Best tested
				statBonus = BaseStats.STR.getValue(activeChar.getDEX()) * 2;
				rateBonus = (activeChar.getStat().getValue(Stats.CRITICAL_RATE_SKILL) * 2) - 1; // Tests made by retail GMs show that 3x10% increase yields to 16.2 -> 26.1
			}
			else
			{
				// Default base stat used for skill critical formula is STR.
				statBonus = BaseStats.STR.calcBonus(activeChar);
				rateBonus = activeChar.getStat().getValue(Stats.CRITICAL_RATE_SKILL);
			}
			
			final double finalRate = rate * statBonus * rateBonus * 10;
			return finalRate > Rnd.get(1000);
		}
		
		// Autoattack critical rate.
		// It is capped to 500, but unbound by positional critical rate and level diff bonus.
		rate *= activeChar.getStat().getPositionTypeValue(Stats.CRITICAL_RATE, Position.getPosition(activeChar, target));
		
		// In retail, it appears that when you are higher level attacking lower level mobs, your critical rate is much higher.
		// Level 91 attacking level 1 appear that nearly all hits are critical. Unconfirmed for skills and pvp.
		if (activeChar.isNpc() || target.isNpc())
		{
			final double levelMod = 1 + (activeChar.getLevelMod() - target.getLevelMod());
			rate *= levelMod;
		}
		
		final double finalRate = target.getStat().getValue(Stats.DEFENCE_CRITICAL_RATE, rate) + target.getStat().getValue(Stats.DEFENCE_CRITICAL_RATE_ADD, 0);
		return finalRate > Rnd.get(1000);
	}
	
	/**
	 * @param attacker
	 * @param target
	 * @param skill {@code skill} to be used in the calculation, else calculation will result for autoattack.
	 * @return regular critical damage bonus. Positional bonus is excluded!
	 */
	public static double calcCritDamage(L2Character attacker, L2Character target, Skill skill)
	{
		final double criticalDamage;
		final double defenceCriticalDamage;
		
		if (skill != null)
		{
			if (skill.isMagic())
			{
				// Magic critical damage.
				criticalDamage = attacker.getStat().getValue(Stats.MAGIC_CRITICAL_DAMAGE, 1);
				defenceCriticalDamage = target.getStat().getValue(Stats.DEFENCE_MAGIC_CRITICAL_DAMAGE, 1);
			}
			else
			{
				criticalDamage = attacker.getStat().getValue(Stats.CRITICAL_DAMAGE_SKILL, 1);
				defenceCriticalDamage = target.getStat().getValue(Stats.DEFENCE_CRITICAL_DAMAGE_SKILL, 1);
			}
		}
		else
		{
			// Autoattack critical damage.
			criticalDamage = attacker.getStat().getValue(Stats.CRITICAL_DAMAGE, 1) * attacker.getStat().getPositionTypeValue(Stats.CRITICAL_DAMAGE, Position.getPosition(attacker, target));
			defenceCriticalDamage = target.getStat().getValue(Stats.DEFENCE_CRITICAL_DAMAGE, 1);
		}
		
		return criticalDamage * defenceCriticalDamage;
	}
	
	/**
	 * @param attacker
	 * @param target
	 * @param skill {@code skill} to be used in the calculation, else calculation will result for autoattack.
	 * @return critical damage additional bonus, not multiplier!
	 */
	public static double calcCritDamageAdd(L2Character attacker, L2Character target, Skill skill)
	{
		final double criticalDamageAdd;
		final double defenceCriticalDamageAdd;
		
		if (skill != null)
		{
			if (skill.isMagic())
			{
				// Magic critical damage.
				criticalDamageAdd = attacker.getStat().getValue(Stats.MAGIC_CRITICAL_DAMAGE_ADD, 0);
				defenceCriticalDamageAdd = target.getStat().getValue(Stats.DEFENCE_MAGIC_CRITICAL_DAMAGE_ADD, 0);
			}
			else
			{
				criticalDamageAdd = attacker.getStat().getValue(Stats.CRITICAL_DAMAGE_SKILL_ADD, 0);
				defenceCriticalDamageAdd = target.getStat().getValue(Stats.DEFENCE_CRITICAL_DAMAGE_SKILL_ADD, 0);
			}
		}
		else
		{
			// Autoattack critical damage.
			criticalDamageAdd = attacker.getStat().getValue(Stats.CRITICAL_DAMAGE_ADD, 0);
			defenceCriticalDamageAdd = target.getStat().getValue(Stats.DEFENCE_CRITICAL_DAMAGE_ADD, 0);
		}
		
		return criticalDamageAdd + defenceCriticalDamageAdd;
	}
	
	/**
	 * @param target
	 * @param dmg
	 * @return true in case when ATTACK is canceled due to hit
	 */
	public static boolean calcAtkBreak(L2Character target, double dmg)
	{
		if (target.isChanneling())
		{
			return false;
		}
		
		double init = 0;
		
		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow(SkillCaster::canAbortCast))
		{
			init = 15;
		}
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			final L2Weapon wpn = target.getActiveWeaponItem();
			if ((wpn != null) && (wpn.getItemType() == WeaponType.BOW))
			{
				init = 15;
			}
		}
		
		if (target.isRaid() || target.isHpBlocked() || (init <= 0))
		{
			return false; // No attack break
		}
		
		// Chance of break is higher with higher dmg
		init += Math.sqrt(13 * dmg);
		
		// Chance is affected by target MEN
		init -= ((BaseStats.MEN.calcBonus(target) * 100) - 100);
		
		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.getStat().getValue(Stats.ATTACK_CANCEL, init);
		
		// Adjust the rate to be between 1 and 99
		rate = Math.max(Math.min(rate, 99), 1);
		
		return Rnd.get(100) < rate;
	}
	
	/**
	 * Calculate delay (in milliseconds) for skills cast
	 * @param attacker
	 * @param skill
	 * @param skillTime
	 * @return
	 */
	public static int calcAtkSpd(L2Character attacker, Skill skill, double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) ((skillTime / attacker.getMAtkSpd()) * 333);
		}
		return (int) ((skillTime / attacker.getPAtkSpd()) * 300);
	}
	
	/**
	 * TODO: Implement those:
	 * <ul>
	 * <li>Skill cool time is block player from doing anything (moving, casting, attacking).</li>
	 * <li>Seems hardcoded channeling value is not used for the skill task</li>
	 * </ul>
	 * @param creature
	 * @param skill
	 * @return the hit time of the skill.
	 */
	public static int calcHitTime(L2Character creature, Skill skill)
	{
		int skillTime = skill.getHitTime() - SKILL_LAUNCH_TIME;
		
		// Calculate the Casting Time of the "Non-Static" Skills (with caster PAtk/MAtkSpd).
		if (!skill.isStatic())
		{
			skillTime = calcAtkSpd(creature, skill, skillTime);
		}
		// Calculate the Casting Time of Magic Skills (reduced in 40% if using SPS/BSPS)
		if (skill.isMagic() && (creature.isChargedShot(ShotType.SPIRITSHOTS) || creature.isChargedShot(ShotType.BLESSED_SPIRITSHOTS)))
		{
			skillTime = (int) (0.6 * skillTime);
		}
		
		return Math.max(skillTime, 0);
	}
	
	public static int calcSkillCancelTime(L2Character creature, Skill skill)
	{
		// Fishing skills.
		if ((skill.getId() == 1312) || (skill.getId() == 1314) || (skill.getId() == 1315))
		{
			return 0;
		}
		// return (int) Math.max(skill.getCancelTime() / calcSkillTimeFactor(attacker, skill), 500);
		return (int) Math.max(skill.getHitTime() / calcSkillTimeFactor(creature, skill), SKILL_LAUNCH_TIME);
	}
	
	/**
	 * TODO: Implement armor bonus and NPC Divider
	 * @param creature
	 * @param skill
	 * @return
	 */
	public static double calcSkillTimeFactor(L2Character creature, Skill skill)
	{
		double factor = 0;
		if (skill.isPhysical() || skill.isDance()) // is_magic = 0 or 3
		{
			final double armorBonus = 1; // EquipedArmorSpeedByCrystal TODO: Implement me!
			final double dexBonus = BaseStats.DEX.calcBonus(creature);
			final double weaponAttackSpeed = Stats.weaponBaseValue(creature, Stats.PHYSICAL_ATTACK_SPEED) / armorBonus; // unk868
			final double attackSpeedPerBonus = creature.getStat().getMul(Stats.PHYSICAL_ATTACK_SPEED);
			final double attackSpeedDiffBonus = creature.getStat().getAdd(Stats.PHYSICAL_ATTACK_SPEED);
			factor = (dexBonus * (weaponAttackSpeed / 333) * attackSpeedPerBonus) + (attackSpeedDiffBonus / 333);
		}
		else if (skill.isMagic()) // is_magic = 1
		{
			final double armorBonus = 1; // TODO: Implement me!
			final double witBonus = BaseStats.WIT.calcBonus(creature);
			final double castingSpeedPerBonus = creature.getStat().getMul(Stats.MAGIC_ATTACK_SPEED); // m_use_speed
			final double castingSpeedDiffBonus = creature.getStat().getAdd(Stats.MAGIC_ATTACK_SPEED);
			factor = ((1 / armorBonus) * witBonus * castingSpeedPerBonus) + (castingSpeedDiffBonus / 333);
		}
		else if (skill.isStatic()) // is_magic = 2
		{
			factor = 1;
		}
		
		if (skill.isChanneling()) // operate type = 5 or 6 or 7
		{
			factor = 1;
		}
		
		if (creature.isNpc() || creature.isSummon())
		{
			// TODO: Implement me!
			// if (attacker.unk08B0 > 0)
			{
				// factor /= attacker.unk08B0;
			}
		}
		
		return Math.max(factor, 0.01);
	}
	
	/**
	 * Formula based on http://l2p.l2wh.com/nonskillattacks.html
	 * @param attacker
	 * @param target
	 * @return {@code true} if hit missed (target evaded), {@code false} otherwise.
	 */
	public static boolean calcHitMiss(L2Character attacker, L2Character target)
	{
		int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate()))) * 10;
		
		// Get additional bonus from the conditions when you are attacking
		chance *= HitConditionBonusData.getInstance().getConditionBonus(attacker, target);
		
		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);
		
		return chance < Rnd.get(1000);
	}
	
	/**
	 * Returns:<br>
	 * 0 = shield defense doesn't succeed<br>
	 * 1 = shield defense succeed<br>
	 * 2 = perfect block<br>
	 * @param attacker
	 * @param target
	 * @param sendSysMsg
	 * @return
	 */
	public static byte calcShldUse(L2Character attacker, L2Character target, boolean sendSysMsg)
	{
		final L2Item item = target.getSecondaryWeaponItem();
		if ((item == null) || !(item instanceof L2Armor) || (((L2Armor) item).getItemType() == ArmorType.SIGIL))
		{
			return 0;
		}
		
		double shldRate = target.getStat().getValue(Stats.SHIELD_DEFENCE_RATE, 0) * BaseStats.DEX.calcBonus(target);
		
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		if (attacker.getAttackType().isRanged())
		{
			shldRate *= 1.3;
		}
		
		final int degreeside = target.isAffected(EffectFlag.PHYSICAL_SHIELD_ANGLE_ALL) ? 360 : 120;
		if ((degreeside < 360) && (!target.isFacing(attacker, degreeside)))
		{
			return 0;
		}
		
		byte shldSuccess = SHIELD_DEFENSE_FAILED;
		
		// Check shield success
		if (shldRate > Rnd.get(100))
		{
			// If shield succeed, check perfect block.
			if (((100 - (2 * BaseStats.DEX.calcBonus(target))) < Rnd.get(100)))
			{
				shldSuccess = SHIELD_DEFENSE_PERFECT_BLOCK;
			}
			else
			{
				shldSuccess = SHIELD_DEFENSE_SUCCEED;
			}
		}
		
		if (sendSysMsg && target.isPlayer())
		{
			final L2PcInstance enemy = target.getActingPlayer();
			
			switch (shldSuccess)
			{
				case SHIELD_DEFENSE_SUCCEED:
				{
					enemy.sendPacket(SystemMessageId.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
					break;
				}
				case SHIELD_DEFENSE_PERFECT_BLOCK:
				{
					enemy.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
					break;
				}
			}
		}
		
		return shldSuccess;
	}
	
	public static byte calcShldUse(L2Character attacker, L2Character target)
	{
		return calcShldUse(attacker, target, true);
	}
	
	public static boolean calcMagicAffected(L2Character actor, L2Character target, Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		double defence = 0;
		if (skill.isActive() && skill.isBad())
		{
			defence = target.getMDef();
		}
		
		final double attack = 2 * actor.getMAtk() * calcGeneralTraitBonus(actor, target, skill.getTraitType(), false);
		double d = (attack - defence) / (attack + defence);
		
		if (skill.isDebuff())
		{
			if (target.getAbnormalShieldBlocks() > 0)
			{
				if (target.decrementAbnormalShieldBlocks() == 0)
				{
					target.stopEffects(EffectFlag.ABNORMAL_SHIELD);
				}
				return false;
			}
		}
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}
	
	public static double calcLvlBonusMod(L2Character attacker, L2Character target, Skill skill)
	{
		final int attackerLvl = skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel();
		final double skillLvlBonusRateMod = 1 + (skill.getLvlBonusRate() / 100.);
		final double lvlMod = 1 + ((attackerLvl - target.getLevel()) / 100.);
		return skillLvlBonusRateMod * lvlMod;
	}
	
	/**
	 * Calculates the effect landing success.<br>
	 * @param attacker the attacker
	 * @param target the target
	 * @param skill the skill
	 * @return {@code true} if the effect lands
	 */
	public static boolean calcEffectSuccess(L2Character attacker, L2Character target, Skill skill)
	{
		// StaticObjects can not receive continuous effects.
		if (target.isDoor() || (target instanceof L2SiegeFlagInstance) || (target instanceof L2StaticObjectInstance))
		{
			return false;
		}
		
		if (skill.isDebuff())
		{
			boolean resisted = target.isCastingNow(s -> s.getSkill().getAbnormalResists().contains(skill.getAbnormalType()));
			if (!resisted)
			{
				if (target.getAbnormalShieldBlocks() > 0)
				{
					if (target.decrementAbnormalShieldBlocks() == 0)
					{
						target.stopEffects(EffectFlag.ABNORMAL_SHIELD);
					}
					resisted = true;
				}
			}
			
			if (!resisted)
			{
				final double distance = attacker.calculateDistance(target, true, false);
				if (distance > target.getStat().getValue(Stats.SPHERIC_BARRIER_RANGE, Integer.MAX_VALUE))
				{
					resisted = true;
				}
			}
			
			if (resisted)
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_RESISTED_YOUR_S2);
				sm.addCharName(target);
				sm.addSkillName(skill);
				attacker.sendPacket(sm);
				return false;
			}
		}
		
		final int activateRate = skill.getActivateRate();
		if ((activateRate == -1))
		{
			return true;
		}
		
		int magicLevel = skill.getMagicLevel();
		if (magicLevel <= -1)
		{
			magicLevel = target.getLevel() + 3;
		}
		
		final double targetBasicProperty = getAbnormalResist(skill.getBasicProperty(), target);
		final double baseMod = ((((((magicLevel - target.getLevel()) + 3) * skill.getLvlBonusRate()) + activateRate) + 30.0) - targetBasicProperty);
		final double elementMod = calcAttributeBonus(attacker, target, skill);
		final double traitMod = calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
		final double basicPropertyResist = getBasicPropertyResistBonus(skill.getBasicProperty(), target);
		final double buffDebuffMod = skill.isDebuff() ? target.getStat().getValue(Stats.RESIST_ABNORMAL_DEBUFF, 1) : 0;
		final double rate = baseMod * elementMod * traitMod * buffDebuffMod;
		final double finalRate = traitMod > 0 ? CommonUtil.constrain(rate, skill.getMinChance(), skill.getMaxChance()) * basicPropertyResist : 0;
		
		if (attacker.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("baseMod", baseMod);
			set.set("elementMod", elementMod);
			set.set("traitMod", traitMod);
			set.set("buffDebuffMod", buffDebuffMod);
			set.set("rate", rate);
			set.set("finalRate", finalRate);
			Debug.sendSkillDebug(attacker, target, skill, set);
		}
		
		if ((finalRate <= Rnd.get(100)) && (target != attacker))
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAS_RESISTED_YOUR_S2);
			sm.addCharName(target);
			sm.addSkillName(skill);
			attacker.sendPacket(sm);
			return false;
		}
		return true;
	}
	
	public static boolean calcCubicSkillSuccess(CubicInstance attacker, L2Character target, Skill skill, byte shld)
	{
		if (skill.isDebuff())
		{
			if (skill.getActivateRate() == -1)
			{
				return true;
			}
			
			if (target.getAbnormalShieldBlocks() > 0)
			{
				if (target.decrementAbnormalShieldBlocks() == 0)
				{
					target.stopEffects(EffectFlag.ABNORMAL_SHIELD);
				}
				return false;
			}
		}
		
		// Perfect Shield Block.
		if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
		{
			return false;
		}
		
		// if target reflect this skill then the effect will fail
		if (calcBuffDebuffReflection(target, skill))
		{
			return false;
		}
		
		final double targetBasicProperty = getAbnormalResist(skill.getBasicProperty(), target);
		
		// Calculate BaseRate.
		final double baseRate = skill.getActivateRate();
		final double statMod = 1 + (targetBasicProperty / 100);
		double rate = (baseRate / statMod);
		
		// Resist Modifier.
		final double resMod = calcGeneralTraitBonus(attacker.getOwner(), target, skill.getTraitType(), false);
		rate *= resMod;
		
		// Lvl Bonus Modifier.
		final double lvlBonusMod = calcLvlBonusMod(attacker.getOwner(), target, skill);
		rate *= lvlBonusMod;
		
		// Element Modifier.
		final double elementMod = calcAttributeBonus(attacker.getOwner(), target, skill);
		rate *= elementMod;
		
		final double basicPropertyResist = getBasicPropertyResistBonus(skill.getBasicProperty(), target);
		
		// Add Matk/Mdef Bonus (TODO: Pending)
		
		// Check the Rate Limits.
		final double finalRate = CommonUtil.constrain(rate, skill.getMinChance(), skill.getMaxChance()) * basicPropertyResist;
		
		if (attacker.getOwner().isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("baseMod", baseRate);
			set.set("resMod", resMod);
			set.set("statMod", statMod);
			set.set("elementMod", elementMod);
			set.set("lvlBonusMod", lvlBonusMod);
			set.set("rate", rate);
			set.set("finalRate", finalRate);
			Debug.sendSkillDebug(attacker.getOwner(), target, skill, set);
		}
		
		return Rnd.get(100) < finalRate;
	}
	
	public static boolean calcMagicSuccess(L2Character attacker, L2Character target, Skill skill)
	{
		// FIXME: Fix this LevelMod Formula.
		final int lvlDifference = (target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel()));
		final double lvlModifier = Math.pow(1.3, lvlDifference);
		float targetModifier = 1;
		if (target.isAttackable() && !target.isRaid() && !target.isRaidMinion() && (target.getLevel() >= Config.MIN_NPC_LVL_MAGIC_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) >= 3))
		{
			final int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 2;
			if (lvlDiff >= Config.NPC_SKILL_CHANCE_PENALTY.size())
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(Config.NPC_SKILL_CHANCE_PENALTY.size() - 1);
			}
			else
			{
				targetModifier = Config.NPC_SKILL_CHANCE_PENALTY.get(lvlDiff);
			}
		}
		// general magic resist
		final double resModifier = target.getStat().getValue(Stats.MAGIC_SUCCESS_RES, 1);
		final int rate = 100 - Math.round((float) (lvlModifier * targetModifier * resModifier));
		
		if (attacker.isDebug())
		{
			final StatsSet set = new StatsSet();
			set.set("lvlDifference", lvlDifference);
			set.set("lvlModifier", lvlModifier);
			set.set("resModifier", resModifier);
			set.set("targetModifier", targetModifier);
			set.set("rate", rate);
			Debug.sendSkillDebug(attacker, target, skill, set);
		}
		
		return (Rnd.get(100) < rate);
	}
	
	public static double calcManaDam(L2Character attacker, L2Character target, Skill skill, double power, byte shld, boolean sps, boolean bss, boolean mcrit, double critLimit)
	{
		// Formula: (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk();
		double mDef = target.getMDef();
		final double mp = target.getMaxMp();
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
			{
				mDef += target.getShldDef();
				break;
			}
			case SHIELD_DEFENSE_PERFECT_BLOCK: // perfect block
			{
				return 1;
			}
		}
		
		// Bonus Spiritshot
		final double shotsBonus = attacker.getStat().getValue(Stats.SHOTS_BONUS);
		double sapphireBonus = 0;
		if (attacker.isPlayer() && (attacker.getActingPlayer().getActiveShappireJewel() != null))
		{
			sapphireBonus = attacker.getActingPlayer().getActiveShappireJewel().getBonus();
		}
		mAtk *= bss ? 4 * (shotsBonus + sapphireBonus) : sps ? 2 * (shotsBonus + sapphireBonus) : 1;
		
		double damage = (Math.sqrt(mAtk) * power * (mp / 97)) / mDef;
		damage *= calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
		damage *= calculatePvpPveBonus(attacker, target, skill, mcrit);
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker.isPlayer())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DAMAGE_IS_DECREASED_BECAUSE_C1_RESISTED_C2_S_MAGIC);
				sm.addCharName(target);
				sm.addCharName(attacker);
				attacker.sendPacket(sm);
				damage /= 2;
			}
			
			if (target.isPlayer())
			{
				final SystemMessage sm2 = SystemMessage.getSystemMessage(SystemMessageId.C1_WEAKLY_RESISTED_C2_S_MAGIC);
				sm2.addCharName(target);
				sm2.addCharName(attacker);
				target.sendPacket(sm2);
			}
		}
		
		if (mcrit)
		{
			damage *= 3;
			damage = Math.min(damage, critLimit);
			attacker.sendPacket(SystemMessageId.M_CRITICAL);
		}
		return damage;
	}
	
	public static double calculateSkillResurrectRestorePercent(double baseRestorePercent, L2Character caster)
	{
		if ((baseRestorePercent == 0) || (baseRestorePercent == 100))
		{
			return baseRestorePercent;
		}
		
		double restorePercent = baseRestorePercent * BaseStats.WIT.calcBonus(caster);
		if ((restorePercent - baseRestorePercent) > 20.0)
		{
			restorePercent += 20.0;
		}
		
		restorePercent = Math.max(restorePercent, baseRestorePercent);
		restorePercent = Math.min(restorePercent, 90.0);
		
		return restorePercent;
	}
	
	public static boolean calcPhysicalSkillEvasion(L2Character activeChar, L2Character target, Skill skill)
	{
		if (Rnd.get(100) < target.getStat().getSkillEvasionTypeValue(skill.getMagicType()))
		{
			if (activeChar.isPlayer())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DODGED_THE_ATTACK);
				sm.addString(target.getName());
				activeChar.getActingPlayer().sendPacket(sm);
			}
			if (target.isPlayer())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_DODGED_C1_S_ATTACK);
				sm.addString(activeChar.getName());
				target.getActingPlayer().sendPacket(sm);
			}
			return true;
		}
		return false;
	}
	
	public static boolean calcSkillMastery(L2Character actor, Skill sk)
	{
		// Static Skills are not affected by Skill Mastery.
		if (sk.isStatic() || !actor.isPlayer())
		{
			return false;
		}
		
		final int val = (int) actor.getStat().getValue(Stats.SKILL_CRITICAL, -1);
		
		if (val == -1)
		{
			return false;
		}
		
		final double chance = BaseStats.values()[val].calcBonus(actor) * actor.getStat().getValue(Stats.SKILL_CRITICAL_PROBABILITY, 1);
		
		return ((Rnd.nextDouble() * 100.) < chance);
	}
	
	/**
	 * Calculates the attribute bonus with the following formula: <BR>
	 * diff > 0, so AttBonus = 1,025 + sqrt[(diff^3) / 2] * 0,0001, cannot be above 1,25! <BR>
	 * diff < 0, so AttBonus = 0,975 - sqrt[(diff^3) / 2] * 0,0001, cannot be below 0,75! <BR>
	 * diff == 0, so AttBonus = 1
	 * @param attacker
	 * @param target
	 * @param skill Can be {@code null} if there is no skill used for the attack.
	 * @return The attribute bonus
	 */
	public static double calcAttributeBonus(L2Character attacker, L2Character target, Skill skill)
	{
		int attack_attribute;
		int defence_attribute;
		
		if (skill != null)
		{
			if ((skill.getAttributeType() == AttributeType.NONE) || (skill.getAttributeType() == AttributeType.NONE_ARMOR))
			{
				attack_attribute = 0;
				defence_attribute = target.getDefenseElementValue(AttributeType.NONE_ARMOR);
			}
			else if (attacker.getAttackElement() == skill.getAttributeType())
			{
				attack_attribute = attacker.getAttackElementValue(attacker.getAttackElement()) + skill.getAttributeValue();
				defence_attribute = target.getDefenseElementValue(attacker.getAttackElement());
			}
			else
			{
				attack_attribute = skill.getAttributeValue();
				defence_attribute = target.getDefenseElementValue(skill.getAttributeType());
			}
		}
		else
		{
			attack_attribute = attacker.getAttackElementValue(attacker.getAttackElement());
			defence_attribute = target.getDefenseElementValue(attacker.getAttackElement());
		}
		
		final int diff = attack_attribute - defence_attribute;
		if (diff > 0)
		{
			return Math.min(1.025 + (Math.sqrt(Math.pow(diff, 3) / 2) * 0.0001), 1.25);
		}
		else if (diff < 0)
		{
			return Math.max(0.975 - (Math.sqrt(Math.pow(-diff, 3) / 2) * 0.0001), 0.75);
		}
		
		return 1;
	}
	
	public static void calcCounterAttack(L2Character attacker, L2Character target, Skill skill, boolean crit)
	{
		// Only melee skills can be reflected
		if (skill.isMagic() || (skill.getCastRange() > MELEE_ATTACK_RANGE))
		{
			return;
		}
		
		final double chance = target.getStat().getValue(Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE, 0);
		if (Rnd.get(100) < chance)
		{
			if (target.isPlayer())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_COUNTERED_C1_S_ATTACK);
				sm.addCharName(attacker);
				target.sendPacket(sm);
			}
			if (attacker.isPlayer())
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_PERFORMING_A_COUNTERATTACK);
				sm.addCharName(target);
				attacker.sendPacket(sm);
			}
			
			double counterdmg = ((target.getPAtk() * 873) / attacker.getPDef()); // Old: (((target.getPAtk(attacker) * 10.0) * 70.0) / attacker.getPDef(target));
			counterdmg *= calcWeaponTraitBonus(attacker, target);
			counterdmg *= calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false);
			counterdmg *= calcAttributeBonus(attacker, target, skill);
			
			attacker.reduceCurrentHp(counterdmg, target, skill);
		}
	}
	
	/**
	 * Calculate buff/debuff reflection.
	 * @param target
	 * @param skill
	 * @return {@code true} if reflect, {@code false} otherwise.
	 */
	public static boolean calcBuffDebuffReflection(L2Character target, Skill skill)
	{
		if (!skill.isDebuff() || (skill.getActivateRate() == -1))
		{
			return false;
		}
		return target.getStat().getValue(skill.isMagic() ? Stats.REFLECT_SKILL_MAGIC : Stats.REFLECT_SKILL_PHYSIC, 0) > Rnd.get(100);
	}
	
	/**
	 * Calculate damage caused by falling
	 * @param cha
	 * @param fallHeight
	 * @return damage
	 */
	public static double calcFallDam(L2Character cha, int fallHeight)
	{
		if (!Config.ENABLE_FALLING_DAMAGE || (fallHeight < 0))
		{
			return 0;
		}
		return cha.getStat().getValue(Stats.FALL, (fallHeight * cha.getMaxHp()) / 1000.0);
	}
	
	public static boolean calcBlowSuccess(L2Character activeChar, L2Character target, Skill skill, double blowChance)
	{
		final double weaponCritical = 12; // Dagger weapon critical mod is 12... TODO: Make it work for other weapons.
		// double dexBonus = BaseStats.DEX.calcBonus(activeChar); Not used in GOD
		final double critHeightBonus = ((((CommonUtil.constrain(activeChar.getZ() - target.getZ(), -25, 25) * 4) / 5) + 10) / 100) + 1;
		final Position position = Position.getPosition(activeChar, target);
		final double criticalPosition = position == Position.BACK ? 1.3 : position == Position.SIDE ? 1.1 : 1; // 30% chance from back, 10% chance from side.
		final double criticalPositionMod = criticalPosition * activeChar.getStat().getPositionTypeValue(Stats.CRITICAL_RATE, position);
		final double blowRateMod = activeChar.getStat().getValue(Stats.BLOW_RATE, 1);
		blowChance = (weaponCritical + blowChance) * 10;
		
		final double rate = blowChance * critHeightBonus * criticalPositionMod * blowRateMod;
		
		// Blow rate is capped at 80%
		return Rnd.get(1000) < Math.min(rate, 800);
	}
	
	public static List<BuffInfo> calcCancelStealEffects(L2Character activeChar, L2Character target, Skill skill, DispelSlotType slot, int rate, int max)
	{
		final List<BuffInfo> canceled = new ArrayList<>(max);
		switch (slot)
		{
			case BUFF:
			{
				// Resist Modifier.
				final int cancelMagicLvl = skill.getMagicLevel();
				if (activeChar.isDebug())
				{
					final StatsSet set = new StatsSet();
					set.set("baseMod", rate);
					set.set("magicLevel", cancelMagicLvl);
					set.set("resMod", target.getStat().getValue(Stats.RESIST_DISPEL_BUFF, 1));
					set.set("rate", rate);
					Debug.sendSkillDebug(activeChar, target, skill, set);
				}
				
				// Prevent initialization.
				final List<BuffInfo> buffs = target.getEffectList().getBuffs();
				
				for (int i = buffs.size() - 1; i >= 0; i--) // reverse order
				{
					final BuffInfo info = buffs.get(i);
					if (!info.getSkill().canBeStolen() || ((rate < 100) && !calcCancelSuccess(info, cancelMagicLvl, rate, skill, target)))
					{
						continue;
					}
					canceled.add(info);
					if (canceled.size() >= max)
					{
						break;
					}
				}
				break;
			}
			case DEBUFF:
			{
				final List<BuffInfo> debuffs = target.getEffectList().getDebuffs();
				for (int i = debuffs.size() - 1; i >= 0; i--)
				{
					final BuffInfo info = debuffs.get(i);
					if (info.getSkill().canBeDispelled() && (Rnd.get(100) <= rate))
					{
						canceled.add(info);
						if (canceled.size() >= max)
						{
							break;
						}
					}
				}
				break;
			}
		}
		return canceled;
	}
	
	public static boolean calcCancelSuccess(BuffInfo info, int cancelMagicLvl, int rate, Skill skill, L2Character target)
	{
		final int chance = (int) (rate + ((cancelMagicLvl - info.getSkill().getMagicLevel()) * 2) + ((info.getAbnormalTime() / 120) * target.getStat().getValue(Stats.RESIST_DISPEL_BUFF, 1)));
		return Rnd.get(100) < CommonUtil.constrain(chance, 25, 75); // TODO: i_dispel_by_slot_probability min = 40, max = 95.
	}
	
	/**
	 * Calculates the abnormal time for an effect.<br>
	 * The abnormal time is taken from the skill definition, and it's global for all effects present in the skills.
	 * @param caster the caster
	 * @param target the target
	 * @param skill the skill
	 * @return the time that the effect will last
	 */
	public static int calcEffectAbnormalTime(L2Character caster, L2Character target, Skill skill)
	{
		int time = (skill == null) || skill.isPassive() || skill.isToggle() ? -1 : skill.getAbnormalTime();
		
		// If the skill is a mastery skill, the effect will last twice the default time.
		if ((skill != null) && Formulas.calcSkillMastery(caster, skill))
		{
			time *= 2;
		}
		
		return time;
	}
	
	/**
	 * Calculate Probability in following effects:<br>
	 * TargetCancel,<br>
	 * TargetMeProbability,<br>
	 * SkillTurning,<br>
	 * Betray,<br>
	 * Bluff,<br>
	 * DeleteHate,<br>
	 * RandomizeHate,<br>
	 * DeleteHateOfMe,<br>
	 * TransferHate,<br>
	 * Confuse<br>
	 * Knockback<br>
	 * Pull<br>
	 * @param baseChance chance from effect parameter
	 * @param attacker
	 * @param target
	 * @param skill
	 * @return chance for effect to succeed
	 */
	public static boolean calcProbability(double baseChance, L2Character attacker, L2Character target, Skill skill)
	{
		// Outdated formula: return Rnd.get(100) < ((((((skill.getMagicLevel() + baseChance) - target.getLevel()) + 30) - target.getINT()) * calcAttributeBonus(attacker, target, skill)) * calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false));
		// TODO: Find more retail-like formula
		return Rnd.get(100) < (((((skill.getMagicLevel() + baseChance) - target.getLevel()) - getAbnormalResist(skill.getBasicProperty(), target)) * calcAttributeBonus(attacker, target, skill)) * calcGeneralTraitBonus(attacker, target, skill.getTraitType(), false));
	}
	
	/**
	 * Calculates karma lost upon death.
	 * @param player
	 * @param finalExp
	 * @return the amount of karma player has loosed.
	 */
	public static int calculateKarmaLost(L2PcInstance player, double finalExp)
	{
		final double karmaLooseMul = KarmaData.getInstance().getMultiplier(player.getLevel());
		if (finalExp > 0) // Received exp
		{
			finalExp /= Config.RATE_KARMA_LOST;
		}
		return (int) ((Math.abs(finalExp) / karmaLooseMul) / 30);
	}
	
	/**
	 * Calculates karma gain upon playable kill.</br>
	 * Updated to High Five on 10.09.2014 by Zealar tested in retail.
	 * @param pkCount
	 * @param isSummon
	 * @return karma points that will be added to the player.
	 */
	public static int calculateKarmaGain(int pkCount, boolean isSummon)
	{
		int result = 43200;
		
		if (isSummon)
		{
			result = (int) ((((pkCount * 0.375) + 1) * 60) * 4) - 150;
			
			if (result > 10800)
			{
				return 10800;
			}
		}
		
		if (pkCount < 99)
		{
			result = (int) ((((pkCount * 0.5) + 1) * 60) * 12);
		}
		else if (pkCount < 180)
		{
			result = (int) ((((pkCount * 0.125) + 37.75) * 60) * 12);
		}
		
		return result;
	}
	
	public static double calcGeneralTraitBonus(L2Character attacker, L2Character target, TraitType traitType, boolean ignoreResistance)
	{
		if (traitType == TraitType.NONE)
		{
			return 1.0;
		}
		
		if (target.getStat().isTraitInvul(traitType))
		{
			return 0;
		}
		
		switch (traitType.getType())
		{
			case 2:
			{
				if (!attacker.getStat().hasAttackTrait(traitType) || !target.getStat().hasDefenceTrait(traitType))
				{
					return 1.0;
				}
				break;
			}
			case 3:
			{
				if (ignoreResistance)
				{
					return 1.0;
				}
				break;
			}
			default:
			{
				return 1.0;
			}
		}
		
		final double result = (attacker.getStat().getAttackTrait(traitType) - target.getStat().getDefenceTrait(traitType)) + 1.0;
		return CommonUtil.constrain(result, 0.05, 2.0);
	}
	
	public static double calcWeaponTraitBonus(L2Character attacker, L2Character target)
	{
		final TraitType type = attacker.getAttackType().getTraitType();
		final double result = target.getStat().getDefenceTraits()[type.getId()] - 1.0;
		return 1.0 - result;
	}
	
	public static double calcAttackTraitBonus(L2Character attacker, L2Character target)
	{
		final double weaponTraitBonus = calcWeaponTraitBonus(attacker, target);
		if (weaponTraitBonus == 0)
		{
			return 0;
		}
		
		double weaknessBonus = 1.0;
		for (TraitType traitType : TraitType.values())
		{
			if (traitType.getType() == 2)
			{
				weaknessBonus *= calcGeneralTraitBonus(attacker, target, traitType, true);
				if (weaknessBonus == 0)
				{
					return 0;
				}
			}
		}
		
		return CommonUtil.constrain((weaponTraitBonus * weaknessBonus), 0.05, 2.0);
	}
	
	public static double getBasicPropertyResistBonus(BasicProperty basicProperty, L2Character target)
	{
		if ((basicProperty == BasicProperty.NONE) || !target.hasBasicPropertyResist())
		{
			return 1.0;
		}
		
		final BasicPropertyResist resist = target.getBasicPropertyResist(basicProperty);
		switch (resist.getResistLevel())
		{
			case 0:
			{
				return 1.0;
			}
			case 1:
			{
				return 0.6;
			}
			case 2:
			{
				return 0.3;
			}
			default:
			{
				return 0;
			}
		}
	}
	
	/**
	 * Calculated damage caused by ATTACK of attacker on target.
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param shld
	 * @param crit if the ATTACK have critical success
	 * @param ss if weapon item was charged by soulshot
	 * @return
	 */
	public static double calcAutoAttackDamage(L2Character attacker, L2Character target, byte shld, boolean crit, boolean ss)
	{
		final double distance = attacker.calculateDistance(target, true, false);
		
		if (distance > target.getStat().getValue(Stats.SPHERIC_BARRIER_RANGE, Integer.MAX_VALUE))
		{
			return 0;
		}
		
		// DEFENCE CALCULATION (pDef + sDef)
		double defence = target.getPDef();
		
		switch (shld)
		{
			case SHIELD_DEFENSE_SUCCEED:
			{
				defence += target.getShldDef();
				break;
			}
			case SHIELD_DEFENSE_PERFECT_BLOCK:
			{
				return 1.;
			}
		}
		
		final L2Weapon weapon = attacker.getActiveWeaponItem();
		final boolean isRanged = (weapon != null) && weapon.getItemType().isRanged();
		final double shotsBonus = attacker.getStat().getValue(Stats.SHOTS_BONUS);
		
		final double cAtk = crit ? (2 * calcCritDamage(attacker, target, null)) : 1;
		final double cAtkAdd = crit ? calcCritDamageAdd(attacker, target, null) : 0;
		final double critMod = crit ? (isRanged ? 0.5 : 1) : 0;
		final double ssBonus = ss ? 2 * shotsBonus : 1;
		final double random_damage = attacker.getRandomDamageMultiplier();
		final double proxBonus = (attacker.isInFrontOf(target) ? 0 : (attacker.isBehind(target) ? 0.2 : 0.05)) * attacker.getPAtk();
		double attack = (attacker.getPAtk() * random_damage) + proxBonus;
		
		// ....................______________Critical Section___________________...._______Non-Critical Section______
		// ATTACK CALCULATION (((pAtk * cAtk * ss + cAtkAdd) * crit) * weaponMod) + (pAtk (1 - crit) * ss * weaponMod)
		// ````````````````````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^````^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		attack = ((((attack * cAtk * ssBonus) + cAtkAdd) * critMod) * (isRanged ? 154 : 77)) + (attack * (1 - critMod) * ssBonus * (isRanged ? 154 : 77));
		
		// DAMAGE CALCULATION (ATTACK / DEFENCE) * trait bonus * attr bonus * pvp bonus * pve bonus
		double damage = attack / defence;
		damage *= calcAttackTraitBonus(attacker, target);
		damage *= calcAttributeBonus(attacker, target, null);
		damage *= calculatePvpPveBonus(attacker, target, null, crit);
		
		damage = Math.max(0, damage);
		
		return damage;
	}
	
	public static double getAbnormalResist(BasicProperty basicProperty, L2Character target)
	{
		switch (basicProperty)
		{
			case PHYSICAL:
			{
				return target.getStat().getValue(Stats.ABNORMAL_RESIST_PHYSICAL);
			}
			case MAGIC:
			{
				return target.getStat().getValue(Stats.ABNORMAL_RESIST_MAGICAL);
			}
			default:
			{
				return 0;
			}
		}
	}
	
	public static double calcPveDamagePenalty(L2Character attacker, L2Character target, Skill skill, boolean crit)
	{
		if (target.isAttackable() && (target.getLevel() >= Config.MIN_NPC_LVL_DMG_PENALTY) && (attacker.getActingPlayer() != null) && ((target.getLevel() - attacker.getActingPlayer().getLevel()) > 1))
		{
			final int lvlDiff = target.getLevel() - attacker.getActingPlayer().getLevel() - 1;
			if (skill != null)
			{
				return Config.NPC_SKILL_DMG_PENALTY.get(Math.min(lvlDiff, Config.NPC_SKILL_DMG_PENALTY.size() - 1));
			}
			else if (crit)
			{
				return Config.NPC_CRIT_DMG_PENALTY.get(Math.min(lvlDiff, Config.NPC_CRIT_DMG_PENALTY.size() - 1));
			}
			
			return Config.NPC_DMG_PENALTY.get(Math.min(lvlDiff, Config.NPC_DMG_PENALTY.size() - 1));
		}
		
		return 1.0;
	}
	
	/**
	 * Calculates if the specified creature can get its stun effect removed due to damage taken.
	 * @param activeChar the character to be checked
	 * @return {@code true} if character should get its stun effects removed, {@code false} otherwise.
	 */
	public static boolean calcStunBreak(L2Character activeChar)
	{
		// Check if target is stunned and 10% chance (retail is 14% and 35% on crit?)
		if (activeChar.hasBlockActions() && (Rnd.get(10) == 0))
		{
			// Any stun that has double duration due to skill mastery, doesn't get removed until its time reaches the usual abnormal time.
			return activeChar.getEffectList().hasAbnormalType(AbnormalType.STUN, info -> info.getTime() <= info.getSkill().getAbnormalTime());
		}
		return false;
	}
	
	public static boolean calcRealTargetBreak()
	{
		// Real Target breaks at 3% (Rnd > 3.0 doesn't break) probability.
		return Rnd.get(100) <= 3;
	}
	
	/**
	 * @param attackSpeed the attack speed of the Creature.
	 * @return {@code 500000 / attackSpeed}.
	 */
	public static int calculateTimeBetweenAttacks(int attackSpeed)
	{
		// Measured Nov 2015 by Nik. Formula: atk.spd/500 = hits per second.
		return Math.max(50, (500000 / attackSpeed));
	}
	
	/**
	 * @param totalAttackTime the time needed to make a full attack.
	 * @param attackType the weapon type used for attack.
	 * @param twoHanded if the weapon is two handed.
	 * @param secondHit calculates the second hit for dual attacks.
	 * @return the time required from the start of the attack until you hit the target.
	 */
	public static int calculateTimeToHit(int totalAttackTime, WeaponType attackType, boolean twoHanded, boolean secondHit)
	{
		// Gracia Final Retail confirmed:
		// Time to damage (1 hand, 1 hit): TotalBasicAttackTime * 0.644
		// Time to damage (2 hand, 1 hit): TotalBasicAttackTime * 0.735
		// Time to damage (2 hand, 2 hit): TotalBasicAttackTime * 0.2726 and TotalBasicAttackTime * 0.6
		// Time to damage (bow/xbow): TotalBasicAttackTime * 0.978
		
		// Measured July 2016 by Nik.
		// Due to retail packet delay, we are unable to gather too accurate results. Therefore the below formulas are based on original Gracia Final values.
		// Any original values that appear higher than tested have been replaced with the tested values, because even with packet delay its obvious they are wrong.
		// All other original values are compared with the test results and differences are considered to be too insignificant and mostly caused due to packet delay.
		switch (attackType)
		{
			case BOW:
			case CROSSBOW:
			case TWOHANDCROSSBOW:
			{
				return (int) (totalAttackTime * 0.95);
			}
			case DUALBLUNT:
			case DUALDAGGER:
			case DUAL:
			case DUALFIST:
			{
				if (secondHit)
				{
					return (int) (totalAttackTime * 0.6);
				}
				
				return (int) (totalAttackTime * 0.2726);
			}
			default:
			{
				if (twoHanded)
				{
					return (int) (totalAttackTime * 0.735);
				}
				
				return (int) (totalAttackTime * 0.644);
			}
		}
	}
	
	/**
	 * @param activeChar
	 * @param weapon
	 * @return {@code (500_000 millis + 333 * WeaponItemReuseDelay) / PAttackSpeed}
	 */
	public static int calculateReuseTime(L2Character activeChar, L2Weapon weapon)
	{
		if (weapon == null)
		{
			return 0;
		}
		
		final WeaponType defaultAttackType = weapon.getItemType();
		final WeaponType weaponType = activeChar.getTransformation().map(transform -> transform.getBaseAttackType(activeChar, defaultAttackType)).orElse(defaultAttackType);
		int reuse = weapon.getReuseDelay();
		
		// only bows should continue for now
		if ((reuse == 0) || !weaponType.isRanged())
		{
			return 0;
		}
		
		reuse *= activeChar.getStat().getWeaponReuseModifier();
		double atkSpd = activeChar.getStat().getPAtkSpd();
		
		return (int) ((500000 + (333 * reuse)) / atkSpd);
	}
	
	public static double calculatePvpPveBonus(L2Character attacker, L2Character target, Skill skill, boolean crit)
	{
		// PvP bonus
		if (attacker.isPlayable() && target.isPlayable())
		{
			final double pvpAttack;
			final double pvpDefense;
			if (skill != null)
			{
				if (skill.isMagic())
				{
					// Magical Skill PvP
					pvpAttack = attacker.getStat().getValue(Stats.PVP_MAGICAL_SKILL_DAMAGE, 1);
					pvpDefense = target.getStat().getValue(Stats.PVP_MAGICAL_SKILL_DEFENCE, 1);
				}
				else
				{
					// Physical Skill PvP
					pvpAttack = attacker.getStat().getValue(Stats.PVP_PHYSICAL_SKILL_DAMAGE, 1);
					pvpDefense = target.getStat().getValue(Stats.PVP_PHYSICAL_SKILL_DEFENCE, 1);
				}
			}
			else
			{
				// Autoattack PvP
				pvpAttack = attacker.getStat().getValue(Stats.PVP_PHYSICAL_ATTACK_DAMAGE, 1);
				pvpDefense = target.getStat().getValue(Stats.PVP_PHYSICAL_ATTACK_DEFENCE, 1);
			}
			
			return 1 + (pvpAttack - pvpDefense);
		}
		
		// PvE Bonus
		if (target.isAttackable() || attacker.isAttackable())
		{
			final double pveAttack;
			final double pveDefense;
			final double pveRaidDefense;
			final double pvePenalty = calcPveDamagePenalty(attacker, target, skill, crit);
			
			if (skill != null)
			{
				if (skill.isMagic())
				{
					// Magical Skill PvE
					pveAttack = attacker.getStat().getValue(Stats.PVE_MAGICAL_SKILL_DAMAGE, 1);
					pveDefense = target.getStat().getValue(Stats.PVE_MAGICAL_SKILL_DEFENCE, 1);
					pveRaidDefense = attacker.isRaid() ? attacker.getStat().getValue(Stats.PVE_RAID_MAGICAL_SKILL_DEFENCE, 1) : 1;
				}
				else
				{
					// Physical Skill PvE
					pveAttack = attacker.getStat().getValue(Stats.PVE_PHYSICAL_SKILL_DAMAGE, 1);
					pveDefense = target.getStat().getValue(Stats.PVE_PHYSICAL_SKILL_DEFENCE, 1);
					pveRaidDefense = attacker.isRaid() ? attacker.getStat().getValue(Stats.PVE_RAID_PHYSICAL_SKILL_DEFENCE, 1) : 1;
				}
			}
			else
			{
				// Autoattack PvE
				pveAttack = attacker.getStat().getValue(Stats.PVE_PHYSICAL_ATTACK_DAMAGE, 1);
				pveDefense = target.getStat().getValue(Stats.PVE_PHYSICAL_ATTACK_DEFENCE, 1);
				pveRaidDefense = attacker.isRaid() ? attacker.getStat().getValue(Stats.PVE_RAID_PHYSICAL_ATTACK_DEFENCE, 1) : 1;
			}
			
			return (1 + (pveAttack - (pveDefense * pveRaidDefense))) * pvePenalty;
		}
		
		return 1;
	}
}
