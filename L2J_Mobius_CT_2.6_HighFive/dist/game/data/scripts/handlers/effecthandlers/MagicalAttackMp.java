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

import org.l2jmobius.gameserver.enums.ShotType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.effects.EffectType;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.stats.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * Magical Attack MP effect.
 * @author Adry_85
 */
public class MagicalAttackMp extends AbstractEffect
{
	public MagicalAttackMp(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		if (info.getEffected().isInvul())
		{
			return false;
		}
		if (!Formulas.calcMagicAffected(info.getEffector(), info.getEffected(), info.getSkill()))
		{
			if (info.getEffector().isPlayer())
			{
				info.getEffector().sendPacket(SystemMessageId.YOUR_ATTACK_HAS_FAILED);
			}
			if (info.getEffected().isPlayer())
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.C1_RESISTED_C2_S_MAGIC);
				sm.addString(info.getEffected().getName());
				sm.addString(info.getEffector().getName());
				info.getEffected().sendPacket(sm);
			}
			return false;
		}
		return true;
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.MAGICAL_ATTACK;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Creature target = info.getEffected();
		final Creature creature = info.getEffector();
		if (creature.isAlikeDead())
		{
			return;
		}
		
		final boolean sps = info.getSkill().useSpiritShot() && creature.isChargedShot(ShotType.SPIRITSHOTS);
		final boolean bss = info.getSkill().useSpiritShot() && creature.isChargedShot(ShotType.BLESSED_SPIRITSHOTS);
		final byte shld = Formulas.calcShldUse(creature, target, info.getSkill());
		final boolean mcrit = Formulas.calcMCrit(creature.getMCriticalHit(target, info.getSkill()));
		final double damage = Formulas.calcManaDam(creature, target, info.getSkill(), shld, sps, bss, mcrit);
		final double mp = (damage > target.getCurrentMp() ? target.getCurrentMp() : damage);
		if (damage > 0)
		{
			target.stopEffectsOnDamage(true);
			target.setCurrentMp(target.getCurrentMp() - mp);
		}
		
		if (target.isPlayer())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S2_S_MP_HAS_BEEN_DRAINED_BY_C1);
			sm.addString(creature.getName());
			sm.addInt((int) mp);
			target.sendPacket(sm);
		}
		
		if (creature.isPlayer())
		{
			final SystemMessage sm2 = new SystemMessage(SystemMessageId.YOUR_OPPONENT_S_MP_WAS_REDUCED_BY_S1);
			sm2.addInt((int) mp);
			creature.sendPacket(sm2);
		}
	}
}