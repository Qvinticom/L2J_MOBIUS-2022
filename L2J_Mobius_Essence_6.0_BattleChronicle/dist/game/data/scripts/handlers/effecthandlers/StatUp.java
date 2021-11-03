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

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.model.stats.BaseStat;
import org.l2jmobius.gameserver.model.stats.Stat;

/**
 * @author Sdw
 */
public class StatUp extends AbstractEffect
{
	private final BaseStat _stat;
	private final double _amount;
	
	public StatUp(StatSet params)
	{
		_amount = params.getDouble("amount", 0);
		_stat = params.getEnum("stat", BaseStat.class, BaseStat.STR);
	}
	
	@Override
	public void pump(Creature effected, Skill skill)
	{
		Stat stat = Stat.STAT_STR;
		
		switch (_stat)
		{
			case INT:
			{
				stat = Stat.STAT_INT;
				break;
			}
			case DEX:
			{
				stat = Stat.STAT_DEX;
				break;
			}
			case WIT:
			{
				stat = Stat.STAT_WIT;
				break;
			}
			case CON:
			{
				stat = Stat.STAT_CON;
				break;
			}
			case MEN:
			{
				stat = Stat.STAT_MEN;
				break;
			}
		}
		effected.getStat().mergeAdd(stat, _amount);
	}
}
