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
package org.l2jmobius.gameserver.skills;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.instancemanager.ClanHallManager;
import org.l2jmobius.gameserver.instancemanager.ClassDamageManager;
import org.l2jmobius.gameserver.instancemanager.SiegeManager;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.SiegeClan;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.CubicInstance;
import org.l2jmobius.gameserver.model.actor.instance.DoorInstance;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.actor.templates.PlayerTemplate;
import org.l2jmobius.gameserver.model.entity.ClanHall;
import org.l2jmobius.gameserver.model.entity.sevensigns.SevenSigns;
import org.l2jmobius.gameserver.model.entity.sevensigns.SevenSignsFestival;
import org.l2jmobius.gameserver.model.entity.siege.Siege;
import org.l2jmobius.gameserver.model.items.Armor;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.type.WeaponType;
import org.l2jmobius.gameserver.model.zone.ZoneId;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.skills.conditions.ConditionPlayerState;
import org.l2jmobius.gameserver.skills.conditions.ConditionPlayerState.CheckPlayerState;
import org.l2jmobius.gameserver.skills.conditions.ConditionUsingItemType;
import org.l2jmobius.gameserver.skills.effects.EffectTemplate;
import org.l2jmobius.gameserver.skills.funcs.Func;
import org.l2jmobius.gameserver.util.Util;

/**
 * Global calculations, can be modified by server admins
 */
public class Formulas
{
	protected static final Logger LOGGER = Logger.getLogger(Creature.class.getName());
	
	private static final int HP_REGENERATE_PERIOD = 3000; // 3 secs
	
	static class FuncAddLevel3 extends Func
	{
		static final FuncAddLevel3[] _instancies = new FuncAddLevel3[Stats.NUM_STATS];
		
		static Func getInstance(Stats stat)
		{
			final int pos = stat.ordinal();
			
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncAddLevel3(stat);
			}
			return _instancies[pos];
		}
		
		private FuncAddLevel3(Stats pStat)
		{
			super(pStat, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value += env.player.getLevel() / 3.0;
		}
	}
	
	static class FuncMultLevelMod extends Func
	{
		static final FuncMultLevelMod[] _instancies = new FuncMultLevelMod[Stats.NUM_STATS];
		
		static Func getInstance(Stats stat)
		{
			final int pos = stat.ordinal();
			
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultLevelMod(stat);
			}
			return _instancies[pos];
		}
		
		private FuncMultLevelMod(Stats pStat)
		{
			super(pStat, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= env.player.getLevelMod();
		}
	}
	
	static class FuncMultRegenResting extends Func
	{
		static final FuncMultRegenResting[] _instancies = new FuncMultRegenResting[Stats.NUM_STATS];
		
		/**
		 * @param stat
		 * @return the Func object corresponding to the state concerned.
		 */
		static Func getInstance(Stats stat)
		{
			final int pos = stat.ordinal();
			
			if (_instancies[pos] == null)
			{
				_instancies[pos] = new FuncMultRegenResting(stat);
			}
			
			return _instancies[pos];
		}
		
		/**
		 * Constructor of the FuncMultRegenResting.<BR>
		 * <BR>
		 * @param pStat
		 */
		private FuncMultRegenResting(Stats pStat)
		{
			super(pStat, 0x20, null);
			setCondition(new ConditionPlayerState(CheckPlayerState.RESTING, true));
		}
		
		/**
		 * Calculate the modifier of the state concerned.<BR>
		 * <BR>
		 */
		@Override
		public void calc(Env env)
		{
			if (!cond.test(env))
			{
				return;
			}
			
			env.value *= 1.45;
		}
	}
	
	static class FuncPAtkMod extends Func
	{
		static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();
		
		static Func getInstance()
		{
			return _fpa_instance;
		}
		
		private FuncPAtkMod()
		{
			super(Stats.POWER_ATTACK, 0x30, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if (env.player instanceof PetInstance)
			{
				if (env.player.getActiveWeaponInstance() != null)
				{
					env.value *= BaseStats.STR.calcBonus(env.player);
				}
			}
			else
			{
				env.value *= BaseStats.STR.calcBonus(env.player) * env.player.getLevelMod();
			}
		}
	}
	
	static class FuncMAtkMod extends Func
	{
		static final FuncMAtkMod _fma_instance = new FuncMAtkMod();
		
		static Func getInstance()
		{
			return _fma_instance;
		}
		
		private FuncMAtkMod()
		{
			super(Stats.MAGIC_ATTACK, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final double intb = BaseStats.INT.calcBonus(env.player);
			final double lvlb = env.player.getLevelMod();
			env.value *= (lvlb * lvlb) * (intb * intb);
		}
	}
	
	static class FuncMDefMod extends Func
	{
		static final FuncMDefMod _fmm_instance = new FuncMDefMod();
		
		static Func getInstance()
		{
			return _fmm_instance;
		}
		
		private FuncMDefMod()
		{
			super(Stats.MAGIC_DEFENCE, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if (env.player instanceof PlayerInstance)
			{
				final PlayerInstance p = (PlayerInstance) env.player;
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LFINGER) != null)
				{
					env.value -= 5;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RFINGER) != null)
				{
					env.value -= 5;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEAR) != null)
				{
					env.value -= 9;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_REAR) != null)
				{
					env.value -= 9;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK) != null)
				{
					env.value -= 13;
				}
			}
			env.value *= BaseStats.MEN.calcBonus(env.player) * env.player.getLevelMod();
		}
	}
	
	static class FuncPDefMod extends Func
	{
		static final FuncPDefMod _fmm_instance = new FuncPDefMod();
		
		static Func getInstance()
		{
			return _fmm_instance;
		}
		
		private FuncPDefMod()
		{
			super(Stats.POWER_DEFENCE, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			if (env.player instanceof PlayerInstance)
			{
				final PlayerInstance p = (PlayerInstance) env.player;
				final boolean hasMagePDef = (p.getClassId().isMage() || (p.getClassId().getId() == 0x31)); // orc mystics are a special case
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_HEAD) != null)
				{
					env.value -= 12;
				}
				final ItemInstance chest = p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
				if (chest != null)
				{
					env.value -= hasMagePDef ? 15 : 31;
				}
				if ((p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS) != null) || ((chest != null) && (chest.getItem().getBodyPart() == Item.SLOT_FULL_ARMOR)))
				{
					env.value -= hasMagePDef ? 8 : 18;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_GLOVES) != null)
				{
					env.value -= 8;
				}
				if (p.getInventory().getPaperdollItem(Inventory.PAPERDOLL_FEET) != null)
				{
					env.value -= 7;
				}
			}
			env.value *= env.player.getLevelMod();
		}
	}
	
	static class FuncBowAtkRange extends Func
	{
		private static final FuncBowAtkRange _fbar_instance = new FuncBowAtkRange();
		
		static Func getInstance()
		{
			return _fbar_instance;
		}
		
		private FuncBowAtkRange()
		{
			super(Stats.POWER_ATTACK_RANGE, 0x10, null);
			setCondition(new ConditionUsingItemType(WeaponType.BOW.mask()));
		}
		
		@Override
		public void calc(Env env)
		{
			if (!cond.test(env))
			{
				return;
			}
			env.value += 460;
		}
	}
	
	static class FuncAtkAccuracy extends Func
	{
		static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();
		
		static Func getInstance()
		{
			return _faa_instance;
		}
		
		private FuncAtkAccuracy()
		{
			super(Stats.ACCURACY_COMBAT, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();
			// [Square(DEX)]*6 + lvl + weapon hitbonus;
			
			final Creature p = env.player;
			if (p instanceof PetInstance)
			{
				env.value += Math.sqrt(env.player.getDEX());
			}
			else
			{
				env.value += Math.sqrt(env.player.getDEX()) * 6;
				env.value += level;
				if (level > 77)
				{
					env.value += (level - 77);
				}
				if (level > 69)
				{
					env.value += (level - 69);
				}
				if (env.player instanceof Summon)
				{
					env.value += (level < 60) ? 4 : 5;
				}
			}
		}
	}
	
	static class FuncAtkEvasion extends Func
	{
		static final FuncAtkEvasion _fae_instance = new FuncAtkEvasion();
		
		static Func getInstance()
		{
			return _fae_instance;
		}
		
		private FuncAtkEvasion()
		{
			super(Stats.EVASION_RATE, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final int level = env.player.getLevel();
			
			final Creature p = env.player;
			if (p instanceof PetInstance)
			{
				env.value += Math.sqrt(env.player.getDEX());
			}
			else
			{
				env.value += Math.sqrt(env.player.getDEX()) * 6;
				env.value += level;
				if (level > 77)
				{
					env.value += (level - 77);
				}
				if (level > 69)
				{
					env.value += (level - 69);
				}
			}
		}
	}
	
	static class FuncAtkCritical extends Func
	{
		static final FuncAtkCritical _fac_instance = new FuncAtkCritical();
		
		static Func getInstance()
		{
			return _fac_instance;
		}
		
		private FuncAtkCritical()
		{
			super(Stats.CRITICAL_RATE, 0x09, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
			
			final Creature p = env.player;
			if (!(p instanceof PetInstance))
			{
				env.value *= 10;
			}
			
			env.baseValue = env.value;
		}
	}
	
	static class FuncMAtkCritical extends Func
	{
		static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();
		
		static Func getInstance()
		{
			return _fac_instance;
		}
		
		private FuncMAtkCritical()
		{
			super(Stats.MCRITICAL_RATE, 0x30, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final Creature p = env.player;
			if (p instanceof Summon)
			{
				env.value = 8; // TODO: needs retail value
			}
			else if ((p instanceof PlayerInstance) && (p.getActiveWeaponInstance() != null))
			{
				env.value *= BaseStats.WIT.calcBonus(p);
			}
		}
	}
	
	static class FuncMoveSpeed extends Func
	{
		static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();
		
		static Func getInstance()
		{
			return _fms_instance;
		}
		
		private FuncMoveSpeed()
		{
			super(Stats.RUN_SPEED, 0x30, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
		}
	}
	
	static class FuncPAtkSpeed extends Func
	{
		static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();
		
		static Func getInstance()
		{
			return _fas_instance;
		}
		
		private FuncPAtkSpeed()
		{
			super(Stats.POWER_ATTACK_SPEED, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.DEX.calcBonus(env.player);
		}
	}
	
	static class FuncMAtkSpeed extends Func
	{
		static final FuncMAtkSpeed _fas_instance = new FuncMAtkSpeed();
		
		static Func getInstance()
		{
			return _fas_instance;
		}
		
		private FuncMAtkSpeed()
		{
			super(Stats.MAGIC_ATTACK_SPEED, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.WIT.calcBonus(env.player);
		}
	}
	
	static class FuncHennaSTR extends Func
	{
		static final FuncHennaSTR _fh_instance = new FuncHennaSTR();
		
		static Func getInstance()
		{
			return _fh_instance;
		}
		
		private FuncHennaSTR()
		{
			super(Stats.STAT_STR, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerInstance pc = (PlayerInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatSTR();
			}
		}
	}
	
	static class FuncHennaDEX extends Func
	{
		static final FuncHennaDEX _fh_instance = new FuncHennaDEX();
		
		static Func getInstance()
		{
			return _fh_instance;
		}
		
		private FuncHennaDEX()
		{
			super(Stats.STAT_DEX, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerInstance pc = (PlayerInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatDEX();
			}
		}
	}
	
	static class FuncHennaINT extends Func
	{
		static final FuncHennaINT _fh_instance = new FuncHennaINT();
		
		static Func getInstance()
		{
			return _fh_instance;
		}
		
		private FuncHennaINT()
		{
			super(Stats.STAT_INT, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerInstance pc = (PlayerInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatINT();
			}
		}
	}
	
	static class FuncHennaMEN extends Func
	{
		static final FuncHennaMEN _fh_instance = new FuncHennaMEN();
		
		static Func getInstance()
		{
			return _fh_instance;
		}
		
		private FuncHennaMEN()
		{
			super(Stats.STAT_MEN, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerInstance pc = (PlayerInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatMEN();
			}
		}
	}
	
	static class FuncHennaCON extends Func
	{
		static final FuncHennaCON _fh_instance = new FuncHennaCON();
		
		static Func getInstance()
		{
			return _fh_instance;
		}
		
		private FuncHennaCON()
		{
			super(Stats.STAT_CON, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerInstance pc = (PlayerInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatCON();
			}
		}
	}
	
	static class FuncHennaWIT extends Func
	{
		static final FuncHennaWIT _fh_instance = new FuncHennaWIT();
		
		static Func getInstance()
		{
			return _fh_instance;
		}
		
		private FuncHennaWIT()
		{
			super(Stats.STAT_WIT, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerInstance pc = (PlayerInstance) env.player;
			if (pc != null)
			{
				env.value += pc.getHennaStatWIT();
			}
		}
	}
	
	static class FuncMaxHpAdd extends Func
	{
		static final FuncMaxHpAdd _fmha_instance = new FuncMaxHpAdd();
		
		static Func getInstance()
		{
			return _fmha_instance;
		}
		
		private FuncMaxHpAdd()
		{
			super(Stats.MAX_HP, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerTemplate t = (PlayerTemplate) env.player.getTemplate();
			final int lvl = env.player.getLevel() - t.classBaseLevel;
			final double hpmod = t.lvlHpMod * lvl;
			final double hpmax = (t.lvlHpAdd + hpmod) * lvl;
			final double hpmin = (t.lvlHpAdd * lvl) + hpmod;
			env.value += (hpmax + hpmin) / 2;
		}
	}
	
	static class FuncMaxHpMul extends Func
	{
		static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();
		
		static Func getInstance()
		{
			return _fmhm_instance;
		}
		
		private FuncMaxHpMul()
		{
			super(Stats.MAX_HP, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}
	
	static class FuncMaxCpAdd extends Func
	{
		static final FuncMaxCpAdd _fmca_instance = new FuncMaxCpAdd();
		
		static Func getInstance()
		{
			return _fmca_instance;
		}
		
		private FuncMaxCpAdd()
		{
			super(Stats.MAX_CP, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerTemplate t = (PlayerTemplate) env.player.getTemplate();
			final int lvl = env.player.getLevel() - t.classBaseLevel;
			final double cpmod = t.lvlCpMod * lvl;
			final double cpmax = (t.lvlCpAdd + cpmod) * lvl;
			final double cpmin = (t.lvlCpAdd * lvl) + cpmod;
			env.value += (cpmax + cpmin) / 2;
		}
	}
	
	static class FuncMaxCpMul extends Func
	{
		static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();
		
		static Func getInstance()
		{
			return _fmcm_instance;
		}
		
		private FuncMaxCpMul()
		{
			super(Stats.MAX_CP, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.CON.calcBonus(env.player);
		}
	}
	
	static class FuncMaxMpAdd extends Func
	{
		static final FuncMaxMpAdd _fmma_instance = new FuncMaxMpAdd();
		
		static Func getInstance()
		{
			return _fmma_instance;
		}
		
		private FuncMaxMpAdd()
		{
			super(Stats.MAX_MP, 0x10, null);
		}
		
		@Override
		public void calc(Env env)
		{
			final PlayerTemplate t = (PlayerTemplate) env.player.getTemplate();
			final int lvl = env.player.getLevel() - t.classBaseLevel;
			final double mpmod = t.lvlMpMod * lvl;
			final double mpmax = (t.lvlMpAdd + mpmod) * lvl;
			final double mpmin = (t.lvlMpAdd * lvl) + mpmod;
			env.value += (mpmax + mpmin) / 2;
		}
	}
	
	static class FuncMaxMpMul extends Func
	{
		static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();
		
		static Func getInstance()
		{
			return _fmmm_instance;
		}
		
		private FuncMaxMpMul()
		{
			super(Stats.MAX_MP, 0x20, null);
		}
		
		@Override
		public void calc(Env env)
		{
			env.value *= BaseStats.MEN.calcBonus(env.player);
		}
	}
	
	private static final Formulas INSTANCE = new Formulas();
	
	public static Formulas getInstance()
	{
		return INSTANCE;
	}
	
	private Formulas()
	{
	}
	
	/**
	 * @param creature
	 * @return the period between 2 regeneration task (3s for Creature, 5 min for DoorInstance).
	 */
	public static int getRegeneratePeriod(Creature creature)
	{
		if (creature instanceof DoorInstance)
		{
			return HP_REGENERATE_PERIOD * 100; // 5 mins
		}
		
		return HP_REGENERATE_PERIOD; // 3s
	}
	
	/**
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a Mathematics function : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * To reduce cache memory use, NPCInstances who don't have skills share the same Calculator set called <B>NPC_STD_CALCULATOR</B>.<BR>
	 * <BR>
	 * @return the standard NPC Calculator set containing ACCURACY_COMBAT and EVASION_RATE.
	 */
	public Calculator[] getStdNPCCalculators()
	{
		final Calculator[] std = new Calculator[Stats.NUM_STATS];
		
		std[Stats.MAX_HP.ordinal()] = new Calculator();
		std[Stats.MAX_HP.ordinal()].addFunc(FuncMaxHpMul.getInstance());
		
		std[Stats.MAX_MP.ordinal()] = new Calculator();
		std[Stats.MAX_MP.ordinal()].addFunc(FuncMaxMpMul.getInstance());
		
		std[Stats.POWER_ATTACK.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK.ordinal()].addFunc(FuncPAtkMod.getInstance());
		
		std[Stats.MAGIC_ATTACK.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK.ordinal()].addFunc(FuncMAtkMod.getInstance());
		
		std[Stats.POWER_DEFENCE.ordinal()] = new Calculator();
		std[Stats.POWER_DEFENCE.ordinal()].addFunc(FuncPDefMod.getInstance());
		
		std[Stats.MAGIC_DEFENCE.ordinal()] = new Calculator();
		std[Stats.MAGIC_DEFENCE.ordinal()].addFunc(FuncMDefMod.getInstance());
		
		std[Stats.CRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.CRITICAL_RATE.ordinal()].addFunc(FuncAtkCritical.getInstance());
		
		std[Stats.MCRITICAL_RATE.ordinal()] = new Calculator();
		std[Stats.MCRITICAL_RATE.ordinal()].addFunc(FuncMAtkCritical.getInstance());
		
		std[Stats.ACCURACY_COMBAT.ordinal()] = new Calculator();
		std[Stats.ACCURACY_COMBAT.ordinal()].addFunc(FuncAtkAccuracy.getInstance());
		
		std[Stats.EVASION_RATE.ordinal()] = new Calculator();
		std[Stats.EVASION_RATE.ordinal()].addFunc(FuncAtkEvasion.getInstance());
		
		std[Stats.POWER_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.POWER_ATTACK_SPEED.ordinal()].addFunc(FuncPAtkSpeed.getInstance());
		
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()] = new Calculator();
		std[Stats.MAGIC_ATTACK_SPEED.ordinal()].addFunc(FuncMAtkSpeed.getInstance());
		
		std[Stats.RUN_SPEED.ordinal()] = new Calculator();
		std[Stats.RUN_SPEED.ordinal()].addFunc(FuncMoveSpeed.getInstance());
		
		return std;
	}
	
	/**
	 * Add basics Func objects to PlayerInstance and Summon.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR>
	 * <BR>
	 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR>
	 * <BR>
	 * @param creature PlayerInstance or Summon that must obtain basic Func objects
	 */
	public void addFuncsToNewCharacter(Creature creature)
	{
		if (creature instanceof PlayerInstance)
		{
			creature.addStatFunc(FuncMaxHpAdd.getInstance());
			creature.addStatFunc(FuncMaxHpMul.getInstance());
			creature.addStatFunc(FuncMaxCpAdd.getInstance());
			creature.addStatFunc(FuncMaxCpMul.getInstance());
			creature.addStatFunc(FuncMaxMpAdd.getInstance());
			creature.addStatFunc(FuncMaxMpMul.getInstance());
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_CP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			creature.addStatFunc(FuncBowAtkRange.getInstance());
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_ATTACK));
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.POWER_DEFENCE));
			// cha.addStatFunc(FuncMultLevelMod.getInstance(Stats.MAGIC_DEFENCE));
			creature.addStatFunc(FuncPAtkMod.getInstance());
			creature.addStatFunc(FuncMAtkMod.getInstance());
			creature.addStatFunc(FuncPDefMod.getInstance());
			creature.addStatFunc(FuncMDefMod.getInstance());
			creature.addStatFunc(FuncAtkCritical.getInstance());
			creature.addStatFunc(FuncMAtkCritical.getInstance());
			creature.addStatFunc(FuncAtkAccuracy.getInstance());
			creature.addStatFunc(FuncAtkEvasion.getInstance());
			creature.addStatFunc(FuncPAtkSpeed.getInstance());
			creature.addStatFunc(FuncMAtkSpeed.getInstance());
			creature.addStatFunc(FuncMoveSpeed.getInstance());
			
			creature.addStatFunc(FuncHennaSTR.getInstance());
			creature.addStatFunc(FuncHennaDEX.getInstance());
			creature.addStatFunc(FuncHennaINT.getInstance());
			creature.addStatFunc(FuncHennaMEN.getInstance());
			creature.addStatFunc(FuncHennaCON.getInstance());
			creature.addStatFunc(FuncHennaWIT.getInstance());
		}
		else if (creature instanceof PetInstance)
		{
			creature.addStatFunc(FuncPAtkMod.getInstance());
			// cha.addStatFunc(FuncMAtkMod.getInstance());
			// cha.addStatFunc(FuncPDefMod.getInstance());
			creature.addStatFunc(FuncMDefMod.getInstance());
			creature.addStatFunc(FuncAtkCritical.getInstance());
			creature.addStatFunc(FuncMAtkCritical.getInstance());
			creature.addStatFunc(FuncAtkAccuracy.getInstance());
			creature.addStatFunc(FuncAtkEvasion.getInstance());
			creature.addStatFunc(FuncMoveSpeed.getInstance());
			creature.addStatFunc(FuncPAtkSpeed.getInstance());
			creature.addStatFunc(FuncMAtkSpeed.getInstance());
		}
		else if (creature instanceof Summon)
		{
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_HP_RATE));
			// cha.addStatFunc(FuncMultRegenResting.getInstance(Stats.REGENERATE_MP_RATE));
			creature.addStatFunc(FuncAtkCritical.getInstance());
			creature.addStatFunc(FuncMAtkCritical.getInstance());
			creature.addStatFunc(FuncAtkAccuracy.getInstance());
			creature.addStatFunc(FuncAtkEvasion.getInstance());
			creature.addStatFunc(FuncMoveSpeed.getInstance());
		}
	}
	
	/**
	 * Calculate the HP regen rate (base + modifiers).<BR>
	 * <BR>
	 * @param creature
	 * @return
	 */
	public static final double calcHpRegen(Creature creature)
	{
		double init = creature.getTemplate().baseHpReg;
		double hpRegenMultiplier = creature.isRaid() ? Config.RAID_HP_REGEN_MULTIPLIER : Config.HP_REGEN_MULTIPLIER;
		double hpRegenBonus = 0;
		
		if (Config.L2JMOD_CHAMPION_ENABLE && creature.isChampion())
		{
			hpRegenMultiplier *= Config.L2JMOD_CHAMPION_HP_REGEN;
		}
		
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			
			// Calculate correct baseHpReg value for certain level of PC
			init += (player.getLevel() > 10) ? ((player.getLevel() - 1) / 10.0) : 0.5;
			
			// SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				hpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			else
			{
				final double siegeModifier = calcSiegeRegenModifer(player);
				if (siegeModifier > 0)
				{
					hpRegenMultiplier *= siegeModifier;
				}
			}
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && (player.getClan() != null))
			{
				final int clanHallIndex = player.getClan().getHasHideout();
				if (clanHallIndex > 0)
				{
					final ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
						{
							hpRegenMultiplier *= 1 + (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).getLvl() / 100);
						}
					}
				}
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(ZoneId.MOTHERTREE))
			{
				hpRegenBonus += 2;
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				hpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				hpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				hpRegenMultiplier *= 0.7; // Running
			}
			
			// Add CON bonus
			init *= creature.getLevelMod() * BaseStats.CON.calcBonus(creature);
		}
		
		if (init < 1)
		{
			init = 1;
		}
		
		return (creature.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier) + hpRegenBonus;
	}
	
	/**
	 * Calculate the MP regen rate (base + modifiers).<BR>
	 * <BR>
	 * @param creature
	 * @return
	 */
	public static final double calcMpRegen(Creature creature)
	{
		double init = creature.getTemplate().baseMpReg;
		double mpRegenMultiplier = creature.isRaid() ? Config.RAID_MP_REGEN_MULTIPLIER : Config.MP_REGEN_MULTIPLIER;
		double mpRegenBonus = 0;
		
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			
			// Calculate correct baseMpReg value for certain level of PC
			init += 0.3 * ((player.getLevel() - 1) / 10.0);
			
			// SevenSigns Festival modifier
			if (SevenSignsFestival.getInstance().isFestivalInProgress() && player.isFestivalParticipant())
			{
				mpRegenMultiplier *= calcFestivalRegenModifier(player);
			}
			
			// Mother Tree effect is calculated at last
			if (player.isInsideZone(ZoneId.MOTHERTREE))
			{
				mpRegenBonus += 1;
			}
			
			if (player.isInsideZone(ZoneId.CLAN_HALL) && (player.getClan() != null))
			{
				final int clanHallIndex = player.getClan().getHasHideout();
				if (clanHallIndex > 0)
				{
					final ClanHall clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex);
					if (clansHall != null)
					{
						if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
						{
							mpRegenMultiplier *= 1 + (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).getLvl() / 100);
						}
					}
				}
			}
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				mpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				mpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				mpRegenMultiplier *= 0.7; // Running
			}
			
			// Add MEN bonus
			init *= creature.getLevelMod() * BaseStats.MEN.calcBonus(creature);
		}
		
		if (init < 1)
		{
			init = 1;
		}
		
		return (creature.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier) + mpRegenBonus;
	}
	
	/**
	 * Calculate the CP regen rate (base + modifiers).<BR>
	 * <BR>
	 * @param creature
	 * @return
	 */
	public static final double calcCpRegen(Creature creature)
	{
		double init = creature.getTemplate().baseHpReg;
		double cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER;
		final double cpRegenBonus = 0;
		
		if (creature instanceof PlayerInstance)
		{
			final PlayerInstance player = (PlayerInstance) creature;
			
			// Calculate correct baseHpReg value for certain level of PC
			init += player.getLevel() > 10 ? (player.getLevel() - 1) / 10.0 : 0.5;
			
			// Calculate Movement bonus
			if (player.isSitting())
			{
				cpRegenMultiplier *= 1.5; // Sitting
			}
			else if (!player.isMoving())
			{
				cpRegenMultiplier *= 1.1; // Staying
			}
			else if (player.isRunning())
			{
				cpRegenMultiplier *= 0.7; // Running
			}
		}
		else // Calculate Movement bonus
		if (!creature.isMoving())
		{
			cpRegenMultiplier *= 1.1; // Staying
		}
		else if (creature.isRunning())
		{
			cpRegenMultiplier *= 0.7; // Running
		}
		
		// Apply CON bonus
		init *= creature.getLevelMod() * BaseStats.CON.calcBonus(creature);
		if (init < 1)
		{
			init = 1;
		}
		
		return (creature.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier) + cpRegenBonus;
	}
	
	@SuppressWarnings("deprecation")
	public static final double calcFestivalRegenModifier(PlayerInstance player)
	{
		final int[] festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(player);
		final int oracle = festivalInfo[0];
		final int festivalId = festivalInfo[1];
		int[] festivalCenter;
		
		// If the player isn't found in the festival, leave the regen rate as it is.
		if (festivalId < 0)
		{
			return 0;
		}
		
		// Retrieve the X and Y coords for the center of the festival arena the player is in.
		if (oracle == SevenSigns.CABAL_DAWN)
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId];
		}
		else
		{
			festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId];
		}
		
		// Check the distance between the player and the player spawn point, in the center of the arena.
		final double distToCenter = player.getDistance(festivalCenter[0], festivalCenter[1]);
		return 1.0 - (distToCenter * 0.0005); // Maximum Decreased Regen of ~ -65%;
	}
	
	public static final double calcSiegeRegenModifer(PlayerInstance player)
	{
		if ((player == null) || (player.getClan() == null))
		{
			return 0;
		}
		
		final Siege siege = SiegeManager.getInstance().getSiege(player.getPosition().getX(), player.getPosition().getY(), player.getPosition().getZ());
		if ((siege == null) || !siege.getIsInProgress())
		{
			return 0;
		}
		
		final SiegeClan siegeClan = siege.getAttackerClan(player.getClan().getClanId());
		if ((siegeClan == null) || (siegeClan.getFlag().size() == 0) || !Util.checkIfInRange(200, player, siegeClan.getFlag().get(0), true))
		{
			return 0;
		}
		
		return 1.5; // If all is true, then modifer will be 50% more
	}
	
	/**
	 * Calculate blow damage based on cAtk
	 * @param attacker
	 * @param target
	 * @param skill
	 * @param shld
	 * @param crit
	 * @param ss
	 * @return
	 */
	public static double calcBlowDamage(Creature attacker, Creature target, Skill skill, boolean shld, boolean crit, boolean ss)
	{
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		
		if (ss)
		{
			damage *= 2.;
		}
		
		if (shld)
		{
			defence += target.getShldDef();
		}
		
		if (crit)
		{
			// double cAtkMultiplied = (damage) + attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
			final double improvedDamageByCriticalVuln = target.calcStat(Stats.CRIT_VULN, damage, target, skill);
			final double improvedDamageByCriticalVulnAndAdd = (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, improvedDamageByCriticalVuln, target, skill));
			
			damage = improvedDamageByCriticalVulnAndAdd;
			
			final Effect vicious = attacker.getFirstEffect(312);
			if ((vicious != null) && (damage > 1))
			{
				for (Func func : vicious.getStatFuncs())
				{
					final Env env = new Env();
					env.player = attacker;
					env.target = target;
					env.skill = skill;
					env.value = damage;
					func.calc(env);
					damage = env.value;
				}
			}
		}
		
		// skill add is not influenced by criticals improvements, so it's applied later
		double skillpower = skill.getPower(attacker);
		final float ssboost = skill.getSSBoost();
		if (ssboost <= 0)
		{
			damage += skillpower;
		}
		else if (ssboost > 0)
		{
			if (ss)
			{
				skillpower *= ssboost;
				damage += skillpower;
			}
			else
			{
				damage += skillpower;
			}
		}
		
		// possible skill power critical hit, based on Official Description:
		if (calcCrit(skill.getBaseCritRate() * 10 * BaseStats.DEX.calcBonus(attacker)))
		{
			damage *= 2;
		}
		
		damage *= 70. / defence;
		
		// finally, apply the critical multiplier if present (it's not subjected to defense)
		if (crit)
		{
			damage = attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
		}
		
		// get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
		damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null);
		// get the natural vulnerability for the template
		if (target instanceof NpcInstance)
		{
			damage *= ((NpcInstance) target).getTemplate().getVulnerability(Stats.DAGGER_WPN_VULN);
		}
		
		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();
		
		// After C4 nobles make 4% more dmg in PvP.
		if ((attacker instanceof PlayerInstance) && ((PlayerInstance) attacker).isNoble() && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			damage *= 1.04;
		}
		
		// Must be removed, after armor resistances are checked.
		// These values are a quick fix to balance dagger gameplay and give armor resistances vs dagger. daggerWpnRes could also be used if a skill was given to all classes. The values here try to be a compromise. They were originally added in a late C4 rev (2289).
		if (target instanceof PlayerInstance)
		{
			final Armor armor = ((PlayerInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((PlayerInstance) target).isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				if (((PlayerInstance) target).isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				if (((PlayerInstance) target).isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && (attacker instanceof PlayerInstance) && (target instanceof PlayerInstance))
		{
			if (((PlayerInstance) attacker).isInOlympiadMode() && ((PlayerInstance) target).isInOlympiadMode())
			{
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
				}
			}
			else
			{
				damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
			}
		}
		
		return damage < 1 ? 1. : damage;
	}
	
	/**
	 * Calculated damage caused by ATTACK of attacker on target, called separatly for each weapon, if dual-weapon is used.
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param skill
	 * @param shld
	 * @param crit if the ATTACK have critical success
	 * @param dual if dual weapon is used
	 * @param ss if weapon item was charged by soulshot
	 * @return damage points
	 */
	public static final double calcPhysDam(Creature attacker, Creature target, Skill skill, boolean shld, boolean crit, boolean dual, boolean ss)
	{
		if (attacker instanceof PlayerInstance)
		{
			final PlayerInstance pcInst = (PlayerInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
			{
				return 0;
			}
		}
		
		double damage = attacker.getPAtk(target);
		double defence = target.getPDef(attacker);
		if (ss)
		{
			damage *= 2;
		}
		
		if (skill != null)
		{
			double skillpower = skill.getPower(attacker);
			final float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
			{
				damage += skillpower;
			}
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
			}
		}
		
		// In C5 summons make 10 % less dmg in PvP.
		if ((attacker instanceof Summon) && (target instanceof PlayerInstance))
		{
			damage *= 0.9;
		}
		
		// After C4 nobles make 4% more dmg in PvP.
		if ((attacker instanceof PlayerInstance) && ((PlayerInstance) attacker).isNoble() && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			damage *= 1.04;
		}
		
		// defence modifier depending of the attacker weapon
		final Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
				{
					stat = Stats.BOW_WPN_VULN;
					break;
				}
				case BLUNT:
				{
					stat = Stats.BLUNT_WPN_VULN;
					break;
				}
				case DAGGER:
				{
					stat = Stats.DAGGER_WPN_VULN;
					break;
				}
				case DUAL:
				{
					stat = Stats.DUAL_WPN_VULN;
					break;
				}
				case DUALFIST:
				{
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				}
				case ETC:
				{
					stat = Stats.ETC_WPN_VULN;
					break;
				}
				case FIST:
				{
					stat = Stats.FIST_WPN_VULN;
					break;
				}
				case POLE:
				{
					stat = Stats.POLE_WPN_VULN;
					break;
				}
				case SWORD:
				{
					stat = Stats.SWORD_WPN_VULN;
					break;
				}
				case BIGSWORD:
				{
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				}
				case BIGBLUNT:
				{
					stat = Stats.BIGBLUNT_WPN_VULN;
					break;
				}
			}
		}
		
		if (crit)
		{
			// Finally retail like formula
			final double cAtkMultiplied = damage + attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
			final double cAtkVuln = target.calcStat(Stats.CRIT_VULN, 1, target, null);
			final double improvedDamageByCriticalMulAndVuln = cAtkMultiplied * cAtkVuln;
			final double improvedDamageByCriticalMulAndAdd = improvedDamageByCriticalMulAndVuln + attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0, target, skill);
			damage = improvedDamageByCriticalMulAndAdd;
		}
		
		if (shld && !Config.ALT_GAME_SHIELD_BLOCKS)
		{
			defence += target.getShldDef();
		}
		
		damage = (70 * damage) / defence;
		
		if (stat != null)
		{
			// get the vulnerability due to skills (buffs, passives, toggles, etc)
			damage = target.calcStat(stat, damage, target, null);
			if (target instanceof NpcInstance)
			{
				// get the natural vulnerability for the template
				damage *= ((NpcInstance) target).getTemplate().getVulnerability(stat);
			}
		}
		
		damage += (Rnd.nextDouble() * damage) / 10;
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}
		
		if ((target instanceof PlayerInstance) && (weapon != null) && (weapon.getItemType() == WeaponType.DAGGER) && (skill != null))
		{
			final Armor armor = ((PlayerInstance) target).getActiveChestArmorItem();
			if (armor != null)
			{
				if (((PlayerInstance) target).isWearingHeavyArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_HEAVY;
				}
				if (((PlayerInstance) target).isWearingLightArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_LIGHT;
				}
				if (((PlayerInstance) target).isWearingMagicArmor())
				{
					damage /= Config.ALT_DAGGER_DMG_VS_ROBE;
				}
			}
		}
		
		if (attacker instanceof NpcInstance)
		{
			// Skill Race : Undead
			if (((NpcInstance) attacker).getTemplate().getRace() == NpcTemplate.Race.UNDEAD)
			{
				damage /= attacker.getPDefUndead(target);
			}
			
			if (((NpcInstance) attacker).getTemplate().getRace() == NpcTemplate.Race.PLANT)
			{
				damage /= attacker.getPDefPlants(target);
			}
			
			if (((NpcInstance) attacker).getTemplate().getRace() == NpcTemplate.Race.BUG)
			{
				damage /= attacker.getPDefInsects(target);
			}
			
			if (((NpcInstance) attacker).getTemplate().getRace() == NpcTemplate.Race.ANIMAL)
			{
				damage /= attacker.getPDefAnimals(target);
			}
			
			if (((NpcInstance) attacker).getTemplate().getRace() == NpcTemplate.Race.BEAST)
			{
				damage /= attacker.getPDefMonsters(target);
			}
			
			if (((NpcInstance) attacker).getTemplate().getRace() == NpcTemplate.Race.DRAGON)
			{
				damage /= attacker.getPDefDragons(target);
			}
		}
		
		if (target instanceof NpcInstance)
		{
			switch (((NpcInstance) target).getTemplate().getRace())
			{
				case UNDEAD:
				{
					damage *= attacker.getPAtkUndead(target);
					break;
				}
				case BEAST:
				{
					damage *= attacker.getPAtkMonsters(target);
					break;
				}
				case ANIMAL:
				{
					damage *= attacker.getPAtkAnimals(target);
					break;
				}
				case PLANT:
				{
					damage *= attacker.getPAtkPlants(target);
					break;
				}
				case DRAGON:
				{
					damage *= attacker.getPAtkDragons(target);
					break;
				}
				case ANGEL:
				{
					damage *= attacker.getPAtkAngels(target);
					break;
				}
				case BUG:
				{
					damage *= attacker.getPAtkInsects(target);
					break;
				}
				default:
				{
					// nothing
					break;
				}
			}
		}
		
		if (shld)
		{
			if ((100 - Config.ALT_PERFECT_SHLD_BLOCK) < Rnd.get(100))
			{
				damage = 1;
				target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
			}
		}
		
		if ((damage > 0) && (damage < 1))
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		// Dmg bonusses in PvP fight
		if (((attacker instanceof PlayerInstance) || (attacker instanceof Summon)) && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			if (skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (attacker instanceof PlayerInstance)
		{
			if (((PlayerInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_PHYSICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_PHYSICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker instanceof Summon)
		{
			damage = damage * Config.ALT_PETS_PHYSICAL_DAMAGE_MULTI;
		}
		else if (attacker instanceof NpcInstance)
		{
			damage = damage * Config.ALT_NPC_PHYSICAL_DAMAGE_MULTI;
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && (attacker instanceof PlayerInstance) && (target instanceof PlayerInstance))
		{
			if (((PlayerInstance) attacker).isInOlympiadMode() && ((PlayerInstance) target).isInOlympiadMode())
			{
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
				}
			}
			else
			{
				damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
			}
		}
		
		return damage;
	}
	
	public static final double calcMagicDam(Creature attacker, Creature target, Skill skill, boolean ss, boolean bss, boolean mcrit)
	{
		// Add Matk/Mdef Bonus
		int ssModifier = 1;
		// Add Bonus for Sps/SS
		if ((attacker instanceof Summon) && !(attacker instanceof PetInstance))
		{
			if (bss)
			{
				ssModifier = 4;
			}
			else if (ss)
			{
				ssModifier = 2;
			}
		}
		else
		{
			final ItemInstance weapon = attacker.getActiveWeaponInstance();
			if (weapon != null)
			{
				if (bss)
				{
					ssModifier = 4;
				}
				else if (ss)
				{
					ssModifier = 2;
				}
			}
		}
		
		if (attacker instanceof PlayerInstance)
		{
			final PlayerInstance pcInst = (PlayerInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
			{
				return 0;
			}
		}
		
		double mAtk = attacker.getMAtk(target, skill);
		final double mDef = target.getMDef(attacker, skill);
		
		// apply ss bonus
		mAtk *= ssModifier;
		
		double damage = ((91 * Math.sqrt(mAtk)) / mDef) * skill.getPower(attacker) * calcSkillVulnerability(target, skill);
		
		// In C5 summons make 10 % less dmg in PvP.
		if ((attacker instanceof Summon) && (target instanceof PlayerInstance))
		{
			damage *= 0.9;
		}
		
		// After C4 nobles make 4% more dmg in PvP.
		if ((attacker instanceof PlayerInstance) && ((PlayerInstance) attacker).isNoble() && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			damage *= 1.04;
		}
		
		// Failure calculation
		if (Config.ALT_GAME_MAGICFAILURES && !calcMagicSuccess(attacker, target, skill))
		{
			if (attacker instanceof PlayerInstance)
			{
				if (calcMagicSuccess(attacker, target, skill) && ((target.getLevel() - attacker.getLevel()) <= 9))
				{
					if (skill.getSkillType() == SkillType.DRAIN)
					{
						attacker.sendPacket(new SystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL));
					}
					else
					{
						attacker.sendPacket(new SystemMessage(SystemMessageId.ATTACK_FAILED));
					}
					
					damage /= 2;
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WAS_UNAFFECTED_BY_S2);
					sm.addString(target.getName());
					sm.addSkillName(skill.getId());
					attacker.sendPacket(sm);
					
					damage = 1;
				}
			}
			
			if (target instanceof PlayerInstance)
			{
				if (skill.getSkillType() == SkillType.DRAIN)
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_DRAIN);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(SystemMessageId.RESISTED_S1_MAGIC);
					sm.addString(attacker.getName());
					target.sendPacket(sm);
				}
			}
		}
		else if (mcrit)
		{
			// damage *= 4;
			damage *= Config.MAGIC_CRITICAL_POWER;
		}
		
		// Pvp bonusses for dmg
		if (((attacker instanceof PlayerInstance) || (attacker instanceof Summon)) && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			if (skill.isMagic())
			{
				damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (attacker instanceof PlayerInstance)
		{
			if (((PlayerInstance) attacker).getClassId().isMage())
			{
				damage = damage * Config.ALT_MAGES_MAGICAL_DAMAGE_MULTI;
			}
			else
			{
				damage = damage * Config.ALT_FIGHTERS_MAGICAL_DAMAGE_MULTI;
			}
		}
		else if (attacker instanceof Summon)
		{
			damage = damage * Config.ALT_PETS_MAGICAL_DAMAGE_MULTI;
		}
		else if (attacker instanceof NpcInstance)
		{
			damage = damage * Config.ALT_NPC_MAGICAL_DAMAGE_MULTI;
		}
		
		if (target instanceof Playable)
		{
			damage *= skill.getPvpMulti();
		}
		
		if (skill.getSkillType() == SkillType.DEATHLINK)
		{
			damage = damage * (1.0 - (attacker.getStatus().getCurrentHp() / attacker.getMaxHp())) * 2.0;
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && (attacker instanceof PlayerInstance) && (target instanceof PlayerInstance))
		{
			if (((PlayerInstance) attacker).isInOlympiadMode() && ((PlayerInstance) target).isInOlympiadMode())
			{
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
				}
			}
			else
			{
				damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
			}
		}
		
		return damage;
	}
	
	public static final double calcMagicDam(CubicInstance attacker, Creature target, Skill skill, boolean mcrit)
	{
		final double damage = calcMagicDam(attacker.getOwner(), target, skill, false, false, mcrit);
		return damage;
	}
	
	/**
	 * @param rate
	 * @return true in case of critical hit
	 */
	public static final boolean calcCrit(double rate)
	{
		return rate > Rnd.get(1000);
	}
	
	/**
	 * Calcul value of blow success
	 * @param creature
	 * @param target
	 * @param chance
	 * @return
	 */
	public boolean calcBlow(Creature creature, Creature target, int chance)
	{
		return creature.calcStat(Stats.BLOW_RATE, chance * (1.0 + ((creature.getDEX() - 20) / 100)), target, null) > Rnd.get(100);
	}
	
	/**
	 * Calcul value of lethal chance
	 * @param creature
	 * @param target
	 * @param baseLethal
	 * @return
	 */
	public static final double calcLethal(Creature creature, Creature target, int baseLethal)
	{
		double mult = 0.1 * target.calcStat(Stats.LETHAL_RATE, 100, target, null);
		mult *= baseLethal;
		return mult;
	}
	
	public static final boolean calcLethalHit(Creature creature, Creature target, Skill skill)
	{
		final int chance = Rnd.get(1000);
		
		if ((target.isRaid() && Config.ALLOW_RAID_LETHAL) || (!target.isRaid() && !(target instanceof DoorInstance) && (!Config.ALLOW_LETHAL_PROTECTION_MOBS || !(target instanceof NpcInstance) || !(Config.LIST_LETHAL_PROTECTED_MOBS.contains(((NpcInstance) target).getNpcId())))))
		{
			if ((!target.isRaid() || Config.ALLOW_RAID_LETHAL) && !(target instanceof DoorInstance) && (!(target instanceof NpcInstance) || (((NpcInstance) target).getNpcId() != 35062)) && (!Config.ALLOW_LETHAL_PROTECTION_MOBS || !(target instanceof NpcInstance) || !(Config.LIST_LETHAL_PROTECTED_MOBS.contains(((NpcInstance) target).getNpcId()))))
			{
				// 1nd lethal set CP to 1
				// 2nd lethal effect activate (cp,hp to 1 or if target is npc then hp to 1)
				if ((skill.getLethalChance2() > 0) && (chance < calcLethal(creature, target, skill.getLethalChance2())))
				{
					creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
					if (target instanceof NpcInstance)
					{
						target.reduceCurrentHp(target.getCurrentHp() - 1, creature);
					}
					else if (target instanceof PlayerInstance) // If is a active player set his HP and CP to 1
					{
						final PlayerInstance player = (PlayerInstance) target;
						if (!player.isInvul())
						{
							if ((!(creature instanceof PlayerInstance) || (!((PlayerInstance) creature).isGM() || ((PlayerInstance) creature).getAccessLevel().canGiveDamage())))
							{
								player.setCurrentHp(1);
								player.setCurrentCp(1);
								player.sendPacket(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL);
							}
						}
					}
				}
				else if ((skill.getLethalChance1() > 0) && (chance < calcLethal(creature, target, skill.getLethalChance1())))
				{
					if (target instanceof PlayerInstance)
					{
						final PlayerInstance player = (PlayerInstance) target;
						if (!player.isInvul())
						{
							if ((!(creature instanceof PlayerInstance) || (!((PlayerInstance) creature).isGM() || ((PlayerInstance) creature).getAccessLevel().canGiveDamage())))
							{
								player.setCurrentCp(1); // Set CP to 1
								player.sendPacket(SystemMessage.sendString("Combat points disappear when hit with a half kill skill"));
								creature.sendPacket(new SystemMessage(SystemMessageId.LETHAL_STRIKE));
							}
						}
					}
				}
				else
				{
					return false;
				}
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static final boolean calcMCrit(double mRate)
	{
		return mRate > Rnd.get(1000);
	}
	
	/**
	 * @param target
	 * @param dmg
	 * @return true in case when ATTACK is canceled due to hit
	 */
	public static final boolean calcAtkBreak(Creature target, double dmg)
	{
		if (target instanceof PlayerInstance)
		{
			if (((PlayerInstance) target).getForceBuff() != null)
			{
				return true;
			}
		}
		double init = 0;
		
		if (Config.ALT_GAME_CANCEL_CAST && target.isCastingNow())
		{
			init = 15;
		}
		
		if (Config.ALT_GAME_CANCEL_BOW && target.isAttackingNow())
		{
			final Weapon wpn = target.getActiveWeaponItem();
			if ((wpn != null) && (wpn.getItemType() == WeaponType.BOW))
			{
				init = 15;
			}
		}
		
		if (target.isRaid() || target.isInvul() || (init <= 0))
		{
			return false; // No attack break
		}
		
		// Chance of break is higher with higher dmg
		init += Math.sqrt(13 * dmg);
		
		// Chance is affected by target MEN
		init -= ((BaseStats.MEN.calcBonus(target) * 100) - 100);
		
		// Calculate all modifiers for ATTACK_CANCEL
		double rate = target.calcStat(Stats.ATTACK_CANCEL, init, null, null);
		
		// Adjust the rate to be between 1 and 99
		if (rate > 99)
		{
			rate = 99;
		}
		else if (rate < 1)
		{
			rate = 1;
		}
		
		return Rnd.get(100) < rate;
	}
	
	/**
	 * Calculate delay (in milliseconds) before next ATTACK
	 * @param attacker
	 * @param target
	 * @param rate
	 * @return
	 */
	public int calcPAtkSpd(Creature attacker, Creature target, double rate)
	{
		// measured Oct 2006 by Tank6585, formula by Sami
		// attack speed 312 equals 1500 ms delay... (or 300 + 40 ms delay?)
		if (rate < 2)
		{
			return 2700;
		}
		return (int) (470000 / rate);
	}
	
	/**
	 * Calculate delay (in milliseconds) for skills cast
	 * @param attacker
	 * @param target
	 * @param skill
	 * @param skillTime
	 * @return
	 */
	public int calcMAtkSpd(Creature attacker, Creature target, Skill skill, double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) ((skillTime * 333) / attacker.getMAtkSpd());
		}
		return (int) ((skillTime * 333) / attacker.getPAtkSpd());
	}
	
	/**
	 * Calculate delay (in milliseconds) for skills cast
	 * @param attacker
	 * @param skill
	 * @param skillTime
	 * @return
	 */
	public int calcMAtkSpd(Creature attacker, Skill skill, double skillTime)
	{
		if (skill.isMagic())
		{
			return (int) ((skillTime * 333) / attacker.getMAtkSpd());
		}
		return (int) ((skillTime * 333) / attacker.getPAtkSpd());
	}
	
	/**
	 * @param attacker
	 * @param target
	 * @return true if hit missed (taget evaded)
	 */
	public static boolean calcHitMiss(Creature attacker, Creature target)
	{
		int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)))) * 10;
		// Get additional bonus from the conditions when you are attacking
		chance *= hitConditionBonus.getConditionBonus(attacker, target);
		
		chance = Math.max(chance, 200);
		chance = Math.min(chance, 980);
		
		return chance < Rnd.get(1000);
	}
	
	/**
	 * @param attacker
	 * @param target
	 * @return true if shield defence successfull
	 */
	public static boolean calcShldUse(Creature attacker, Creature target)
	{
		final Weapon at_weapon = attacker.getActiveWeaponItem();
		// double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * DEXbonus[target.getDEX()];
		double shldRate = target.calcStat(Stats.SHIELD_RATE, 0, attacker, null) * BaseStats.DEX.calcBonus(target);
		if (shldRate == 0.0)
		{
			return false;
		}
		// Check for passive skill Aegis (316) or Aegis Stance (318)
		// Like L2OFF you can't parry if your target is behind you
		if ((target.getKnownSkill(316) == null) && (target.getFirstEffect(318) == null))
		{
			if (target.isBehind(attacker) || !target.isFront(attacker) || !attacker.isFront(target))
			{
				return false;
			}
		}
		// if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
		if ((at_weapon != null) && (at_weapon.getItemType() == WeaponType.BOW))
		{
			shldRate *= 1.3;
		}
		return shldRate > Rnd.get(100);
	}
	
	public boolean calcMagicAffected(Creature actor, Creature target, Skill skill)
	{
		// TODO: CHECK/FIX THIS FORMULA UP!!
		final SkillType type = skill.getSkillType();
		double defence = 0;
		if (skill.isActive() && skill.isOffensive())
		{
			defence = target.getMDef(actor, skill);
		}
		
		final double attack = 2 * actor.getMAtk(target, skill) * calcSkillVulnerability(target, skill);
		double d = (attack - defence) / (attack + defence);
		if (target.isRaid() && ((type == SkillType.CONFUSION) || (type == SkillType.MUTE) || (type == SkillType.PARALYZE) || (type == SkillType.ROOT) || (type == SkillType.FEAR) || (type == SkillType.SLEEP) || (type == SkillType.STUN) || (type == SkillType.DEBUFF) || (type == SkillType.AGGDEBUFF)))
		{
			if ((d > 0) && (Rnd.get(1000) == 1))
			{
				return true;
			}
			return false;
		}
		
		if ((target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0) && skill.is_Debuff())
		{
			return false;
		}
		
		d += 0.5 * Rnd.nextGaussian();
		return d > 0;
	}
	
	public static double calcSkillVulnerability(Creature target, Skill skill)
	{
		double multiplier = 1; // initialize...
		
		// Get the skill type to calculate its effect in function of base stats of the Creature target
		if (skill != null)
		{
			// first, get the natural template vulnerability values for the target
			final Stats stat = skill.getStat();
			if (stat != null)
			{
				switch (stat)
				{
					case AGGRESSION:
					{
						multiplier = target.getTemplate().baseAggressionVuln;
						break;
					}
					case BLEED:
					{
						multiplier = target.getTemplate().baseBleedVuln;
						break;
					}
					case POISON:
					{
						multiplier = target.getTemplate().basePoisonVuln;
						break;
					}
					case STUN:
					{
						multiplier = target.getTemplate().baseStunVuln;
						break;
					}
					case ROOT:
					{
						multiplier = target.getTemplate().baseRootVuln;
						break;
					}
					case MOVEMENT:
					{
						multiplier = target.getTemplate().baseMovementVuln;
						break;
					}
					case CONFUSION:
					{
						multiplier = target.getTemplate().baseConfusionVuln;
						break;
					}
					case SLEEP:
					{
						multiplier = target.getTemplate().baseSleepVuln;
						break;
					}
					case FIRE:
					{
						multiplier = target.getTemplate().baseFireVuln;
						break;
					}
					case WIND:
					{
						multiplier = target.getTemplate().baseWindVuln;
						break;
					}
					case WATER:
					{
						multiplier = target.getTemplate().baseWaterVuln;
						break;
					}
					case EARTH:
					{
						multiplier = target.getTemplate().baseEarthVuln;
						break;
					}
					case HOLY:
					{
						multiplier = target.getTemplate().baseHolyVuln;
						break;
					}
					case DARK:
					{
						multiplier = target.getTemplate().baseDarkVuln;
						break;
					}
					default:
					{
						multiplier = 1;
					}
				}
			}
			
			// Next, calculate the elemental vulnerabilities
			switch (skill.getElement())
			{
				case Skill.ELEMENT_EARTH:
				{
					multiplier = target.calcStat(Stats.EARTH_VULN, multiplier, target, skill);
					break;
				}
				case Skill.ELEMENT_FIRE:
				{
					multiplier = target.calcStat(Stats.FIRE_VULN, multiplier, target, skill);
					break;
				}
				case Skill.ELEMENT_WATER:
				{
					multiplier = target.calcStat(Stats.WATER_VULN, multiplier, target, skill);
					break;
				}
				case Skill.ELEMENT_WIND:
				{
					multiplier = target.calcStat(Stats.WIND_VULN, multiplier, target, skill);
					break;
				}
				case Skill.ELEMENT_HOLY:
				{
					multiplier = target.calcStat(Stats.HOLY_VULN, multiplier, target, skill);
					break;
				}
				case Skill.ELEMENT_DARK:
				{
					multiplier = target.calcStat(Stats.DARK_VULN, multiplier, target, skill);
					break;
				}
			}
			
			// Finally, calculate skilltype vulnerabilities
			SkillType type = skill.getSkillType();
			
			// For additional effects on PDAM and MDAM skills (like STUN, SHOCK, PARALYZE...)
			if ((type != null) && ((type == SkillType.PDAM) || (type == SkillType.MDAM)))
			{
				type = skill.getEffectType();
			}
			
			if (type != null)
			{
				switch (type)
				{
					case BLEED:
					{
						multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
						break;
					}
					case POISON:
					{
						multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
						break;
					}
					case STUN:
					{
						multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
						break;
					}
					case PARALYZE:
					{
						multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
						break;
					}
					case ROOT:
					{
						multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
						break;
					}
					case SLEEP:
					{
						multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
						break;
					}
					case MUTE:
					case FEAR:
					case BETRAY:
					case AGGREDUCE_CHAR:
					{
						multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
						break;
					}
					case CONFUSION:
					{
						multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
						break;
					}
					case DEBUFF:
					case WEAKNESS:
					{
						multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
						break;
					}
					case BUFF:
					{
						multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
						break;
					}
				}
			}
		}
		return multiplier;
	}
	
	public static double calcSkillStatModifier(Skill skill, Creature target)
	{
		final BaseStats saveVs = skill.getSavevs();
		if (saveVs == null)
		{
			return 1;
		}
		
		return 1 / saveVs.calcBonus(target);
	}
	
	public static boolean calcCubicSkillSuccess(CubicInstance attacker, Creature target, Skill skill)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if ((target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0) && skill.is_Debuff())
		{
			return false;
		}
		
		final SkillType type = skill.getSkillType();
		
		// these skills should not work on RaidBoss
		if (target.isRaid())
		{
			switch (type)
			{
				case CONFUSION:
				case ROOT:
				case STUN:
				case MUTE:
				case FEAR:
				case DEBUFF:
				case PARALYZE:
				case SLEEP:
				case AGGDEBUFF:
				{
					return false;
				}
			}
		}
		
		final int value = (int) skill.getPower();
		final double statModifier = calcSkillStatModifier(skill, target);
		int rate = (int) (value * statModifier);
		
		// Add Matk/Mdef Bonus
		double mAtkModifier = 0;
		if (skill.isMagic())
		{
			mAtkModifier = target.getMDef(attacker.getOwner(), skill);
			
			mAtkModifier = Math.pow(attacker.getMAtk() / mAtkModifier, 0.2);
			
			rate += (int) (mAtkModifier * 100) - 100;
		}
		
		// Resists
		final double vulnModifier = calcSkillVulnerability(target, skill);
		final double res = vulnModifier;
		double resMod = 1;
		if (res != 0)
		{
			if (res < 0)
			{
				resMod = 1 - (0.075 * res);
				resMod = 1 / resMod;
			}
			else
			{
				final double x_factor = 1.3;
				
				resMod = res * x_factor;
				if (resMod > 1)
				{
					resMod = res;
				}
			}
			
			if (resMod > 0.9)
			{
				resMod = 0.9;
			}
			else if (resMod < 0.5)
			{
				resMod = 0.5;
			}
			
			rate *= resMod;
		}
		
		// lvl modifier.
		final int deltamod = calcLvlDependModifier(attacker.getOwner(), target, skill);
		rate += deltamod;
		
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}
		
		return (Rnd.get(100) < rate);
	}
	
	public boolean calcSkillSuccess(Creature attacker, Creature target, Skill skill, boolean ss, boolean sps, boolean bss)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if ((target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0) && skill.is_Debuff())
		{
			return false;
		}
		
		// Add Matk/Mdef Bonus
		double mAtkModifier = 1;
		int ssModifier = 1;
		if (skill.isMagic())
		{
			mAtkModifier = target.getMDef(target, skill);
			
			if (bss)
			{
				ssModifier = 4;
			}
			else if (sps)
			{
				ssModifier = 2;
			}
			
			mAtkModifier = (14 * Math.sqrt(ssModifier * attacker.getMAtk(target, skill))) / mAtkModifier;
		}
		
		final SkillType type = skill.getSkillType();
		
		if (target.isRaid() && ((type == SkillType.CONFUSION) || (type == SkillType.MUTE) || (type == SkillType.PARALYZE) || (type == SkillType.ROOT) || (type == SkillType.FEAR) || (type == SkillType.SLEEP) || (type == SkillType.STUN) || (type == SkillType.DEBUFF) || (type == SkillType.AGGDEBUFF)))
		{
			return false; // these skills should not work on RaidBoss
		}
		
		if (target.isInvul() && ((type == SkillType.CONFUSION) || (type == SkillType.MUTE) || (type == SkillType.PARALYZE) || (type == SkillType.ROOT) || (type == SkillType.FEAR) || (type == SkillType.SLEEP) || (type == SkillType.STUN) || (type == SkillType.DEBUFF) || (type == SkillType.CANCEL) || (type == SkillType.NEGATE) || (type == SkillType.WARRIOR_BANE) || (type == SkillType.MAGE_BANE)))
		{
			return false; // these skills should not work on Invulable persons
		}
		
		final int value = (int) skill.getPower();
		final double statModifier = calcSkillStatModifier(skill, target);
		
		// Calculate BaseRate.
		int rate = (int) (value * statModifier);
		
		// matk modifier
		rate = (int) (rate * mAtkModifier);
		
		// Resists
		final double vulnModifier = calcSkillVulnerability(target, skill);
		final double res = vulnModifier;
		double resMod = 1;
		if (res != 0)
		{
			if (res < 0)
			{
				resMod = 1 - (0.075 * res);
				resMod = 1 / resMod;
			}
			else
			{
				final double x_factor = 1.3;
				
				resMod = res * x_factor;
				if (resMod > 1)
				{
					resMod = res;
				}
			}
			
			if (resMod > 0.9)
			{
				resMod = 0.9;
			}
			else if (resMod < 0.5)
			{
				resMod = 0.5;
			}
			
			rate *= resMod;
		}
		
		// lvl modifier.
		final int deltamod = calcLvlDependModifier(attacker, target, skill);
		rate += deltamod;
		
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}
		
		// physics configuration addons
		final float physics_mult = getChanceMultiplier(skill);
		rate *= physics_mult;
		
		if ((attacker instanceof PlayerInstance) && Config.SEND_SKILLS_CHANCE_TO_PLAYERS)
		{
			((PlayerInstance) attacker).sendMessage("Skill: " + skill.getName() + " Chance: " + rate + "%");
		}
		
		return Rnd.get(100) < rate;
	}
	
	public static boolean calcEffectSuccess(Creature attacker, Creature target, EffectTemplate effect, Skill skill, boolean ss, boolean sps, boolean bss)
	{
		if (attacker == null)
		{
			return false;
		}
		
		if ((target.calcStat(Stats.DEBUFF_IMMUNITY, 0, null, skill) > 0) && skill.is_Debuff())
		{
			return false;
		}
		
		final SkillType type = effect.effectType;
		final int value = (int) effect.effectPower;
		if (type == null)
		{
			return Rnd.get(100) < value;
		}
		else if (type.equals(SkillType.CANCEL))
		{
			return true;
		}
		
		final double statModifier = calcSkillStatModifier(skill, target);
		
		// Calculate BaseRate.
		int rate = (int) (value * statModifier);
		
		// Add Matk/Mdef Bonus
		double mAtkModifier = 0;
		int ssModifier = 0;
		if (skill.isMagic())
		{
			mAtkModifier = target.getMDef(target, skill);
			
			// Add Bonus for Sps/SS
			if (bss)
			{
				ssModifier = 4;
			}
			else if (sps)
			{
				ssModifier = 2;
			}
			else
			{
				ssModifier = 1;
			}
			
			mAtkModifier = (14 * Math.sqrt(ssModifier * attacker.getMAtk(target, skill))) / mAtkModifier;
			
			rate = (int) (rate * mAtkModifier);
		}
		
		// Resists
		final double vulnModifier = calcSkillTypeVulnerability(1, target, type);
		final double res = vulnModifier;
		double resMod = 1;
		if (res != 0)
		{
			if (res < 0)
			{
				resMod = 1 - (0.075 * res);
				resMod = 1 / resMod;
			}
			else
			{
				final double x_factor = 1.3;
				
				resMod = res * x_factor;
				if (resMod > 1)
				{
					resMod = res;
				}
			}
			
			if (resMod > 0.9)
			{
				resMod = 0.9;
			}
			else if (resMod < 0.5)
			{
				resMod = 0.5;
			}
			
			rate *= resMod;
		}
		
		// lvl modifier.
		final int deltamod = calcLvlDependModifier(attacker, target, skill);
		rate += deltamod;
		
		if (rate > skill.getMaxChance())
		{
			rate = skill.getMaxChance();
		}
		else if (rate < skill.getMinChance())
		{
			rate = skill.getMinChance();
		}
		
		// physics configuration addons
		final float physics_mult = getChanceMultiplier(skill);
		rate *= physics_mult;
		
		if ((attacker instanceof PlayerInstance) && Config.SEND_SKILLS_CHANCE_TO_PLAYERS)
		{
			((PlayerInstance) attacker).sendMessage("EffectType " + effect.effectType + " Chance: " + rate + "%");
		}
		
		return (Rnd.get(100) < rate);
	}
	
	public static double calcSkillTypeVulnerability(double multiplier, Creature target, SkillType type)
	{
		if (type != null)
		{
			switch (type)
			{
				case BLEED:
				{
					multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null);
					break;
				}
				case POISON:
				{
					multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null);
					break;
				}
				case STUN:
				{
					multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null);
					break;
				}
				case PARALYZE:
				{
					multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null);
					break;
				}
				case ROOT:
				{
					multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null);
					break;
				}
				case SLEEP:
				{
					multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null);
					break;
				}
				case MUTE:
				case FEAR:
				case BETRAY:
				case AGGDEBUFF:
				case ERASE:
				{
					multiplier = target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null);
					break;
				}
				case CONFUSION:
				case CONFUSE_MOB_ONLY:
				{
					multiplier = target.calcStat(Stats.CONFUSION_VULN, multiplier, target, null);
					break;
				}
				case DEBUFF:
				{
					multiplier = target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null);
					break;
				}
				case BUFF:
				{
					multiplier = target.calcStat(Stats.BUFF_VULN, multiplier, target, null);
					break;
				}
				case CANCEL:
				{
					multiplier = target.calcStat(Stats.CANCEL_VULN, multiplier, target, null);
					break;
				}
				default:
				{
				}
			}
		}
		
		return multiplier;
	}
	
	public static int calcLvlDependModifier(Creature attacker, Creature target, Skill skill)
	{
		if (attacker == null)
		{
			return 0;
		}
		
		if (skill.getLevelDepend() == 0)
		{
			return 0;
		}
		
		final int attackerMod;
		if (skill.getMagicLevel() > 0)
		{
			attackerMod = skill.getMagicLevel();
		}
		else
		{
			attackerMod = attacker.getLevel();
		}
		
		final int delta = attackerMod - target.getLevel();
		int deltamod = delta / 5;
		deltamod = deltamod * 5;
		if (deltamod != delta)
		{
			if (delta < 0)
			{
				deltamod -= 5;
			}
			else
			{
				deltamod += 5;
			}
		}
		
		return deltamod;
	}
	
	public static float getChanceMultiplier(Skill skill)
	{
		float multiplier = 1;
		
		if ((skill != null) && (skill.getSkillType() != null))
		{
			switch (skill.getSkillType())
			{
				case BLEED:
				{
					multiplier = Config.BLEED_CHANCE_MODIFIER;
					break;
				}
				case POISON:
				{
					multiplier = Config.POISON_CHANCE_MODIFIER;
					break;
				}
				case STUN:
				{
					multiplier = Config.STUN_CHANCE_MODIFIER;
					break;
				}
				case PARALYZE:
				{
					multiplier = Config.PARALYZE_CHANCE_MODIFIER;
					break;
				}
				case ROOT:
				{
					multiplier = Config.ROOT_CHANCE_MODIFIER;
					break;
				}
				case SLEEP:
				{
					multiplier = Config.SLEEP_CHANCE_MODIFIER;
					break;
				}
				case MUTE:
				case FEAR:
				case BETRAY:
				case AGGREDUCE_CHAR:
				{
					multiplier = Config.FEAR_CHANCE_MODIFIER;
					break;
				}
				case CONFUSION:
				{
					multiplier = Config.CONFUSION_CHANCE_MODIFIER;
					break;
				}
				case DEBUFF:
				case WEAKNESS:
				case WARRIOR_BANE:
				case MAGE_BANE:
				{
					multiplier = Config.DEBUFF_CHANCE_MODIFIER;
					break;
				}
				case BUFF:
				{
					multiplier = Config.BUFF_CHANCE_MODIFIER;
					break;
				}
			}
		}
		
		return multiplier;
	}
	
	public boolean calcBuffSuccess(Creature target, Skill skill)
	{
		final int rate = 100 * (int) calcSkillVulnerability(target, skill);
		return Rnd.get(100) < rate;
	}
	
	public static boolean calcMagicSuccess(Creature attacker, Creature target, Skill skill)
	{
		final double lvlDifference = target.getLevel() - (skill.getMagicLevel() > 0 ? skill.getMagicLevel() : attacker.getLevel());
		final int rate = Math.round((float) (Math.pow(1.3, lvlDifference) * 100));
		
		return Rnd.get(10000) > rate;
	}
	
	public boolean calculateUnlockChance(Skill skill)
	{
		final int level = skill.getLevel();
		int chance = 0;
		switch (level)
		{
			case 1:
			{
				chance = 30;
				break;
			}
			case 2:
			{
				chance = 50;
				break;
			}
			case 3:
			{
				chance = 75;
				break;
			}
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			{
				chance = 100;
				break;
			}
		}
		if (Rnd.get(120) > chance)
		{
			return false;
		}
		return true;
	}
	
	public double calcManaDam(Creature attacker, Creature target, Skill skill, boolean ss, boolean bss)
	{
		if ((attacker == null) || (target == null))
		{
			return 0;
		}
		
		// Mana Burnt = (SQR(M.Atk)*Power*(Target Max MP/97))/M.Def
		double mAtk = attacker.getMAtk(target, skill);
		final double mDef = target.getMDef(attacker, skill);
		final double mp = target.getMaxMp();
		
		int ssModifier = 1;
		// Add Bonus for Sps/SS
		if ((attacker instanceof Summon) && !(attacker instanceof PetInstance))
		{
			if (bss)
			{
				ssModifier = 4;
			}
			else if (ss)
			{
				ssModifier = 2;
			}
		}
		else
		{
			final ItemInstance weapon = attacker.getActiveWeaponInstance();
			if (weapon != null)
			{
				if (bss)
				{
					ssModifier = 4;
				}
				else if (ss)
				{
					ssModifier = 2;
				}
			}
		}
		
		mAtk *= ssModifier;
		
		double damage = (Math.sqrt(mAtk) * skill.getPower(attacker) * mp) / 97 / mDef;
		damage *= calcSkillVulnerability(target, skill);
		return damage;
	}
	
	public double calculateSkillResurrectRestorePercent(double baseRestorePercent, Creature caster)
	{
		double restorePercent = baseRestorePercent;
		
		// double modifier = WITbonus[casterWIT];
		final double modifier = BaseStats.WIT.calcBonus(caster);
		
		if ((restorePercent != 100) && (restorePercent != 0))
		{
			restorePercent = baseRestorePercent * modifier;
			
			if ((restorePercent - baseRestorePercent) > 20.0)
			{
				restorePercent = baseRestorePercent + 20.0;
			}
		}
		
		if (restorePercent > 100)
		{
			restorePercent = 100;
		}
		if (restorePercent < baseRestorePercent)
		{
			restorePercent = baseRestorePercent;
		}
		
		return restorePercent;
	}
	
	public static boolean calcPhysicalSkillEvasion(Creature target, Skill skill)
	{
		if (skill.isMagic() || (skill.getCastRange() > 40))
		{
			return false;
		}
		
		return Rnd.get(100) < target.calcStat(Stats.P_SKILL_EVASION, 0, null, skill);
	}
	
	public boolean calcSkillMastery(Creature actor)
	{
		if (actor == null)
		{
			return false;
		}
		
		double val = actor.getStat().calcStat(Stats.SKILL_MASTERY, 0, null, null);
		
		if (actor instanceof PlayerInstance)
		{
			if (((PlayerInstance) actor).isMageClass())
			{
				val *= BaseStats.INT.calcBonus(actor);
			}
			else
			{
				val *= BaseStats.STR.calcBonus(actor);
			}
		}
		
		return Rnd.get(100) < val;
	}
	
	/**
	 * Calculate damage caused by falling
	 * @param creature
	 * @param fallHeight
	 * @return damage
	 */
	public static double calcFallDam(Creature creature, int fallHeight)
	{
		if (!Config.FALL_DAMAGE || (fallHeight < 0))
		{
			return 0;
		}
		final double damage = creature.calcStat(Stats.FALL, (fallHeight * creature.getMaxHp()) / 1000, null, null);
		return damage;
	}
	
	/**
	 * Calculated damage caused by charges skills types. - THX aCis The special thing is about the multiplier (56 and not 70), and about the fixed amount of damages
	 * @param attacker player or NPC that makes ATTACK
	 * @param target player or NPC, target of ATTACK
	 * @param skill
	 * @param shld
	 * @param crit if the ATTACK have critical success
	 * @param ss if weapon item was charged by soulshot
	 * @param _numCharges
	 * @return damage points
	 */
	public static final double calcChargeSkillsDam(Creature attacker, Creature target, Skill skill, boolean shld, boolean crit, boolean ss, int _numCharges)
	{
		if (attacker instanceof PlayerInstance)
		{
			final PlayerInstance pcInst = (PlayerInstance) attacker;
			if (pcInst.isGM() && !pcInst.getAccessLevel().canGiveDamage())
			{
				return 0;
			}
		}
		
		final boolean isPvP = (attacker instanceof Playable) && (target instanceof Playable);
		double damage = attacker.getPAtk(target);
		final double defence = target.getPDef(attacker);
		
		if (ss)
		{
			damage *= 2;
		}
		
		if (crit)
		{
			final double improvedDamageByCriticalVuln = target.calcStat(Stats.CRIT_VULN, damage, target, skill);
			damage = (attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, improvedDamageByCriticalVuln, target, skill));
		}
		
		if (skill != null) // skill add is not influenced by criticals improvements, so it's applied later
		{
			double skillpower = skill.getPower(attacker);
			final float ssboost = skill.getSSBoost();
			if (ssboost <= 0)
			{
				damage += skillpower;
			}
			else if (ssboost > 0)
			{
				if (ss)
				{
					skillpower *= ssboost;
					damage += skillpower;
				}
				else
				{
					damage += skillpower;
				}
			}
			
			// Charges multiplier, just when skill is used
			if (_numCharges >= 1)
			{
				final double chargesModifier = 0.7 + (0.3 * _numCharges);
				damage *= chargesModifier;
			}
		}
		
		damage = (56 * damage) / defence;
		
		// finally, apply the critical multiplier if present (it's not subjected to defense)
		if (crit)
		{
			damage = attacker.calcStat(Stats.CRITICAL_DAMAGE, damage, target, skill);
		}
		
		// defence modifier depending of the attacker weapon
		final Weapon weapon = attacker.getActiveWeaponItem();
		Stats stat = null;
		if (weapon != null)
		{
			switch (weapon.getItemType())
			{
				case BOW:
				{
					stat = Stats.BOW_WPN_VULN;
					break;
				}
				case BLUNT:
				{
					stat = Stats.BLUNT_WPN_VULN;
					break;
				}
				case BIGSWORD:
				{
					stat = Stats.BIGSWORD_WPN_VULN;
					break;
				}
				case BIGBLUNT:
				{
					stat = Stats.BIGBLUNT_WPN_VULN;
					break;
				}
				case DAGGER:
				{
					stat = Stats.DAGGER_WPN_VULN;
					break;
				}
				case DUAL:
				{
					stat = Stats.DUAL_WPN_VULN;
					break;
				}
				case DUALFIST:
				{
					stat = Stats.DUALFIST_WPN_VULN;
					break;
				}
				case ETC:
				{
					stat = Stats.ETC_WPN_VULN;
					break;
				}
				case FIST:
				{
					stat = Stats.FIST_WPN_VULN;
					break;
				}
				case POLE:
				{
					stat = Stats.POLE_WPN_VULN;
					break;
				}
				case SWORD:
				{
					stat = Stats.SWORD_WPN_VULN;
					break;
				}
			}
		}
		
		if (stat != null)
		{
			damage = target.calcStat(stat, damage, target, null);
		}
		
		// Weapon random damage
		damage *= attacker.getRandomDamageMultiplier();
		
		// After C4 nobles make 4% more dmg in PvP.
		if ((attacker instanceof PlayerInstance) && ((PlayerInstance) attacker).isNoble() && ((target instanceof PlayerInstance) || (target instanceof Summon)))
		{
			damage *= 1.04;
		}
		
		if (shld && Config.ALT_GAME_SHIELD_BLOCKS)
		{
			damage -= target.getShldDef();
			if (damage < 0)
			{
				damage = 0;
			}
		}
		
		if (target instanceof NpcInstance)
		{
			double multiplier;
			switch (((NpcInstance) target).getTemplate().getRace())
			{
				case BEAST:
				{
					multiplier = 1 + ((attacker.getPAtkMonsters(target) - target.getPDefMonsters(target)) / 100);
					damage *= multiplier;
					break;
				}
				case ANIMAL:
				{
					multiplier = 1 + ((attacker.getPAtkAnimals(target) - target.getPDefAnimals(target)) / 100);
					damage *= multiplier;
					break;
				}
				case PLANT:
				{
					multiplier = 1 + ((attacker.getPAtkPlants(target) - target.getPDefPlants(target)) / 100);
					damage *= multiplier;
					break;
				}
				case DRAGON:
				{
					multiplier = 1 + ((attacker.getPAtkDragons(target) - target.getPDefDragons(target)) / 100);
					damage *= multiplier;
					break;
				}
				case ANGEL:
				{
					multiplier = 1 + ((attacker.getPAtkAngels(target) - target.getPDefAngels(target)) / 100);
					damage *= multiplier;
					break;
				}
				case BUG:
				{
					multiplier = 1 + ((attacker.getPAtkInsects(target) - target.getPDefInsects(target)) / 100);
					damage *= multiplier;
					break;
				}
				case GIANT:
				{
					multiplier = 1 + ((attacker.getPAtkGiants(target) - target.getPDefGiants(target)) / 100);
					damage *= multiplier;
					break;
				}
				case MAGICCREATURE:
				{
					multiplier = 1 + ((attacker.getPAtkMagicCreatures(target) - target.getPDefMagicCreatures(target)) / 100);
					damage *= multiplier;
					break;
				}
				default:
				{
					// nothing
					break;
				}
			}
		}
		
		if (shld)
		{
			if ((100 - Config.ALT_PERFECT_SHLD_BLOCK) < Rnd.get(100))
			{
				damage = 1;
				target.sendPacket(new SystemMessage(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS));
			}
		}
		
		if ((damage > 0) && (damage < 1))
		{
			damage = 1;
		}
		else if (damage < 0)
		{
			damage = 0;
		}
		
		// Dmg bonusses in PvP fight
		if (isPvP)
		{
			if (skill == null)
			{
				damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1, null, null);
			}
			else
			{
				damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1, null, null);
			}
		}
		
		if (Config.ENABLE_CLASS_DAMAGES && (attacker instanceof PlayerInstance) && (target instanceof PlayerInstance))
		{
			if (((PlayerInstance) attacker).isInOlympiadMode() && ((PlayerInstance) target).isInOlympiadMode())
			{
				if (Config.ENABLE_CLASS_DAMAGES_IN_OLY)
				{
					damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
				}
			}
			else
			{
				damage = damage * ClassDamageManager.getDamageMultiplier((PlayerInstance) attacker, (PlayerInstance) target);
			}
		}
		
		return damage;
	}
}
