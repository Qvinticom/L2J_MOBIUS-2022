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

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.stats.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Unsummon effect implementation.
 * @author Adry_85
 */
public class Unsummon extends AbstractEffect
{
	private final int _chance;
	
	public Unsummon(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_chance = params.getInt("chance", 100);
	}
	
	@Override
	public boolean calcSuccess(BuffInfo info)
	{
		final int magicLevel = info.getSkill().getMagicLevel();
		if ((magicLevel <= 0) || ((info.getEffected().getLevel() - 9) <= magicLevel))
		{
			final double chance = _chance * Formulas.calcAttributeBonus(info.getEffector(), info.getEffected(), info.getSkill()) * Formulas.calcGeneralTraitBonus(info.getEffector(), info.getEffected(), info.getSkill().getTraitType(), false);
			if (chance > (Rnd.nextDouble() * 100))
			{
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		final Summon summon = info.getEffected().getSummon();
		if (summon != null)
		{
			final Player summonOwner = summon.getOwner();
			summon.abortAttack();
			summon.abortCast();
			summon.stopAllEffects();
			
			summon.unSummon(summonOwner);
			summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED_YOU_LL_NEED_TO_SUMMON_A_NEW_ONE);
		}
	}
}
