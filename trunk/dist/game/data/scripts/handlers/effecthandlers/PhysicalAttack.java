/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import java.util.StringTokenizer;

import com.l2jserver.gameserver.enums.ShotType;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Formulas;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Physical Attack effect implementation.
 * @author Adry_85
 */
public final class PhysicalAttack extends AbstractEffect
{
	private final String _type1;
	private final double _valueReduce;
	private final String _type2;
	private final double _valueIncrease;
	private final boolean _isLastAttack;
	
	public PhysicalAttack(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_type1 = params.getString("weaponTypeDec", "NONE");
		_valueReduce = params.getDouble("valueDec", 1);
		_type2 = params.getString("weaponTypeInc", "NONE");
		_valueIncrease = params.getDouble("valueInc", 1);
		_isLastAttack = params.getBoolean("isLastAttack", false);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
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
		final L2Character target = info.getEffected();
		final L2Character activeChar = info.getEffector();
		final Skill skill = info.getSkill();
		
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		if (((info.getSkill().getFlyRadius() > 0) || (skill.getFlyType() != null)) && activeChar.isMovementDisabled())
		{
			final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS);
			sm.addSkillName(skill);
			activeChar.sendPacket(sm);
			return;
		}
		
		if (target.isPlayer() && target.getActingPlayer().isFakeDeath())
		{
			target.stopFakeDeath(true);
		}
		
		int damage = 0;
		final boolean ss = skill.isPhysical() && activeChar.isChargedShot(ShotType.SOULSHOTS);
		final byte shld = Formulas.calcShldUse(activeChar, target, skill);
		// Physical damage critical rate is only affected by STR.
		boolean crit = false;
		if (skill.getBaseCritRate() > 0)
		{
			crit = Formulas.calcCrit(activeChar, target, skill);
		}
		
		damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, ss);
		
		if (crit)
		{
			damage *= 2;
		}
		
		if ((activeChar.getActiveWeaponItem() != null) && (_type1 != "NONE") && (_type2 != "NONE"))
		{
			StringTokenizer st = new StringTokenizer(_type1, ",");
			while (st.hasMoreTokens())
			{
				final String item = st.nextToken().trim();
				if (activeChar.getActiveWeaponItem().getItemType() == WeaponType.valueOf(item))
				{
					damage *= _valueReduce;
					break;
				}
			}
			st = new StringTokenizer(_type2, ",");
			while (st.hasMoreTokens())
			{
				final String item = st.nextToken().trim();
				if (activeChar.getActiveWeaponItem().getItemType() == WeaponType.valueOf(item))
				{
					damage *= _valueIncrease;
					break;
				}
			}
		}
		
		if (damage > 0)
		{
			// reduce damage if target has maxdamage buff
			final double maxDamage = (target.getStat().calcStat(Stats.MAX_SKILL_DAMAGE, 0, null, null));
			if (maxDamage > 0)
			{
				damage = (int) maxDamage;
			}
			
			activeChar.sendDamageMessage(target, damage, false, crit, false);
			if (_isLastAttack && !target.isPlayer() && !target.isRaid())
			{
				if (damage < target.getCurrentHp())
				{
					target.setCurrentHp(1);
				}
				else
				{
					target.reduceCurrentHp(damage, activeChar, skill);
				}
			}
			else
			{
				target.reduceCurrentHp(damage, activeChar, skill);
				target.notifyDamageReceived(damage, activeChar, skill, crit, false);
			}
			
			// Check if damage should be reflected
			Formulas.calcDamageReflected(activeChar, target, skill, crit);
		}
		else
		{
			activeChar.sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
		}
		
		if (skill.isSuicideAttack())
		{
			activeChar.doDie(activeChar);
		}
	}
}
