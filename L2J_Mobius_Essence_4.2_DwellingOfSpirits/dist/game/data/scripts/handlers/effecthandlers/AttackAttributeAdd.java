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

import org.l2jmobius.gameserver.enums.AttributeType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class AttackAttributeAdd extends AbstractEffect
{
	private final double _amount;
	
	public AttackAttributeAdd(StatSet params)
	{
		_amount = params.getDouble("amount", 0);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		Stat stat = Stat.FIRE_POWER;
		AttributeType maxAttribute = AttributeType.FIRE;
		int maxValue = 0;
		
		for (AttributeType attribute : AttributeType.values())
		{
			final int attributeValue = effected.getStat().getAttackElementValue(attribute);
			if ((attributeValue > 0) && (attributeValue > maxValue))
			{
				maxAttribute = attribute;
				maxValue = attributeValue;
			}
		}
		
		switch (maxAttribute)
		{
			case WATER:
			{
				stat = Stat.WATER_POWER;
				break;
			}
			case WIND:
			{
				stat = Stat.WIND_POWER;
				break;
			}
			case EARTH:
			{
				stat = Stat.EARTH_POWER;
				break;
			}
			case HOLY:
			{
				stat = Stat.HOLY_POWER;
				break;
			}
			case DARK:
			{
				stat = Stat.DARK_POWER;
				break;
			}
		}
		
		effected.getStat().mergeAdd(stat, _amount);
	}
}
