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
package com.l2jmobius.gameserver.model.actor.stat;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.Experience;
import com.l2jmobius.gameserver.skills.Calculator;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.templates.L2Weapon;
import com.l2jmobius.gameserver.templates.L2WeaponType;

public class CharStat
{
	// =========================================================
	// Data Field
	private final L2Character _ActiveChar;
	private long _Exp = Experience.LEVEL[Config.STARTING_LEVEL];
	private int _Sp = 0;
	private byte _Level = Config.STARTING_LEVEL;
	
	// =========================================================
	// Constructor
	public CharStat(L2Character activeChar)
	{
		_ActiveChar = activeChar;
	}
	
	// =========================================================
	// Method - Public
	/**
	 * Calculate the new value of the state with modifiers that will be applied on the targeted L2Character.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A L2Character owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in
	 * the value property of an Env class instance.<BR>
	 * <BR>
	 * @param stat The stat to calculate the new value with modifiers
	 * @param init The initial value of the stat before applying modifiers
	 * @param target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
	 * @param skill The L2Skill whose properties will be used in the calculation (ex : Level...)
	 * @return
	 */
	public final double calcStat(Stats stat, double init, L2Character target, L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return init;
		}
		
		final int id = stat.ordinal();
		
		final Calculator c = _ActiveChar.getCalculators()[id];
		
		// If no Func object found, no modifier is applied
		if ((c == null) || (c.size() == 0))
		{
			return init;
		}
		
		// Create and init an Env object to pass parameters to the Calculator
		final Env env = new Env();
		env.player = _ActiveChar;
		env.target = target;
		env.skill = skill;
		env.value = init;
		
		// Launch the calculation
		c.calc(env);
		// avoid some troubles with negative stats (some stats should never be negative)
		if ((env.value < 1) && ((stat == Stats.MAX_HP) || (stat == Stats.MAX_MP) || (stat == Stats.MAX_CP) || (stat == Stats.MAGIC_DEFENCE) || (stat == Stats.POWER_DEFENCE) || (stat == Stats.POWER_ATTACK) || (stat == Stats.MAGIC_ATTACK) || (stat == Stats.POWER_ATTACK_SPEED) || (stat == Stats.MAGIC_ATTACK_SPEED) || (stat == Stats.SHIELD_DEFENCE) || (stat == Stats.STAT_CON) || (stat == Stats.STAT_DEX) || (stat == Stats.STAT_INT) || (stat == Stats.STAT_MEN) || (stat == Stats.STAT_STR) || (stat == Stats.STAT_WIT)))
		{
			env.value = 1;
		}
		
		return env.value;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	/**
	 * Return the Accuracy (base+modifier) of the L2Character.
	 * @return
	 */
	public int getAccuracy()
	{
		if (_ActiveChar == null)
		{
			return 0;
		}
		
		return (int) Math.round(calcStat(Stats.ACCURACY_COMBAT, 0, null, null));
	}
	
	public L2Character getActiveChar()
	{
		return _ActiveChar;
	}
	
	/**
	 * Return the Attack Speed multiplier (base+modifier) of the L2Character to get proper animations.
	 * @return
	 */
	public final float getAttackSpeedMultiplier()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (float) (((1.1) * getPAtkSpd()) / _ActiveChar.getTemplate().basePAtkSpd);
	}
	
	/**
	 * Return the CON of the L2Character (base+modifier).
	 * @return
	 */
	public final int getCON()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_CON, _ActiveChar.getTemplate().baseCON, null, null);
	}
	
	/**
	 * Return the Critical Damage rate (base+modifier) of the L2Character.
	 * @param target
	 * @param init
	 * @return
	 */
	public final double getCriticalDmg(L2Character target, double init)
	{
		return calcStat(Stats.CRITICAL_DAMAGE, init, target, null);
	}
	
	/**
	 * Return the Critical Hit rate (base+modifier) of the L2Character.
	 * @param target
	 * @param skill
	 * @return
	 */
	public int getCriticalHit(L2Character target, L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		int criticalHit = (int) Math.round(calcStat(Stats.CRITICAL_RATE, _ActiveChar.getTemplate().baseCritRate, target, skill));
		
		if (criticalHit > Config.MAX_PCRIT_RATE)
		{
			criticalHit = Config.MAX_PCRIT_RATE;
		}
		
		return criticalHit;
	}
	
	/**
	 * Return the DEX of the L2Character (base+modifier).
	 * @return
	 */
	public final int getDEX()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_DEX, _ActiveChar.getTemplate().baseDEX, null, null);
	}
	
	/**
	 * Return the Attack Evasion rate (base+modifier) of the L2Character.
	 * @param target
	 * @return
	 */
	public int getEvasionRate(L2Character target)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) Math.round(calcStat(Stats.EVASION_RATE, 0, target, null));
	}
	
	public long getExp()
	{
		return _Exp;
	}
	
	public void setExp(long value)
	{
		_Exp = value;
	}
	
	/**
	 * Return the INT of the L2Character (base+modifier).
	 * @return
	 */
	public int getINT()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_INT, _ActiveChar.getTemplate().baseINT, null, null);
	}
	
	public byte getLevel()
	{
		return _Level;
	}
	
	public void setLevel(byte value)
	{
		_Level = value;
	}
	
	/**
	 * Return the Magical Attack range (base+modifier) of the L2Character.
	 * @param skill
	 * @return
	 */
	public final int getMagicalAttackRange(L2Skill skill)
	{
		if (skill != null)
		{
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		}
		
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return _ActiveChar.getTemplate().baseAtkRange;
	}
	
	public int getMaxCp()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_CP, _ActiveChar.getTemplate().baseCpMax, null, null);
	}
	
	public int getMaxHp()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_HP, _ActiveChar.getTemplate().baseHpMax, null, null);
	}
	
	public int getMaxMp()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.MAX_MP, _ActiveChar.getTemplate().baseMpMax, null, null);
	}
	
	/**
	 * Return the MAtk (base+modifier) of the L2Character for a skill used in function of abnormal effects in progress.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li><BR>
	 * <BR>
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 * @return
	 */
	public int getMAtk(L2Character target, L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _ActiveChar.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}
		
		// Get the base MAtk of the L2Character
		final double attack = _ActiveChar.getTemplate().baseMAtk * bonusAtk;
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_ATTACK, attack, target, skill);
	}
	
	/**
	 * Return the MAtk Speed (base+modifier) of the L2Character.
	 * @return
	 */
	public int getMAtkSpd()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		float bonusSpdAtk = 1;
		if (Config.CHAMPION_ENABLE && _ActiveChar.isChampion())
		{
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		}
		
		double val = calcStat(Stats.MAGIC_ATTACK_SPEED, _ActiveChar.getTemplate().baseMAtkSpd * bonusSpdAtk, null, null);
		if (_ActiveChar instanceof L2PcInstance)
		{
			if ((val > Config.MAX_MATK_SPEED) && !((L2PcInstance) _ActiveChar).isGM())
			{
				val = Config.MAX_MATK_SPEED;
			}
		}
		return (int) val;
	}
	
	/**
	 * Return the Magic Critical Hit rate (base+modifier) of the L2Character.
	 * @param target
	 * @param skill
	 * @return
	 */
	public final int getMCriticalHit(L2Character target, L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		double mrate = calcStat(Stats.MCRITICAL_RATE, _ActiveChar.getTemplate().baseMCritRate, target, skill);
		
		if (mrate > Config.MAX_MCRIT_RATE)
		{
			mrate = Config.MAX_MCRIT_RATE;
		}
		
		return (int) mrate;
	}
	
	/**
	 * Return the MDef (base+modifier) of the L2Character against a skill in function of abnormal effects in progress.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Calculate Magic damage</li><BR>
	 * <BR>
	 * @param target The L2Character targeted by the skill
	 * @param skill The L2Skill used against the target
	 * @return
	 */
	public int getMDef(L2Character target, L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		// Get the base MAtk of the L2Character
		double defence = _ActiveChar.getTemplate().baseMDef;
		
		// Calculate modifier for Raid Bosses
		if (_ActiveChar.isRaid())
		{
			defence *= Config.RAID_MDEFENCE_MULTIPLIER;
		}
		
		// Calculate modifiers Magic Attack
		return (int) calcStat(Stats.MAGIC_DEFENCE, defence, target, skill);
	}
	
	/**
	 * Return the MEN of the L2Character (base+modifier).
	 * @return
	 */
	public final int getMEN()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_MEN, _ActiveChar.getTemplate().baseMEN, null, null);
	}
	
	public float getMovementSpeedMultiplier()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return getRunSpeed() / (float) _ActiveChar.getTemplate().baseRunSpd;
	}
	
	/**
	 * Return the RunSpeed (base+modifier) or WalkSpeed (base+modifier) of the L2Character in function of the movement type.
	 * @return
	 */
	public float getMoveSpeed()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		if (_ActiveChar.isRunning())
		{
			return getRunSpeed();
		}
		return getWalkSpeed();
	}
	
	/**
	 * Return the MReuse rate (base+modifier) of the L2Character.
	 * @param skill
	 * @return
	 */
	public final double getMReuseRate(L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return calcStat(Stats.MAGIC_REUSE_RATE, _ActiveChar.getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PReuse rate (base+modifier) of the L2Character.
	 * @param skill
	 * @return
	 */
	public final double getPReuseRate(L2Skill skill)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return calcStat(Stats.PHYSICAL_REUSE_RATE, _ActiveChar.getTemplate().baseMReuseRate, null, skill);
	}
	
	/**
	 * Return the PAtk (base+modifier) of the L2Character.
	 * @param target
	 * @return
	 */
	public int getPAtk(L2Character target)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		float bonusAtk = 1;
		if (Config.CHAMPION_ENABLE && _ActiveChar.isChampion())
		{
			bonusAtk = Config.CHAMPION_ATK;
		}
		
		return (int) calcStat(Stats.POWER_ATTACK, _ActiveChar.getTemplate().basePAtk * bonusAtk, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against animals.
	 * @param target
	 * @return
	 */
	public final double getPAtkAnimals(L2Character target)
	{
		return calcStat(Stats.PATK_ANIMALS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against dragons.
	 * @param target
	 * @return
	 */
	public final double getPAtkDragons(L2Character target)
	{
		return calcStat(Stats.PATK_DRAGONS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against insects.
	 * @param target
	 * @return
	 */
	public final double getPAtkInsects(L2Character target)
	{
		return calcStat(Stats.PATK_INSECTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against monsters.
	 * @param target
	 * @return
	 */
	public final double getPAtkMonsters(L2Character target)
	{
		return calcStat(Stats.PATK_MONSTERS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against plants.
	 * @param target
	 * @return
	 */
	public final double getPAtkPlants(L2Character target)
	{
		return calcStat(Stats.PATK_PLANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against giants.
	 * @param target
	 * @return
	 */
	public final double getPAtkGiants(L2Character target)
	{
		return calcStat(Stats.PATK_GIANTS, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against magic creatures.
	 * @param target
	 * @return
	 */
	public final double getPAtkMCreatures(L2Character target)
	{
		return calcStat(Stats.PATK_MCREATURES, 1, target, null);
	}
	
	/**
	 * Return the PAtk Modifier against undead.
	 * @param target
	 * @return
	 */
	public final double getPAtkUndead(L2Character target)
	{
		return calcStat(Stats.PATK_UNDEAD, 1, target, null);
	}
	
	public final double getPDefUndead(L2Character target)
	{
		return calcStat(Stats.PDEF_UNDEAD, 1, target, null);
	}
	
	/**
	 * Return the PAtk Speed (base+modifier) of the L2Character.
	 * @return
	 */
	public int getPAtkSpd()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		float bonusSpdAtk = 1;
		if (Config.CHAMPION_ENABLE && _ActiveChar.isChampion())
		{
			bonusSpdAtk = Config.CHAMPION_SPD_ATK;
		}
		
		int val = (int) Math.round(calcStat(Stats.POWER_ATTACK_SPEED, _ActiveChar.getTemplate().basePAtkSpd * bonusSpdAtk, null, null));
		if (_ActiveChar instanceof L2PcInstance)
		{
			if ((val > Config.MAX_PATK_SPEED) && !((L2PcInstance) _ActiveChar).isGM())
			{
				val = Config.MAX_PATK_SPEED;
			}
		}
		return val;
	}
	
	/**
	 * Return the PDef (base+modifier) of the L2Character.
	 * @param target
	 * @return
	 */
	public int getPDef(L2Character target)
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.POWER_DEFENCE, (_ActiveChar.isRaid()) ? _ActiveChar.getTemplate().basePDef * Config.RAID_PDEFENCE_MULTIPLIER : _ActiveChar.getTemplate().basePDef, target, null);
	}
	
	/**
	 * Return the Physical Attack range (base+modifier) of the L2Character.
	 * @return
	 */
	public final int getPhysicalAttackRange()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		// Polearm handled here for now.
		final L2Weapon weaponItem = _ActiveChar.getActiveWeaponItem();
		if ((weaponItem != null) && (weaponItem.getItemType() == L2WeaponType.POLE))
		{
			return (int) calcStat(Stats.POWER_ATTACK_RANGE, 66, null, null);
		}
		
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, _ActiveChar.getTemplate().baseAtkRange, null, null);
	}
	
	/**
	 * Return the weapon reuse modifier.
	 * @param target
	 * @return
	 */
	public final double getWeaponReuseModifier(L2Character target)
	{
		return calcStat(Stats.ATK_REUSE, 1, target, null);
	}
	
	/**
	 * Return the RunSpeed (base+modifier) of the L2Character.
	 * @return
	 */
	public int getRunSpeed()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		// err we should be adding to the persons run speed
		// not making it a constant
		final double baseRunSpd = _ActiveChar.getTemplate().baseRunSpd;
		if (baseRunSpd == 0)
		{
			return 0;
		}
		
		return (int) Math.round(calcStat(Stats.RUN_SPEED, baseRunSpd, null, null));
	}
	
	/**
	 * Return the ShieldDef rate (base+modifier) of the L2Character.
	 * @return
	 */
	public final int getShldDef()
	{
		return (int) calcStat(Stats.SHIELD_DEFENCE, 0, null, null);
	}
	
	public int getSp()
	{
		return _Sp;
	}
	
	public void setSp(int value)
	{
		_Sp = value;
	}
	
	/**
	 * Return the STR of the L2Character (base+modifier).
	 * @return
	 */
	public final int getSTR()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_STR, _ActiveChar.getTemplate().baseSTR, null, null);
	}
	
	/**
	 * Return the WalkSpeed (base+modifier) of the L2Character.
	 * @return
	 */
	public int getWalkSpeed()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		final double baseWalkSpd = _ActiveChar.getTemplate().baseWalkSpd;
		if (baseWalkSpd == 0)
		{
			return 0;
		}
		
		return (int) calcStat(Stats.WALK_SPEED, baseWalkSpd, null, null);
	}
	
	/**
	 * Return the WIT of the L2Character (base+modifier).
	 * @return
	 */
	public final int getWIT()
	{
		if (_ActiveChar == null)
		{
			return 1;
		}
		
		return (int) calcStat(Stats.STAT_WIT, _ActiveChar.getTemplate().baseWIT, null, null);
	}
	
	/**
	 * Return the mpConsume.
	 * @param skill
	 * @return
	 */
	public final int getMpConsume(L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		double mpConsume = skill.getMpConsume();
		if ((skill.getNextDanceMpCost() > 0) && (_ActiveChar.getDanceCount() > 0))
		{
			mpConsume += _ActiveChar.getDanceCount() * skill.getNextDanceMpCost();
		}
		
		mpConsume = calcStat(Stats.MP_CONSUME, mpConsume, null, skill);
		
		if (skill.isMagic())
		{
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
		}
		return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
	}
	
	/**
	 * Return the mpInitialConsume.
	 * @param skill
	 * @return
	 */
	public final int getMpInitialConsume(L2Skill skill)
	{
		if (skill == null)
		{
			return 1;
		}
		
		final double mpConsume = calcStat(Stats.MP_CONSUME, skill.getMpInitialConsume(), null, skill);
		
		if (skill.isMagic())
		{
			return (int) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null);
		}
		return (int) calcStat(Stats.PHYSICAL_MP_CONSUME_RATE, mpConsume, null, null);
	}
}