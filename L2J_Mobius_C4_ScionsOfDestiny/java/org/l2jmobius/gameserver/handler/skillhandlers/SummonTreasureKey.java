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
import java.util.logging.Logger;

import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.handler.ISkillHandler;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.Skill.SkillType;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Player;

/**
 * @author evill33t
 */
public class SummonTreasureKey implements ISkillHandler
{
	private static final Logger LOGGER = Logger.getLogger(SummonTreasureKey.class.getName());
	
	private static final SkillType[] SKILL_TYPES =
	{
		SkillType.SUMMON_TREASURE_KEY
	};
	
	@Override
	public void useSkill(Creature creature, Skill skill, List<Creature> targets)
	{
		if (!(creature instanceof Player))
		{
			return;
		}
		
		final Player player = (Player) creature;
		
		try
		{
			int itemId = 0;
			switch (skill.getLevel())
			{
				case 1:
				{
					itemId = Rnd.get(6667, 6669);
					break;
				}
				case 2:
				{
					itemId = Rnd.get(6668, 6670);
					break;
				}
				case 3:
				{
					itemId = Rnd.get(6669, 6671);
					break;
				}
				case 4:
				{
					itemId = Rnd.get(6670, 6672);
					break;
				}
			}
			player.addItem("Skill", itemId, Rnd.get(2, 3), player, false);
		}
		catch (Exception e)
		{
			LOGGER.warning("Error using skill summon Treasure Key:" + e);
		}
	}
	
	@Override
	public SkillType[] getSkillTypes()
	{
		return SKILL_TYPES;
	}
}
