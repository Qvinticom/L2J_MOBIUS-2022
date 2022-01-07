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
package org.l2jmobius.gameserver.model.skill;

import java.lang.reflect.Constructor;

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.skill.handlers.SkillCharge;
import org.l2jmobius.gameserver.model.skill.handlers.SkillChargeDmg;
import org.l2jmobius.gameserver.model.skill.handlers.SkillChargeEffect;
import org.l2jmobius.gameserver.model.skill.handlers.SkillCreateItem;
import org.l2jmobius.gameserver.model.skill.handlers.SkillDefault;
import org.l2jmobius.gameserver.model.skill.handlers.SkillDrain;
import org.l2jmobius.gameserver.model.skill.handlers.SkillSeed;
import org.l2jmobius.gameserver.model.skill.handlers.SkillSignet;
import org.l2jmobius.gameserver.model.skill.handlers.SkillSignetCasttime;
import org.l2jmobius.gameserver.model.skill.handlers.SkillSummon;

/**
 * @author Mobius
 */
public enum SkillType
{
	// Damage
	PDAM,
	MDAM,
	CPDAM,
	MANADAM,
	DOT,
	MDOT,
	DRAIN_SOUL,
	DRAIN(SkillDrain.class),
	DEATHLINK,
	FATALCOUNTER,
	BLOW,
	
	// Disablers
	BLEED,
	POISON,
	STUN,
	ROOT,
	CONFUSION,
	FEAR,
	SLEEP,
	CONFUSE_MOB_ONLY,
	MUTE,
	PARALYZE,
	WEAKNESS,
	
	// HP, MP, CP
	HEAL,
	HOT,
	BALANCE_LIFE,
	HEAL_PERCENT,
	HEAL_STATIC,
	COMBATPOINTHEAL,
	COMBATPOINTPERCENTHEAL,
	CPHOT,
	MANAHEAL,
	MANA_BY_LEVEL,
	MANAHEAL_PERCENT,
	MANARECHARGE,
	MPHOT,
	
	// Aggro
	AGGDAMAGE,
	AGGREDUCE,
	AGGREMOVE,
	AGGREDUCE_CHAR,
	AGGDEBUFF,
	
	// Fishing
	FISHING,
	PUMPING,
	REELING,
	
	// Misc
	UNLOCK,
	ENCHANT_ARMOR,
	ENCHANT_WEAPON,
	SOULSHOT,
	SPIRITSHOT,
	SIEGEFLAG,
	TAKECASTLE,
	DELUXE_KEY_UNLOCK,
	SOW,
	HARVEST,
	GET_PLAYER,
	
	// Creation
	COMMON_CRAFT,
	DWARVEN_CRAFT,
	CREATE_ITEM(SkillCreateItem.class),
	SUMMON_TREASURE_KEY,
	
	// Summons
	SUMMON(SkillSummon.class),
	FEED_PET,
	DEATHLINK_PET,
	STRSIEGEASSAULT,
	ERASE,
	BETRAY,
	
	// Cancel
	CANCEL,
	MAGE_BANE,
	WARRIOR_BANE,
	NEGATE,
	
	BUFF,
	DEBUFF,
	PASSIVE,
	CONT,
	SIGNET(SkillSignet.class),
	SIGNET_CASTTIME(SkillSignetCasttime.class),
	
	RESURRECT,
	CHARGE(SkillCharge.class),
	CHARGE_EFFECT(SkillChargeEffect.class),
	CHARGEDAM(SkillChargeDmg.class),
	MHOT,
	DETECT_WEAKNESS,
	LUCK,
	RECALL,
	SUMMON_FRIEND,
	REFLECT,
	SPOIL,
	SWEEP,
	FAKE_DEATH,
	UNBLEED,
	UNPOISON,
	UNDEAD_DEFENSE,
	SEED(SkillSeed.class),
	BEAST_FEED,
	FORCE_BUFF,
	CLAN_GATE,
	GIVE_SP,
	CORE_DONE,
	ZAKEN_PLAYER,
	ZAKEN_SELF,
	
	// Unimplemented
	NOT_DONE;
	
	private final Class<? extends Skill> _class;
	
	public Skill makeSkill(StatSet set)
	{
		try
		{
			final Constructor<? extends Skill> c = _class.getConstructor(StatSet.class);
			return c.newInstance(set);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private SkillType()
	{
		_class = SkillDefault.class;
	}
	
	private SkillType(Class<? extends Skill> classType)
	{
		_class = classType;
	}
}
