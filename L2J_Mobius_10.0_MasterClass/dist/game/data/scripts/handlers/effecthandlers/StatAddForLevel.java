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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.gameserver.enums.StatModifierType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Mobius
 */
public class StatAddForLevel extends AbstractEffect
{
	private final Stat _stat;
	private final Map<Integer, Integer> _values;
	
	public StatAddForLevel(StatSet params)
	{
		_stat = params.getEnum("stat", Stat.class);
		
		final List<Integer> amount = params.getIntegerList("amount");
		_values = new HashMap<>(amount.size());
		int index = 0;
		for (Integer level : params.getIntegerList("level"))
		{
			_values.put(level, amount.get(index++));
		}
		
		if (params.getEnum("mode", StatModifierType.class, StatModifierType.DIFF) != StatModifierType.DIFF)
		{
			LOGGER.warning(getClass().getSimpleName() + " can only use DIFF mode.");
		}
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		final Integer amount = _values.get(effected.getLevel());
		if (amount != null)
		{
			effected.getStat().mergeAdd(_stat, amount.intValue());
		}
	}
}
