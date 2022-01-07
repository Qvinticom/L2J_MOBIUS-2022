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
package org.l2jmobius.gameserver.handler.skillhandlers;

import java.util.List;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.skill.SkillType;

public class ZakenPlayer implements ISkillHandler
{
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.ZAKEN_PLAYER
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		try
		{
			for (WorldObject target1 : targets)
			{
				if (!(target1 instanceof Creature))
				{
					continue;
				}
				
				final Creature target = (Creature) target1;
				switch (Rnd.get(14) + 1)
				{
					case 1:
					{
						target.teleToLocation(55299, 219120, -2952, true);
						break;
					}
					case 2:
					{
						target.teleToLocation(56363, 218043, -2952, true);
						break;
					}
					case 3:
					{
						target.teleToLocation(54245, 220162, -2952, true);
						break;
					}
					case 4:
					{
						target.teleToLocation(56289, 220126, -2952, true);
						break;
					}
					case 5:
					{
						target.teleToLocation(55299, 219120, -3224, true);
						break;
					}
					case 6:
					{
						target.teleToLocation(56363, 218043, -3224, true);
						break;
					}
					case 7:
					{
						target.teleToLocation(54245, 220162, -3224, true);
						break;
					}
					case 8:
					{
						target.teleToLocation(56289, 220126, -3224, true);
						break;
					}
					case 9:
					{
						target.teleToLocation(55299, 219120, -3496, true);
						break;
					}
					case 10:
					{
						target.teleToLocation(56363, 218043, -3496, true);
						break;
					}
					case 11:
					{
						target.teleToLocation(54245, 220162, -3496, true);
						break;
					}
					case 12:
					{
						target.teleToLocation(56289, 220126, -3496, true);
						break;
					}
					default:
					{
						target.teleToLocation(53930, 217760, -2944, true);
						break;
					}
				}
			}
		}
		catch (Throwable e)
		{
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}