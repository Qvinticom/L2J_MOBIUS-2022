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
package org.l2jmobius.gameserver.model.conditions;

import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.items.ItemTemplate;
import org.l2jmobius.gameserver.model.skills.Skill;

/**
 * Exist NPC condition.
 * @author UnAfraid, Zoey76
 */
public class ConditionPlayerRangeFromNpc extends Condition
{
	/** NPC Ids. */
	private final int[] _npcIds;
	/** Radius to check. */
	private final int _radius;
	/** Expected value. */
	private final boolean _value;
	
	public ConditionPlayerRangeFromNpc(int[] npcIds, int radius, boolean value)
	{
		_npcIds = npcIds;
		_radius = radius;
		_value = value;
	}
	
	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		boolean existNpc = false;
		if ((_npcIds != null) && (_npcIds.length > 0) && (_radius > 0))
		{
			for (Npc target : World.getInstance().getVisibleObjectsInRange(effector, Npc.class, _radius))
			{
				if (CommonUtil.contains(_npcIds, target.getId()))
				{
					existNpc = true;
					break;
				}
			}
		}
		return existNpc == _value;
	}
}
