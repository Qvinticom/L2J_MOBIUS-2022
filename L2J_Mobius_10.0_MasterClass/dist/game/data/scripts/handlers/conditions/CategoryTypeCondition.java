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
package handlers.conditions;

import java.util.List;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.conditions.ICondition;

/**
 * @author Sdw
 */
public class CategoryTypeCondition implements ICondition
{
	private final List<CategoryType> _categoryTypes;
	
	public CategoryTypeCondition(StatSet params)
	{
		_categoryTypes = params.getEnumList("category", CategoryType.class);
	}
	
	@Override
	public boolean test(Creature creature, WorldObject target)
	{
		return _categoryTypes.stream().anyMatch(creature::isInCategory);
	}
}
