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
package com.l2jmobius.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.GeoData;
import com.l2jmobius.gameserver.datatables.SkillTreeTable;
import com.l2jmobius.gameserver.model.actor.instance.L2ArtefactInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2ChestInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2DoorInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.BaseStats;
import com.l2jmobius.gameserver.skills.Env;
import com.l2jmobius.gameserver.skills.Formulas;
import com.l2jmobius.gameserver.skills.Stats;
import com.l2jmobius.gameserver.skills.conditions.Condition;
import com.l2jmobius.gameserver.skills.effects.EffectTemplate;
import com.l2jmobius.gameserver.skills.funcs.Func;
import com.l2jmobius.gameserver.skills.funcs.FuncTemplate;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillChargeDmg;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillCreateItem;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillDefault;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillDrain;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillEngrave;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillSeed;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillSiegeFlag;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillSummon;
import com.l2jmobius.gameserver.templates.L2WeaponType;
import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.gameserver.util.Util;

import javolution.text.TextBuilder;
import javolution.util.FastList;

/**
 * This class...
 * @version $Revision: 1.3.2.8.2.22 $ $Date: 2005/04/06 16:13:42 $
 */
public abstract class L2Skill
{
	protected static Logger _log = Logger.getLogger(L2Skill.class.getName());
	
	public static final int SKILL_CUBIC_MASTERY = 143;
	public static final int SKILL_LUCKY = 194;
	public static final int SKILL_CREATE_COMMON = 1320;
	public static final int SKILL_CREATE_DWARVEN = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	
	public static final int SKILL_FAKE_INT = 9001;
	public static final int SKILL_FAKE_WIT = 9002;
	public static final int SKILL_FAKE_MEN = 9003;
	public static final int SKILL_FAKE_CON = 9004;
	public static final int SKILL_FAKE_DEX = 9005;
	public static final int SKILL_FAKE_STR = 9006;
	
	public static enum SkillOpType
	{
		OP_PASSIVE,
		OP_ACTIVE,
		OP_TOGGLE
	}
	
	/** Target types of skills : SELF, PARTY, CLAN, PET... */
	public static enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_ALLY,
		TARGET_CLAN,
		TARGET_PET,
		TARGET_AREA,
		TARGET_AURA,
		TARGET_CORPSE,
		TARGET_UNDEAD,
		TARGET_AURA_UNDEAD,
		TARGET_FRONT_AREA,
		TARGET_CORPSE_ALLY,
		TARGET_CORPSE_CLAN,
		TARGET_CORPSE_PLAYER,
		TARGET_CORPSE_PET,
		TARGET_ITEM,
		TARGET_AREA_CORPSE_MOB,
		TARGET_CORPSE_MOB,
		TARGET_UNLOCKABLE,
		TARGET_HOLY,
		TARGET_PARTY_MEMBER,
		TARGET_OWNER_PET
	}
	
	public static enum SkillType
	{
		PDAM,
		MDAM,
		DOT,
		BLEED,
		POISON,
		HEAL,
		HOT,
		COMBATPOINTHEAL,
		CPHOT,
		MANAHEAL,
		MANARECHARGE,
		MPHOT,
		AGGDAMAGE,
		BUFF,
		DEBUFF,
		STUN,
		ROOT,
		RESURRECT,
		PASSIVE,
		CONT,
		CONFUSION,
		UNLOCK,
		CHARGE,
		FEAR,
		MHOT,
		DRAIN(L2SkillDrain.class),
		
		CANCEL,
		SLEEP,
		AGGREDUCE,
		AGGREMOVE,
		AGGREDUCE_CHAR,
		CHARGEDAM(L2SkillChargeDmg.class),
		CONFUSE_MOB_ONLY,
		DEATHLINK,
		DETECT_WEAKNESS,
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		FEED_PET,
		HEAL_PERCENT,
		LUCK,
		MANADAM,
		MDOT,
		MUTE,
		RECALL,
		REFLECT,
		SOULSHOT,
		SPIRITSHOT,
		SPOIL,
		SWEEP,
		SUMMON(L2SkillSummon.class),
		WEAKNESS,
		DEATHLINK_PET,
		MANA_BY_LEVEL,
		FAKE_DEATH,
		
		SIEGEFLAG(L2SkillSiegeFlag.class),
		TAKECASTLE(L2SkillEngrave.class),
		UNDEAD_DEFENSE,
		SEED(L2SkillSeed.class),
		PARALYZE,
		DRAIN_SOUL,
		COMMON_CRAFT,
		DWARVEN_CRAFT,
		ITEM_SA,
		FISHING,
		PUMPING,
		REELING,
		CREATE_ITEM(L2SkillCreateItem.class),
		AGGDEBUFF,
		STRSIEGEASSAULT,
		HEAL_STATIC,
		BALANCE_LIFE,
		BLOW,
		FATAL,
		CPDAMPERCENT,
		MAGE_BANE,
		WARRIOR_BANE,
		NEGATE,
		SOW,
		HARVEST,
		DELUXE_KEY_UNLOCK,
		BEAST_FEED,
		GET_PLAYER,
		
		// unimplemented
		NOTDONE;
		
		private final Class<? extends L2Skill> _class;
		
		public L2Skill makeSkill(StatsSet set)
		{
			try
			{
				final Constructor<? extends L2Skill> c = _class.getConstructor(StatsSet.class);
				
				return c.newInstance(set);
			}
			catch (final Exception e)
			{
				throw new RuntimeException(e);
			}
		}
		
		private SkillType()
		{
			_class = L2SkillDefault.class;
		}
		
		private SkillType(Class<? extends L2Skill> classType)
		{
			_class = classType;
		}
	}
	
	// elements
	public final static int ELEMENT_WIND = 1;
	public final static int ELEMENT_FIRE = 2;
	public final static int ELEMENT_WATER = 3;
	public final static int ELEMENT_EARTH = 4;
	public final static int ELEMENT_HOLY = 5;
	public final static int ELEMENT_DARK = 6;
	
	// save vs
	public final static int SAVEVS_INT = 1;
	public final static int SAVEVS_WIT = 2;
	public final static int SAVEVS_MEN = 3;
	public final static int SAVEVS_CON = 4;
	public final static int SAVEVS_DEX = 5;
	public final static int SAVEVS_STR = 6;
	
	// stat effected
	public final static int STAT_PATK = 301; // pAtk
	public final static int STAT_PDEF = 302; // pDef
	public final static int STAT_MATK = 303; // mAtk
	public final static int STAT_MDEF = 304; // mDef
	public final static int STAT_MAXHP = 305; // maxHp
	public final static int STAT_MAXMP = 306; // maxMp
	public final static int STAT_CURHP = 307;
	public final static int STAT_CURMP = 308;
	public final static int STAT_HPREGEN = 309; // regHp
	public final static int STAT_MPREGEN = 310; // regMp
	public final static int STAT_CASTINGSPEED = 311; // sCast
	public final static int STAT_ATKSPD = 312; // sAtk
	public final static int STAT_CRITDAM = 313; // critDmg
	public final static int STAT_CRITRATE = 314; // critRate
	public final static int STAT_FIRERES = 315; // fireRes
	public final static int STAT_WINDRES = 316; // windRes
	public final static int STAT_WATERRES = 317; // waterRes
	public final static int STAT_EARTHRES = 318; // earthRes
	public final static int STAT_HOLYRES = 336; // holyRes
	public final static int STAT_DARKRES = 337; // darkRes
	public final static int STAT_ROOTRES = 319; // rootRes
	public final static int STAT_SLEEPRES = 320; // sleepRes
	public final static int STAT_CONFUSIONRES = 321; // confusRes
	public final static int STAT_BREATH = 322; // breath
	public final static int STAT_AGGRESSION = 323; // aggr
	public final static int STAT_BLEED = 324; // bleed
	public final static int STAT_POISON = 325; // poison
	public final static int STAT_STUN = 326; // stun
	public final static int STAT_ROOT = 327; // root
	public final static int STAT_MOVEMENT = 328; // move
	public final static int STAT_EVASION = 329; // evas
	public final static int STAT_ACCURACY = 330; // accu
	public final static int STAT_COMBAT_STRENGTH = 331;
	public final static int STAT_COMBAT_WEAKNESS = 332;
	public final static int STAT_ATTACK_RANGE = 333; // rAtk
	public final static int STAT_NOAGG = 334; // noagg
	public final static int STAT_SHIELDDEF = 335; // sDef
	public final static int STAT_MP_CONSUME_RATE = 336; // Rate of mp consume per skill use
	public final static int STAT_HP_CONSUME_RATE = 337; // Rate of hp consume per skill use
	public final static int STAT_MCRITRATE = 338; // Magic Crit Rate
	
	// COMBAT DAMAGE MODIFIER SKILLS...DETECT WEAKNESS AND WEAKNESS/STRENGTH
	public final static int COMBAT_MOD_ANIMAL = 200;
	public final static int COMBAT_MOD_BEAST = 201;
	public final static int COMBAT_MOD_BUG = 202;
	public final static int COMBAT_MOD_DRAGON = 203;
	public final static int COMBAT_MOD_MONSTER = 204;
	public final static int COMBAT_MOD_PLANT = 205;
	public final static int COMBAT_MOD_HOLY = 206;
	public final static int COMBAT_MOD_UNHOLY = 207;
	public final static int COMBAT_MOD_BOW = 208;
	public final static int COMBAT_MOD_BLUNT = 209;
	public final static int COMBAT_MOD_DAGGER = 210;
	public final static int COMBAT_MOD_FIST = 211;
	public final static int COMBAT_MOD_DUAL = 212;
	public final static int COMBAT_MOD_SWORD = 213;
	public final static int COMBAT_MOD_POISON = 214;
	public final static int COMBAT_MOD_BLEED = 215;
	public final static int COMBAT_MOD_FIRE = 216;
	public final static int COMBAT_MOD_WATER = 217;
	public final static int COMBAT_MOD_EARTH = 218;
	public final static int COMBAT_MOD_WIND = 219;
	public final static int COMBAT_MOD_ROOT = 220;
	public final static int COMBAT_MOD_STUN = 221;
	public final static int COMBAT_MOD_CONFUSION = 222;
	public final static int COMBAT_MOD_DARK = 223;
	
	// conditional values
	public final static int COND_RUNNING = 0x0001;
	public final static int COND_WALKING = 0x0002;
	public final static int COND_SIT = 0x0004;
	public final static int COND_BEHIND = 0x0008;
	public final static int COND_CRIT = 0x0010;
	public final static int COND_LOWHP = 0x0020;
	public final static int COND_ROBES = 0x0040;
	public final static int COND_CHARGES = 0x0080;
	public final static int COND_SHIELD = 0x0100;
	public final static int COND_GRADEA = 0x010000;
	public final static int COND_GRADEB = 0x020000;
	public final static int COND_GRADEC = 0x040000;
	public final static int COND_GRADED = 0x080000;
	public final static int COND_GRADES = 0x100000;
	
	private static final Func[] _emptyFunctionSet = new Func[0];
	private static final L2Effect[] _emptyEffectSet = new L2Effect[0];
	
	private L2Character _affected;
	
	// these two build the primary key
	private final int _id;
	private final int _level;
	
	/** Identifier for a skill that client can't display */
	private int _displayId;
	
	// not needed, just for easier debug
	private final String _name;
	private final SkillOpType _operateType;
	private final boolean _magic;
	private final int _mpConsume;
	private final int _mpInitialConsume;
	private final int _hpConsume;
	private final int _itemConsume;
	private final int _itemConsumeId;
	// item consume count over time
	private final int _itemConsumeOT;
	// item consume id over time
	private final int _itemConsumeIdOT;
	// item consume time in milliseconds
	private final int _itemConsumeTime;
	// how many times to consume an item
	private final int _itemConsumeSteps;
	// for summon spells:
	// a) What is the total lifetime of summons (in millisecs)
	private final int _summonTotalLifeTime;
	// b) how much lifetime is lost per second of idleness (non-fighting)
	private final int _summonTimeLostIdle;
	// c) how much time is lost per second of activity (fighting)
	private final int _summonTimeLostActive;
	
	private final int _castRange;
	private final int _effectRange;
	
	private final int _maxCharges;
	private final int _feed;
	
	private final SkillType[] _negateStats;
	private final float _negatePower;
	private final int _maxNegatedEffects;
	
	// all times in milliseconds
	private final int _hitTime;
	private final int _coolTime;
	private final int _reuseDelay;
	private final int _buffDuration;
	
	/** Target type of the skill : SELF, PARTY, CLAN, PET... */
	private final SkillTargetType _targetType;
	
	private final double _power;
	private final int _magicLevel;
	private final int _levelDepend;
	
	// Effecting area of the skill, in radius.
	// The radius center varies according to the _targetType:
	// "caster" if targetType = AURA/PARTY/CLAN or "target" if targetType = AREA
	private final int _skillRadius;
	
	// radius of the skill that throws the character up
	private final int _flyRadius;
	
	private final SkillType _skillType;
	private final SkillType _effectType;
	private final int _effectPower;
	
	private final boolean _ispotion;
	private final int _element;
	private final int _savevs;
	
	private final boolean _isSuicideAttack;
	
	private final Stats _stat;
	private final BaseStats _saveVs;
	
	private final int _condition;
	private final int _conditionValue;
	private final boolean _overhit;
	private final int _weaponsAllowed;
	private final int _armorsAllowed;
	
	private final int _addCrossLearn; // -1 disable, otherwice SP price for others classes, default 1000
	private final float _mulCrossLearn; // multiplay for others classes, default 2
	private final float _mulCrossLearnRace; // multiplay for others races, default 2
	private final float _mulCrossLearnProf; // multiplay for fighter/mage missmatch, default 3
	private final List<ClassId> _canLearn; // which classes can learn
	private final List<Integer> _teachers; // which NPC teaches
	private final boolean _isOffensive;
	private final int _nextDanceCost;
	
	private final int _baseCritRate;
	
	protected Condition _preCondition;
	protected Condition _itemPreCondition;
	protected FuncTemplate[] _funcTemplates;
	protected EffectTemplate[] _effectTemplates;
	protected EffectTemplate[] _effectTemplatesSelf;
	
	protected L2Skill(StatsSet set)
	{
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		
		_displayId = set.getInteger("displayId", _id);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_magic = set.getBool("isMagic", false);
		_ispotion = set.getBool("isPotion", false);
		_mpConsume = set.getInteger("mpConsume", 0);
		_mpInitialConsume = set.getInteger("mpInitialConsume", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_itemConsume = set.getInteger("itemConsumeCount", 0);
		_itemConsumeId = set.getInteger("itemConsumeId", 0);
		_itemConsumeOT = set.getInteger("itemConsumeCountOT", 0);
		_itemConsumeIdOT = set.getInteger("itemConsumeIdOT", 0);
		_itemConsumeTime = set.getInteger("itemConsumeTime", 0);
		_itemConsumeSteps = set.getInteger("itemConsumeSteps", 0);
		_summonTotalLifeTime = set.getInteger("summonTotalLifeTime", 1200000); // 20 minutes default
		_summonTimeLostIdle = set.getInteger("summonTimeLostIdle", 0);
		_summonTimeLostActive = set.getInteger("summonTimeLostActive", 0);
		
		_castRange = set.getInteger("castRange", -1);
		_effectRange = set.getInteger("effectRange", -1);
		
		_maxCharges = set.getInteger("maxCharges", 0);
		
		_feed = set.getInteger("feed", 0);
		
		final String str = set.getString("negateStats", "");
		if (str.isEmpty())
		{
			_negateStats = new SkillType[0];
		}
		else
		{
			final String[] stats = str.split(" ");
			final SkillType[] array = new SkillType[stats.length];
			
			for (int i = 0; i < stats.length; i++)
			{
				SkillType type = null;
				try
				{
					type = Enum.valueOf(SkillType.class, stats[i]);
				}
				catch (final Exception e)
				{
					throw new IllegalArgumentException("SkillId: " + _id + "Enum value of type " + SkillType.class.getName() + "required, but found: " + stats[i]);
				}
				array[i] = type;
			}
			_negateStats = array;
		}
		
		_negatePower = set.getFloat("negatePower", 0.f);
		_maxNegatedEffects = set.getInteger("maxNegated", 0);
		
		_coolTime = set.getInteger("coolTime", 0);
		_hitTime = set.getInteger("hitTime", 0);
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_buffDuration = set.getInteger("buffDuration", 0);
		
		_skillRadius = set.getInteger("skillRadius", 80);
		
		_flyRadius = set.getInteger("flyRadius", 200);
		
		_targetType = set.getEnum("target", SkillTargetType.class);
		_power = set.getFloat("power", 0.f);
		_magicLevel = set.getInteger("magicLvl", SkillTreeTable.getInstance().getMinSkillLevel(_id, _level));
		_levelDepend = set.getInteger("lvlDepend", 0);
		_stat = set.getEnum("stat", Stats.class, null);
		
		_skillType = set.getEnum("skillType", SkillType.class);
		_effectType = set.getEnum("effectType", SkillType.class, null);
		_effectPower = set.getInteger("effectPower", 0);
		
		_element = set.getInteger("element", 0);
		_savevs = set.getInteger("save", 0);
		
		_saveVs = set.getEnum("saveVs", BaseStats.class, null);
		
		_condition = set.getInteger("condition", 0);
		_conditionValue = set.getInteger("conditionValue", 0);
		_overhit = set.getBool("overHit", false);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_armorsAllowed = set.getInteger("armorsAllowed", 0);
		
		_addCrossLearn = set.getInteger("addCrossLearn", 1000);
		_mulCrossLearn = set.getFloat("mulCrossLearn", 2.f);
		_mulCrossLearnRace = set.getFloat("mulCrossLearnRace", 2.f);
		_mulCrossLearnProf = set.getFloat("mulCrossLearnProf", 3.f);
		_isOffensive = set.getBool("offensive", isSkillTypeOffensive());
		_nextDanceCost = set.getInteger("nextDanceCost", 0);
		
		_baseCritRate = set.getInteger("baseCritRate", ((_skillType == SkillType.PDAM) || (_skillType == SkillType.BLOW)) ? 0 : -1);
		
		final String canLearn = set.getString("canLearn", null);
		if (canLearn == null)
		{
			_canLearn = null;
		}
		else
		{
			_canLearn = new FastList<>();
			final StringTokenizer st = new StringTokenizer(canLearn, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				final String cls = st.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				}
				catch (final Throwable t)
				{
					_log.log(Level.SEVERE, "Bad class " + cls + " to learn skill", t);
				}
			}
		}
		
		final String teachers = set.getString("teachers", null);
		if (teachers == null)
		{
			_teachers = null;
		}
		else
		{
			_teachers = new FastList<>();
			final StringTokenizer st = new StringTokenizer(teachers, " \r\n\t,;");
			while (st.hasMoreTokens())
			{
				final String npcid = st.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				}
				catch (final Throwable t)
				{
					_log.log(Level.SEVERE, "Bad teacher id " + npcid + " to teach skill", t);
				}
			}
		}
	}
	
	public abstract void useSkill(L2Character caster, L2Object[] targets);
	
	public final boolean isPotion()
	{
		return _ispotion;
	}
	
	public final int getArmorsAllowed()
	{
		return _armorsAllowed;
	}
	
	public final int getConditionValue()
	{
		return _conditionValue;
	}
	
	public final SkillType getSkillType()
	{
		return _skillType;
	}
	
	public final int getSavevs()
	{
		return _savevs;
	}
	
	public final int getElement()
	{
		return _element;
	}
	
	/**
	 * Return the target type of the skill : SELF, PARTY, CLAN, PET...<BR>
	 * <BR>
	 * @return
	 */
	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}
	
	/**
	 * Return skill saveVs base stat (STR, INT ...).<BR>
	 * <BR>
	 * @return
	 */
	public final BaseStats getSaveVs()
	{
		return _saveVs;
	}
	
	public final int getCondition()
	{
		return _condition;
	}
	
	public final boolean isOverhit()
	{
		return _overhit;
	}
	
	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}
	
	/**
	 * Return the power of the skill.<BR>
	 * <BR>
	 * @param activeChar
	 * @return
	 */
	public final double getPower(L2Character activeChar)
	{
		if (activeChar == null)
		{
			return _power;
		}
		
		if (_skillType == SkillType.DEATHLINK)
		{
			return _power * Math.pow(1.7165 - (activeChar.getCurrentHp() / activeChar.getMaxHp()), 2) * 0.577;
		}
		else if (_skillType == SkillType.FATAL)
		{
			return _power * 3.5 * (1 - (activeChar.getCurrentHp() / activeChar.getMaxHp()));
		}
		else
		{
			return _power;
		}
	}
	
	public final double getPower()
	{
		return _power;
	}
	
	public final int getMagicLevel()
	{
		return _magicLevel;
	}
	
	public final int getLevelDepend()
	{
		return _levelDepend;
	}
	
	/**
	 * Return the additional effect power or base probability.<BR>
	 * <BR>
	 * @return
	 */
	public final int getEffectPower()
	{
		return _effectPower;
	}
	
	/**
	 * Return the additional effect skill type (ex : STUN, PARALYZE,...).<BR>
	 * <BR>
	 * @return
	 */
	public final SkillType getEffectType()
	{
		return _effectType;
	}
	
	/**
	 * @return Returns the buffDuration.
	 */
	public final int getBuffDuration()
	{
		return _buffDuration;
	}
	
	/**
	 * @return Returns the castRange.
	 */
	public final int getCastRange()
	{
		return _castRange;
	}
	
	/**
	 * @return Returns the effectRange.
	 */
	public final int getEffectRange()
	{
		
		return _effectRange;
	}
	
	/**
	 * @return Returns the maxCharges.
	 */
	public final int getMaxCharges()
	{
		
		return _maxCharges;
	}
	
	/**
	 * @return Returns the pet food
	 */
	public final int getFeed()
	{
		return _feed;
	}
	
	/**
	 * @return Returns the negateStats.
	 */
	public final SkillType[] getNegateStats()
	{
		return _negateStats;
	}
	
	/**
	 * @return Returns the negatePower.
	 */
	public final float getNegatePower()
	{
		return _negatePower;
	}
	
	/**
	 * @return Returns the maxNegatedEffects.
	 */
	public final int getMaxNegatedEffects()
	{
		return _maxNegatedEffects;
	}
	
	/**
	 * @return Returns the hitTime.
	 */
	public final int getHitTime()
	{
		return _hitTime;
	}
	
	/**
	 * @return Returns the coolTime.
	 */
	public final int getCoolTime()
	{
		return _coolTime;
	}
	
	/**
	 * @return Returns the hpConsume.
	 */
	public final int getHpConsume()
	{
		return _hpConsume;
	}
	
	/**
	 * @return Returns the id.
	 */
	public final int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public void setDisplayId(int id)
	{
		_displayId = id;
	}
	
	/**
	 * Return the skill type (ex : BLEED, SLEEP, WATER...).<BR>
	 * <BR>
	 * @return
	 */
	public final Stats getStat()
	{
		return _stat;
	}
	
	/**
	 * @return Returns the itemConsume.
	 */
	public final int getItemConsume()
	{
		return _itemConsume;
	}
	
	/**
	 * @return Returns the itemConsumeId.
	 */
	public final int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeOT()
	{
		return _itemConsumeOT;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getItemConsumeIdOT()
	{
		return _itemConsumeIdOT;
	}
	
	/**
	 * @return Returns the itemConsume time in milliseconds.
	 */
	public final int getItemConsumeTime()
	{
		return _itemConsumeTime;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getItemConsumeSteps()
	{
		return _itemConsumeSteps;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTotalLifeTime()
	{
		return _summonTotalLifeTime;
	}
	
	/**
	 * @return Returns the itemConsume count over time.
	 */
	public final int getTimeLostIdle()
	{
		return _summonTimeLostIdle;
	}
	
	/**
	 * @return Returns the itemConsumeId over time.
	 */
	public final int getTimeLostActive()
	{
		return _summonTimeLostActive;
	}
	
	/**
	 * @return Returns the level.
	 */
	public final int getLevel()
	{
		return _level;
	}
	
	/**
	 * @return Returns the magic.
	 */
	public final boolean isMagic()
	{
		return _magic;
	}
	
	/**
	 * @return Returns the mpConsume.
	 */
	public final int getMpConsume()
	{
		return _mpConsume;
	}
	
	/**
	 * @return Returns the mpInitialConsume.
	 */
	public final int getMpInitialConsume()
	{
		return _mpInitialConsume;
	}
	
	/**
	 * @return Returns the name.
	 */
	public final String getName()
	{
		return _name;
	}
	
	/**
	 * @return Returns the reuseDelay.
	 */
	public final int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public final int getSkillRadius()
	{
		return _skillRadius;
	}
	
	public final int getFlyRadius()
	{
		return _flyRadius;
	}
	
	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}
	
	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}
	
	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}
	
	public final boolean isDance()
	{
		return _nextDanceCost > 0;
	}
	
	public final int getNextDanceMpCost()
	{
		return _nextDanceCost;
	}
	
	public final int getBaseCritRate()
	{
		return _baseCritRate;
	}
	
	public final boolean useSoulShot()
	{
		switch (getSkillType())
		{
			case FATAL:
			case PDAM:
			case BLOW:
			case CHARGEDAM:
			case STRSIEGEASSAULT:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean useSpiritShot()
	{
		return isMagic();
	}
	
	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}
	
	public final int getCrossLearnAdd()
	{
		return _addCrossLearn;
	}
	
	public final float getCrossLearnMul()
	{
		return _mulCrossLearn;
	}
	
	public final float getCrossLearnRace()
	{
		return _mulCrossLearnRace;
	}
	
	public final float getCrossLearnProf()
	{
		return _mulCrossLearnProf;
	}
	
	public final boolean getCanLearn(ClassId cls)
	{
		return (_canLearn == null) || _canLearn.contains(cls);
	}
	
	public final boolean canTeachBy(int npcId)
	{
		return (_teachers == null) || _teachers.contains(npcId);
	}
	
	public boolean isHotSpringsDisease()
	{
		return (getId() == 4551) || (getId() == 4552) || (getId() == 4553) || (getId() == 4554);
	}
	
	public final boolean isPvpSkill()
	{
		switch (_skillType)
		{
			case DOT:
			case BLEED:
			case CONFUSION:
			case POISON:
			case DEBUFF:
			case AGGDAMAGE:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case FEAR:
			case SLEEP:
			case MDOT:
			case MUTE:
			case WEAKNESS:
			case PARALYZE:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case CANCEL:
			case MANADAM:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean hasAggro()
	{
		switch (_skillType)
		{
			case HEAL:
			case HEAL_PERCENT:
			case MANAHEAL:
			case BALANCE_LIFE:
			case BUFF:
			case HOT:
			case REFLECT:
				return true;
			default:
				return false;
		}
	}
	
	public final boolean isOffensive()
	{
		return _isOffensive;
	}
	
	public final boolean isSkillTypeOffensive()
	{
		switch (_skillType)
		{
			case PDAM:
			case FATAL:
			case CPDAMPERCENT:
			case BLOW:
			case MDAM:
			case DOT:
			case BLEED:
			case POISON:
			case AGGDAMAGE:
			case DEBUFF:
			case AGGDEBUFF:
			case STUN:
			case ROOT:
			case CONFUSION:
			case UNLOCK:
			case DELUXE_KEY_UNLOCK:
			case FEAR:
			case DRAIN:
			case SLEEP:
			case CHARGEDAM:
			case CONFUSE_MOB_ONLY:
			case DEATHLINK:
			case DETECT_WEAKNESS:
			case MANADAM:
			case MDOT:
			case MUTE:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case WEAKNESS:
			case MANA_BY_LEVEL:
			case SWEEP:
			case PARALYZE:
			case DRAIN_SOUL:
			case AGGREDUCE:
			case MAGE_BANE:
			case WARRIOR_BANE:
			case CANCEL:
			case AGGREMOVE:
			case AGGREDUCE_CHAR:
			case SOW:
			case HARVEST:
				return true;
			default:
				return false;
		}
	}
	
	// int weapons[] = {L2Weapon.WEAPON_TYPE_ETC, L2Weapon.WEAPON_TYPE_BOW,
	// L2Weapon.WEAPON_TYPE_POLE, L2Weapon.WEAPON_TYPE_DUALFIST,
	// L2Weapon.WEAPON_TYPE_DUAL, L2Weapon.WEAPON_TYPE_BLUNT,
	// L2Weapon.WEAPON_TYPE_SWORD, L2Weapon.WEAPON_TYPE_DAGGER};
	
	public final boolean getWeaponDependancy(L2Character activeChar)
	{
		final int weaponsAllowed = getWeaponsAllowed();
		// check to see if skill has a weapon dependency.
		if (weaponsAllowed == 0)
		{
			return true;
		}
		
		int mask = 0;
		
		if (activeChar.getActiveWeaponItem() != null)
		{
			mask |= activeChar.getActiveWeaponItem().getItemType().mask();
		}
		if (activeChar.getSecondaryWeaponItem() != null)
		{
			mask |= activeChar.getSecondaryWeaponItem().getItemType().mask();
		}
		
		if ((mask & weaponsAllowed) != 0)
		{
			return true;
		}
		
		final TextBuilder skillmsg = new TextBuilder();
		skillmsg.append(getName());
		skillmsg.append(" can only be used with weapons of type ");
		for (final L2WeaponType wt : L2WeaponType.values())
		{
			if ((wt.mask() & weaponsAllowed) != 0)
			{
				skillmsg.append(wt).append('/');
			}
		}
		skillmsg.setCharAt(skillmsg.length() - 1, '.');
		final SystemMessage message = new SystemMessage(SystemMessage.S1_S2);
		message.addString(skillmsg.toString());
		activeChar.sendPacket(message);
		
		return false;
	}
	
	public boolean checkCondition(L2Character activeChar, boolean itemOrWeapon)
	{
		if ((getCondition() & L2Skill.COND_SHIELD) != 0)
		{
			/*
			 * L2Armor armorPiece; L2ItemInstance dummy; dummy = activeChar.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND); armorPiece = (L2Armor) dummy.getItem();
			 */
			// TODO add checks for shield here.
		}
		
		Condition preCondition = _preCondition;
		if (itemOrWeapon)
		{
			preCondition = _itemPreCondition;
		}
		
		if (preCondition == null)
		{
			return true;
		}
		final Env env = new Env();
		env.player = activeChar;
		env.skill = this;
		if (!preCondition.test(env))
		{
			final String msg = preCondition.getMessage();
			
			if (msg != null)
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.S1_S2);
				sm.addString(msg);
				activeChar.sendPacket(sm);
			}
			return false;
		}
		return true;
	}
	
	/**
	 * Return all targets of the skill in a table in function a the skill type.<BR>
	 * <BR>
	 * <B><U> Values of skill type</U> :</B><BR>
	 * <BR>
	 * <li>ONE : The skill can only be used on the L2PcInstance targeted, or on the caster if it's a L2PcInstance and no L2PcInstance targeted</li>
	 * <li>SELF</li>
	 * <li>HOLY, UNDEAD</li>
	 * <li>PET</li>
	 * <li>AURA, AURA_CLOSE</li>
	 * <li>AREA</li>
	 * <li>FRONT_AREA</li>
	 * <li>PARTY, CLAN</li>
	 * <li>CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN</li>
	 * <li>UNLOCKABLE</li>
	 * <li>ITEM</li><BR>
	 * <BR>
	 * @param activeChar The L2Character who use the skill
	 * @param onlyFirst
	 * @return
	 */
	public final L2Object[] getTargetList(L2Character activeChar, boolean onlyFirst)
	{
		final List<L2Character> targetList = new FastList<>();
		
		// Get the target type of the skill
		// (ex : ONE, SELF, HOLY, PET, AURA, AURA_CLOSE, AREA, FRONT_AREA, PARTY, CLAN, CORPSE_PLAYER, CORPSE_MOB, CORPSE_CLAN, UNLOCKABLE, ITEM, UNDEAD)
		final SkillTargetType targetType = getTargetType();
		
		// Init to null the target of the skill
		L2Character target = null;
		
		// Get the L2Objcet targeted by the user of the skill at this moment
		final L2Object objTarget = activeChar.getTarget();
		
		// Get the type of the skill
		// (ex : PDAM, MDAM, DOT, BLEED, POISON, HEAL, HOT, MANAHEAL, MANARECHARGE, AGGDAMAGE, BUFF, DEBUFF, STUN, ROOT, RESURRECT, PASSIVE...)
		final SkillType skillType = getSkillType();
		
		// If the L2Object targeted is a L2Character, it becomes the L2Character target
		if (objTarget instanceof L2Character)
		{
			target = (L2Character) objTarget;
		}
		
		switch (targetType)
		{
			// The skill can only be used on the L2Character targeted, or on the caster itself
			case TARGET_ONE:
			{
				if (target == null)
				
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (target.isDead() || ((target == activeChar) && !((skillType == SkillType.BUFF) || (skillType == SkillType.HEAL) || (skillType == SkillType.HEAL_PERCENT) || (skillType == SkillType.HOT) || (skillType == SkillType.MANAHEAL) || (skillType == SkillType.MANARECHARGE) || (skillType == SkillType.SEED) || (skillType == SkillType.REFLECT) || (skillType == SkillType.COMBATPOINTHEAL)
					
					|| (skillType == SkillType.BALANCE_LIFE))))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					
					return null;
				}
				
				return new L2Character[]
				{
					target
				};
			}
			case TARGET_SELF:
			{
				return new L2Character[]
				{
					activeChar
				};
			}
			case TARGET_HOLY:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (!(target instanceof L2ArtefactInstance))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					return null;
				}
				
				return new L2Character[]
				{
					target
				};
			}
			case TARGET_PET:
			{
				target = activeChar.getPet();
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					return null;
				}
				
				return new L2Character[]
				{
					target
				};
			}
			case TARGET_OWNER_PET:
			{
				if (activeChar instanceof L2Summon)
				{
					target = ((L2Summon) activeChar).getOwner();
					if ((target != null) && !target.isDead())
					{
						return new L2Character[]
						{
							target
						};
					}
				}
				
				return null;
			}
			case TARGET_CORPSE_PET:
			{
				
				target = activeChar.getPet();
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (!target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					return null;
				}
				
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_AURA:
			{
				final int radius = getSkillRadius();
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				
				final L2PcInstance src = activeChar.getActingPlayer();
				
				// Go through the L2Character _knownList
				for (final L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
				{
					if ((obj instanceof L2Attackable) || (obj instanceof L2PlayableInstance))
					{
						// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
						if ((obj == activeChar) || (obj == src) || obj.isDead())
						{
							continue;
						}
						
						if ((activeChar instanceof L2Attackable) && (obj instanceof L2Attackable))
						{
							if (obj.isRaid())
							{
								continue;
							}
							
							if (((L2Attackable) activeChar).getFactionId() != null)
							{
								if (((L2Attackable) activeChar).getFactionId().equals(((L2Attackable) obj).getFactionId()))
								{
									continue;
								}
							}
						}
						
						if (src != null)
						{
							final L2PcInstance trg = obj.getActingPlayer();
							
							// check if both attacker and target are L2PcInstances and if they are in same party
							if (trg != null)
							{
								if (trg == src)
								{
									continue;
								}
								
								if (getId() == 286)
								{
									continue;
								}
								
								if (!src.checkPvpSkill(trg, this, activeChar instanceof L2Summon))
								{
									continue;
								}
								
								if ((src.getParty() != null) && (trg.getParty() != null))
								{
									if (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									{
										continue;
									}
									
									if ((src.getParty().getCommandChannel() != null) && (trg.getParty().getCommandChannel() != null))
									{
										if (src.getParty().getCommandChannel() == trg.getParty().getCommandChannel())
										{
											continue;
										}
									}
								}
								
								if (src.getEventTeam() > 0)
								{
									if (src.getEventTeam() == trg.getEventTeam())
									{
										continue;
									}
								}
								
								if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
								{
									if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
									{
										continue;
									}
									
									if ((src.getClanId() != 0) && (src.getClanId() == trg.getClanId()))
									{
										continue;
									}
								}
							}
						}
						
						if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
						{
							continue;
						}
						
						if (onlyFirst == false)
						{
							targetList.add(obj);
						}
						else
						{
							return new L2Character[]
							{
								obj
							};
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_AREA:
			case TARGET_FRONT_AREA:
			{
				
				if ((getCastRange() >= 0) && (target == null))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if ((!((target instanceof L2Attackable) || (target instanceof L2PlayableInstance))) || // Target is not L2Attackable or L2PlayableInstance
					((getCastRange() >= 0) && ((target == activeChar) || ((target != null) && target.isDead())))) // target is null or self or dead/faking
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
					return null;
				}
				
				L2Character cha;
				
				if (getCastRange() >= 0)
				{
					cha = target;
					
					if (!onlyFirst)
					{
						targetList.add(cha); // Add target to target list
					}
					else
					{
						return new L2Character[]
						{
							cha
						};
					}
				}
				else
				{
					cha = activeChar;
				}
				
				final boolean effectOriginIsL2PlayableInstance = (cha instanceof L2PlayableInstance);
				
				final L2PcInstance src = activeChar.getActingPlayer();
				
				final int radius = getSkillRadius();
				
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				
				for (final L2Character obj : activeChar.getKnownList().getKnownCharacters())
				{
					
					if (!((obj instanceof L2Attackable) || (obj instanceof L2PlayableInstance)))
					{
						continue;
					}
					
					if (obj == cha)
					{
						continue;
					}
					
					if (!obj.isAlikeDead() && (obj != activeChar))
					{
						if (!Util.checkIfInRange(radius, obj, cha, true))
						{
							continue;
						}
						
						if (targetType == SkillTargetType.TARGET_FRONT_AREA)
						{
							if ((target != null) && !target.isInFrontOf(activeChar))
							{
								continue;
							}
						}
						
						if ((activeChar instanceof L2Attackable) && (obj instanceof L2Attackable))
						{
							if (obj.isRaid())
							{
								continue;
							}
							
							if (((L2Attackable) activeChar).getFactionId() != null)
							{
								if (((L2Attackable) activeChar).getFactionId().equals(((L2Attackable) obj).getFactionId()))
								{
									continue;
								}
							}
						}
						
						if (src != null)
						{
							final L2PcInstance trg = obj.getActingPlayer();
							
							if (trg != null)
							{
								
								if (trg == src)
								{
									continue;
								}
								
								if ((src.getParty() != null) && (trg.getParty() != null))
								{
									if (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									{
										continue;
									}
									
									if ((src.getParty().getCommandChannel() != null) && (trg.getParty().getCommandChannel() != null))
									{
										if (src.getParty().getCommandChannel() == trg.getParty().getCommandChannel())
										{
											continue;
										}
									}
								}
								
								if (src.getEventTeam() > 0)
								{
									if (src.getEventTeam() == trg.getEventTeam())
									{
										continue;
									}
								}
								
								if (trg.isInsideZone(L2Character.ZONE_PEACE))
								{
									continue;
								}
								
								if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
								{
									if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
									{
										continue;
									}
									
									if ((src.getClan() != null) && (trg.getClan() != null))
									{
										if (src.getClan().getClanId() == trg.getClan().getClanId())
										{
											continue;
										}
									}
									
									if (!src.checkPvpSkill(obj, this, activeChar instanceof L2Summon))
									{
										continue;
									}
								}
							}
							
						}
						else
						
						{
							if (effectOriginIsL2PlayableInstance && // If effect starts at L2PlayableInstance and
								!(obj instanceof L2PlayableInstance))
							{
								continue;
							}
						}
						
						if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
						{
							continue;
						}
						
						targetList.add(obj);
					}
				}
				
				if (targetList.size() == 0)
				{
					return null;
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			
			case TARGET_PARTY:
			{
				if (onlyFirst)
				{
					return new L2Character[]
					{
						activeChar
					};
				}
				
				targetList.add(activeChar);
				
				L2PcInstance player = null;
				
				if (activeChar instanceof L2Summon)
				{
					player = ((L2Summon) activeChar).getOwner();
					targetList.add(player);
				}
				else if (activeChar instanceof L2PcInstance)
				{
					player = (L2PcInstance) activeChar;
					if ((activeChar.getPet() != null) && !activeChar.getPet().isDead())
					{
						targetList.add(activeChar.getPet());
					}
				}
				
				if (activeChar.getParty() != null)
				{
					// Get all visible objects in a spheric area near the L2Character
					// Get a list of Party Members
					final List<L2PcInstance> partyList = activeChar.getParty().getPartyMembers();
					for (final L2PcInstance partyMember : partyList)
					{
						if (partyMember == null)
						{
							continue;
						}
						
						if (partyMember == player)
						{
							continue;
						}
						
						if (!partyMember.isDead() && Util.checkIfInRange(getSkillRadius(), activeChar, partyMember, true))
						{
							targetList.add(partyMember);
							
							if ((partyMember.getPet() != null) && !partyMember.getPet().isDead())
							{
								targetList.add(partyMember.getPet());
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_PARTY_MEMBER:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (target.isDead())
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
					return null;
				}
				
				if ((target == activeChar) || ((activeChar.getParty() != null) && (target.getParty() != null) && (activeChar.getParty().getPartyLeaderOID() == target.getParty().getPartyLeaderOID())) || ((activeChar instanceof L2PcInstance) && (target instanceof L2Summon) && (activeChar.getPet() == target)) || ((activeChar instanceof L2Summon) && (target instanceof L2PcInstance) && (activeChar == target.getPet())))
				{
					return new L2Character[]
					{
						target
					};
				}
				activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
				return null;
			}
			case TARGET_CORPSE_ALLY:
			case TARGET_ALLY:
			{
				if (activeChar instanceof L2PcInstance)
				{
					final int radius = getSkillRadius();
					final L2PcInstance player = (L2PcInstance) activeChar;
					final L2Clan clan = player.getClan();
					
					if (player.isInOlympiadMode())
					{
						return new L2Character[]
						{
							player
						};
					}
					
					if (targetType != SkillTargetType.TARGET_CORPSE_ALLY)
					{
						if (onlyFirst == false)
						{
							targetList.add(player);
						}
						else
						{
							return new L2Character[]
							{
								player
							};
						}
					}
					
					if (activeChar.getPet() != null)
					{
						if (Util.checkIfInRange(radius, activeChar, activeChar.getPet(), true))
						{
							if ((targetType != SkillTargetType.TARGET_CORPSE_ALLY) && !(activeChar.getPet().isDead()))
							{
								targetList.add(activeChar.getPet());
							}
						}
					}
					
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (final L2Character newTarget : activeChar.getKnownList().getKnownCharactersInRadius(radius))
						{
							if (!(newTarget instanceof L2PcInstance))
							{
								continue;
							}
							
							if (((((L2PcInstance) newTarget).getAllyId() == 0) || (((L2PcInstance) newTarget).getAllyId() != player.getAllyId())) && ((((L2PcInstance) newTarget).getClan() == null) || (((L2PcInstance) newTarget).getClanId() != player.getClanId())))
							{
								continue;
							}
							
							if (((L2PcInstance) newTarget).getEventTeam() > 0)
							{
								if (player.getEventTeam() != ((L2PcInstance) newTarget).getEventTeam())
								{
									continue;
								}
							}
							
							if (((L2PcInstance) newTarget).getPet() != null)
							{
								if (Util.checkIfInRange(radius, activeChar, ((L2PcInstance) newTarget).getPet(), true))
								{
									if ((targetType != SkillTargetType.TARGET_CORPSE_ALLY) && !(((L2PcInstance) newTarget).getPet().isDead()) && player.checkPvpSkill(newTarget, this) && (onlyFirst == false))
									{
										targetList.add(((L2PcInstance) newTarget).getPet());
									}
								}
							}
							
							if (targetType == SkillTargetType.TARGET_CORPSE_ALLY)
							{
								
								if (!((L2PcInstance) newTarget).isDead())
								{
									continue;
								}
								
								if (getSkillType() == SkillType.RESURRECT)
								{
									if (((L2PcInstance) newTarget).isInsideZone(L2Character.ZONE_SIEGE))
									{
										continue;
									}
								}
							}
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}
							
							if (onlyFirst == false)
							{
								targetList.add(newTarget);
							}
							else
							{
								return new L2Character[]
								{
									newTarget
								};
							}
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_CLAN:
			case TARGET_CLAN:
			{
				if (activeChar instanceof L2PcInstance)
				{
					final L2PcInstance player = (L2PcInstance) activeChar;
					
					if (player.isInOlympiadMode())
					{
						return new L2Character[]
						{
							player
						};
					}
					
					if (targetType != SkillTargetType.TARGET_CORPSE_CLAN)
					{
						if (onlyFirst == false)
						{
							targetList.add(player);
						}
						else
						{
							return new L2Character[]
							{
								player
							};
						}
					}
					
					final int radius = getSkillRadius();
					final L2Clan clan = player.getClan();
					
					if (activeChar.getPet() != null)
					{
						if (Util.checkIfInRange(radius, activeChar, activeChar.getPet(), true))
						{
							if ((targetType != SkillTargetType.TARGET_CORPSE_CLAN) && !(activeChar.getPet().isDead()))
							{
								targetList.add(activeChar.getPet());
							}
						}
					}
					
					if (clan != null)
					{
						// Get all visible objects in a spheric area near the L2Character
						// Get Clan Members
						for (final L2ClanMember member : clan.getMembers())
						{
							final L2PcInstance newTarget = member.getPlayerInstance();
							if ((newTarget == null) || (newTarget == player))
							{
								continue;
							}
							
							if (newTarget.getEventTeam() > 0)
							{
								if (player.getEventTeam() != newTarget.getEventTeam())
								{
									continue;
								}
							}
							
							if (newTarget.getPet() != null)
							{
								if (Util.checkIfInRange(radius, activeChar, newTarget.getPet(), true))
								{
									if ((targetType != SkillTargetType.TARGET_CORPSE_CLAN) && !(newTarget.getPet().isDead()) && player.checkPvpSkill(newTarget, this) && (onlyFirst == false))
									{
										targetList.add(newTarget.getPet());
									}
								}
							}
							
							if (targetType == SkillTargetType.TARGET_CORPSE_CLAN)
							{
								if (!newTarget.isDead())
								{
									continue;
								}
								
								if (getSkillType() == SkillType.RESURRECT)
								{
									// check target is not in a active siege zone
									if (newTarget.isInsideZone(L2Character.ZONE_SIEGE))
									{
										continue;
									}
								}
							}
							
							if (!Util.checkIfInRange(radius, activeChar, newTarget, true))
							{
								continue;
							}
							
							// Don't add this target if this is a Pc->Pc pvp casting and pvp condition not met
							if (!player.checkPvpSkill(newTarget, this))
							{
								continue;
							}
							
							if (onlyFirst == false)
							{
								targetList.add(newTarget);
							}
							else
							{
								return new L2Character[]
								{
									newTarget
								};
							}
							
						}
					}
				}
				else if (activeChar instanceof L2NpcInstance)
				{
					// for buff purposes, returns one unbuffed friendly mob nearby or mob itself?
					final L2NpcInstance npc = (L2NpcInstance) activeChar;
					for (final L2Object newTarget : activeChar.getKnownList().getKnownObjects().values())
					{
						if ((newTarget instanceof L2NpcInstance) && (((L2NpcInstance) newTarget).getFactionId() == npc.getFactionId()))
						{
							if (!Util.checkIfInRange(getCastRange(), activeChar, newTarget, true))
							{
								continue;
							}
							
							if (((L2NpcInstance) newTarget).getFirstEffect(this) != null)
							{
								targetList.add((L2NpcInstance) newTarget);
								break;
							}
						}
					}
					
					if (targetList.isEmpty())
					{
						targetList.add(activeChar);
					}
				}
				
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_CORPSE_PLAYER:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (!target.isDead())
				
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
					return null;
				}
				
				if (getSkillType() == SkillType.RESURRECT)
				{
					L2Character dead = null;
					if (target instanceof L2PcInstance)
					{
						dead = target;
					}
					else if (target instanceof L2PetInstance)
					{
						dead = target;
					}
					
					if (dead != null)
					{
						if (dead.isInsideZone(L2Character.ZONE_SIEGE))
						{
							activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_BE_RESURRECTED_DURING_SIEGE));
							return null;
						}
						
						if (dead instanceof L2PcInstance)
						{
							if (((L2PcInstance) dead).isReviveRequested())
							{
								if (((L2PcInstance) dead).isRevivingPet())
								{
									activeChar.sendPacket(new SystemMessage(1511)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
								}
								else
								{
									activeChar.sendPacket(new SystemMessage(1513)); // Resurrection is already been proposed.
								}
								
								return null;
							}
						}
						
						if (dead instanceof L2PetInstance)
						{
							if (((L2PetInstance) dead).getOwner() != activeChar)
							{
								activeChar.sendMessage("You are not the owner of this pet.");
								return null;
							}
						}
					}
					else
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
						return null;
					}
				}
				
				if (onlyFirst == false)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_CORPSE_MOB:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (!(target instanceof L2Attackable) || !target.isDead())
				
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
					return null;
				}
				
				if (onlyFirst == false)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return new L2Character[]
				{
					target
				};
			}
			case TARGET_AREA_CORPSE_MOB:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if ((!(target instanceof L2Attackable)) || !target.isDead())
				
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
					return null;
				}
				
				if (onlyFirst == false)
				{
					targetList.add(target);
				}
				else
				{
					return new L2Character[]
					{
						target
					};
				}
				
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				
				final L2PcInstance src = activeChar.getActingPlayer();
				
				final int radius = getSkillRadius();
				
				for (final L2Character obj : activeChar.getKnownList().getKnownCharacters())
				{
					
					if (!((obj instanceof L2Attackable) || (obj instanceof L2PlayableInstance)) || obj.isDead() || (obj == activeChar))
					{
						continue;
					}
					
					if (!Util.checkIfInRange(radius, target, obj, true))
					{
						continue;
					}
					
					if (src != null)
					{
						final L2PcInstance trg = obj.getActingPlayer();
						if (trg != null)
						{
							
							if (trg == src)
							{
								continue;
							}
							
							if ((src.getParty() != null) && (trg.getParty() != null))
							{
								if (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
								{
									continue;
								}
								
								if ((src.getParty().getCommandChannel() != null) && (trg.getParty().getCommandChannel() != null))
								{
									if (src.getParty().getCommandChannel() == trg.getParty().getCommandChannel())
									{
										continue;
									}
								}
							}
							
							if (src.getEventTeam() > 0)
							{
								if (src.getEventTeam() == trg.getEventTeam())
								{
									continue;
								}
							}
							
							if (trg.isInsideZone(L2Character.ZONE_PEACE))
							{
								continue;
							}
							
							if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
							{
								if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
								{
									continue;
								}
								
								if ((src.getClan() != null) && (trg.getClan() != null))
								{
									if (src.getClan().getClanId() == trg.getClan().getClanId())
									{
										continue;
									}
								}
								
								if (!src.checkPvpSkill(obj, this, activeChar instanceof L2Summon))
								{
									continue;
								}
							}
						}
						
					}
					
					if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
					{
						continue;
					}
					
					targetList.add(obj);
				}
				
				if (targetList.size() == 0)
				{
					return null;
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			case TARGET_UNLOCKABLE:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if (!(target instanceof L2DoorInstance) && !(target instanceof L2ChestInstance))
				
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
					return null;
				}
				
				if (onlyFirst == false)
				{
					targetList.add(target);
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return new L2Character[]
				{
					target
				};
				
			}
			case TARGET_ITEM:
			{
				activeChar.sendMessage("Target type of skill is not currently handled.");
				return null;
			}
			case TARGET_UNDEAD:
			{
				if (target == null)
				{
					activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
					return null;
				}
				
				if ((target instanceof L2NpcInstance) || (target instanceof L2SummonInstance))
				{
					if (!target.isUndead() || target.isDead())
					
					{
						activeChar.sendPacket(new SystemMessage(SystemMessage.INCORRECT_TARGET));
						return null;
					}
					
					if (onlyFirst == false)
					{
						targetList.add(target);
					}
					else
					{
						return new L2Character[]
						{
							target
						};
					}
					
					return targetList.toArray(new L2Object[targetList.size()]);
				}
				return null;
				
			}
			case TARGET_AURA_UNDEAD:
			
			{
				final int radius = getSkillRadius();
				final boolean srcInArena = (activeChar.isInsideZone(L2Character.ZONE_PVP) && !activeChar.isInsideZone(L2Character.ZONE_SIEGE));
				
				final L2PcInstance src = activeChar.getActingPlayer();
				
				// Go through the L2Character _knownList
				for (final L2Character obj : activeChar.getKnownList().getKnownCharactersInRadius(radius))
				{
					if ((obj instanceof L2NpcInstance) || (obj instanceof L2SummonInstance))
					{
						
						if (obj.isDead())
						{
							continue;
						}
						
						if (!obj.isUndead())
						{
							continue;
						}
						
						if (src != null)
						{
							
							final L2PcInstance trg = obj.getActingPlayer();
							
							// check if both attacker and target are L2PcInstances and if they are in same party
							if (trg != null)
							{
								if (trg == src)
								{
									continue;
								}
								
								if (!src.checkPvpSkill(trg, this, activeChar instanceof L2Summon))
								{
									continue;
								}
								
								if ((src.getParty() != null) && (trg.getParty() != null))
								{
									if (src.getParty().getPartyLeaderOID() == trg.getParty().getPartyLeaderOID())
									{
										continue;
									}
									
									if ((src.getParty().getCommandChannel() != null) && (trg.getParty().getCommandChannel() != null))
									{
										if (src.getParty().getCommandChannel() == trg.getParty().getCommandChannel())
										{
											continue;
										}
									}
								}
								
								if (src.getEventTeam() > 0)
								{
									if (src.getEventTeam() == trg.getEventTeam())
									{
										continue;
									}
								}
								
								if (!srcInArena && !(trg.isInsideZone(L2Character.ZONE_PVP) && !trg.isInsideZone(L2Character.ZONE_SIEGE)))
								{
									if ((src.getAllyId() == trg.getAllyId()) && (src.getAllyId() != 0))
									{
										continue;
									}
									
									if ((src.getClanId() != 0) && (src.getClanId() == trg.getClanId()))
									{
										continue;
									}
								}
							}
						}
						
						if (!GeoData.getInstance().canSeeTarget(activeChar, obj))
						{
							continue;
						}
						
						if (onlyFirst == false)
						{
							targetList.add(obj);
						}
						else
						{
							return new L2Character[]
							{
								obj
							};
						}
					}
				}
				return targetList.toArray(new L2Character[targetList.size()]);
			}
			default:
			{
				activeChar.sendMessage("Target type of skill is not currently handled.");
				return null;
			}
		}
	}
	
	public final L2Object[] getTargetList(L2Character activeChar)
	{
		return getTargetList(activeChar, false);
	}
	
	public final L2Object getFirstOfTargetList(L2Character activeChar)
	{
		L2Object[] targets;
		
		targets = getTargetList(activeChar, true);
		
		if ((targets == null) || (targets.length == 0))
		{
			return null;
		}
		return targets[0];
	}
	
	public final Func[] getStatFuncs(L2Effect effect, L2Character player)
	{
		
		if (!(player instanceof L2PcInstance) && !(player instanceof L2Attackable) && !(player instanceof L2Summon) && !(player instanceof L2SiegeFlagInstance))
		{
			return _emptyFunctionSet;
		}
		
		if (_funcTemplates == null)
		{
			return _emptyFunctionSet;
		}
		
		_affected = player;
		
		final List<Func> funcs = new FastList<>();
		for (final FuncTemplate t : _funcTemplates)
		{
			final Env env = new Env();
			env.player = _affected;
			env.skill = this;
			final Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.size() == 0)
		{
			return _emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	public boolean hasEffects()
	{
		return ((_effectTemplates != null) && (_effectTemplates.length > 0));
	}
	
	public final L2Effect[] getEffects(L2Character effector, L2Character effected)
	{
		return getEffects(effector, effected, !isToggle() && Formulas.getInstance().calcSkillMastery(effector, this));
	}
	
	public final L2Effect[] getEffects(L2Character effector, L2Character effected, boolean skillMastery)
	{
		if (isPassive())
		{
			return _emptyEffectSet;
		}
		
		if (_effectTemplates == null)
		{
			return _emptyEffectSet;
		}
		
		if ((effector != effected) && effected.isInvul())
		{
			return _emptyEffectSet;
		}
		
		// block bad buffs with /block command
		if (Config.BLOCK_UNWANTED_BUFFS)
		{
			if ((effector instanceof L2PcInstance) && (effected instanceof L2PcInstance))
			{
				if (!isOffensive() && BlockList.isBlocked(((L2PcInstance) effected), ((L2PcInstance) effector)))
				{
					return _emptyEffectSet;
				}
			}
		}
		
		// No effects for doors & siege headquarters
		if ((effected instanceof L2DoorInstance) || (effected instanceof L2SiegeFlagInstance))
		{
			return _emptyEffectSet;
		}
		
		final List<L2Effect> effects = new FastList<>();
		for (final EffectTemplate et : _effectTemplates)
		{
			final Env env = new Env();
			env.player = effector;
			env.target = effected;
			env.skill = this;
			env.skillMastery = skillMastery;
			final L2Effect e = et.getEffect(env);
			if (e != null)
			{
				effects.add(e);
			}
			
		}
		
		if (effects.size() == 0)
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final L2Effect[] getEffectsSelf(L2Character effector)
	{
		if (isPassive())
		{
			return _emptyEffectSet;
		}
		
		if (_effectTemplatesSelf == null)
		{
			return _emptyEffectSet;
		}
		
		final List<L2Effect> effects = new FastList<>();
		
		for (final EffectTemplate et : _effectTemplatesSelf)
		{
			final Env env = new Env();
			env.player = effector;
			env.target = effector;
			env.skill = this;
			final L2Effect e = et.getEffect(env);
			if (e != null)
			{
				e.setSelfEffect();
				effects.add(e);
			}
			
		}
		if (effects.size() == 0)
		{
			return _emptyEffectSet;
		}
		
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	public final void attach(FuncTemplate f)
	{
		if (_funcTemplates == null)
		{
			_funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = _funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			System.arraycopy(_funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			_funcTemplates = tmp;
		}
	}
	
	public final void attach(EffectTemplate effect)
	{
		if (_effectTemplates == null)
		{
			_effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = _effectTemplates.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}
	
	public final void attachSelf(EffectTemplate effect)
	{
		if (_effectTemplatesSelf == null)
		{
			_effectTemplatesSelf = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = _effectTemplatesSelf.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplatesSelf, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplatesSelf = tmp;
		}
	}
	
	public final void attach(Condition c, boolean itemOrWeapon)
	{
		if (itemOrWeapon)
		{
			_itemPreCondition = c;
		}
		else
		{
			_preCondition = c;
		}
	}
	
	@Override
	public String toString()
	{
		return "" + _name + "[id=" + _id + ",lvl=" + _level + "]";
	}
	
	public void doNegate(L2Character target)
	{
		if (_negateStats == null)
		{
			return;
		}
		
		for (final SkillType stat : _negateStats)
		{
			if (stat == null)
			{
				continue;
			}
			
			target.negateEffects(stat, _negatePower, getMaxNegatedEffects());
		}
	}
	
	public L2Character getAffected()
	{
		return _affected;
	}
}