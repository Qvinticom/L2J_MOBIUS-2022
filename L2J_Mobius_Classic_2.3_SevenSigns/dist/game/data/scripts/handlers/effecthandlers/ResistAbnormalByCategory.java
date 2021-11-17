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

import org.l2jmobius.gameserver.enums.DispelSlotType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class ResistAbnormalByCategory extends AbstractEffect
{
	private final DispelSlotType _slot;
	private final double _amount;
	
	public ResistAbnormalByCategory(StatSet params)
	{
		_amount = params.getDouble("amount", 0);
		_slot = params.getEnum("slot", DispelSlotType.class, DispelSlotType.DEBUFF);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		switch (_slot)
		{
			// Only this one is in use it seems
			case DEBUFF:
			{
				effected.getStat().mergeMul(Stat.RESIST_ABNORMAL_DEBUFF, 1 + (_amount / 100));
				break;
			}
		}
	}
}
