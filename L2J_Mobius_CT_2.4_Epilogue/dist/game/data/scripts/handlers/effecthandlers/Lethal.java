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
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.Condition;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.BuffInfo;
import org.l2jmobius.gameserver.model.stats.Formulas;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Lethal effect implementation.
 * @author Adry_85
 */
public class Lethal extends AbstractEffect
{
	private final int _fullLethal;
	private final int _halfLethal;
	
	public Lethal(Condition attachCond, Condition applyCond, StatSet set, StatSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_fullLethal = params.getInt("fullLethal", 0);
		_halfLethal = params.getInt("halfLethal", 0);
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
		if (creature.isPlayer() && !creature.getAccessLevel().canGiveDamage())
		{
			return;
		}
		
		if (info.getSkill().getMagicLevel() < (target.getLevel() - 6))
		{
			return;
		}
		
		if (!target.isLethalable() || target.isInvul())
		{
			return;
		}
		
		final double chanceMultiplier = Formulas.calcAttributeBonus(creature, target, info.getSkill()) * Formulas.calcGeneralTraitBonus(creature, target, info.getSkill().getTraitType(), false);
		// Lethal Strike
		if (Rnd.get(100) < (_fullLethal * chanceMultiplier))
		{
			// for Players CP and HP is set to 1.
			if (target.isPlayer())
			{
				target.notifyDamageReceived(target.getCurrentHp() - 1, info.getEffector(), info.getSkill(), true, false);
				target.setCurrentCp(1);
				target.setCurrentHp(1);
				target.sendPacket(SystemMessageId.LETHAL_STRIKE);
			}
			// for Monsters HP is set to 1.
			else if (target.isMonster() || target.isSummon())
			{
				target.notifyDamageReceived(target.getCurrentHp() - 1, info.getEffector(), info.getSkill(), true, false);
				target.setCurrentHp(1);
			}
			creature.sendPacket(SystemMessageId.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
		}
		// Half-Kill
		else if (Rnd.get(100) < (_halfLethal * chanceMultiplier))
		{
			// for Players CP is set to 1.
			if (target.isPlayer())
			{
				target.setCurrentCp(1);
				target.sendPacket(SystemMessageId.HALF_KILL);
				target.sendPacket(SystemMessageId.YOUR_CP_WAS_DRAINED_BECAUSE_YOU_WERE_HIT_WITH_A_HALF_KILL_SKILL);
			}
			// for Monsters HP is set to 50%.
			else if (target.isMonster() || target.isSummon())
			{
				target.notifyDamageReceived(target.getCurrentHp() * 0.5, info.getEffector(), info.getSkill(), true, false);
				target.setCurrentHp(target.getCurrentHp() * 0.5);
			}
			creature.sendPacket(SystemMessageId.HALF_KILL);
		}
	}
}