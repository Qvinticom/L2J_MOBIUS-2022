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
package org.l2jmobius.gameserver.model.skill.conditions;

import java.util.List;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.skill.Env;

/**
 * @author nBd
 */
public class ConditionTargetRaceId extends Condition
{
	private final List<Integer> _raceIds;
	
	public ConditionTargetRaceId(List<Integer> raceId)
	{
		_raceIds = raceId;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		if ((_raceIds == null) || (env.target == null) || !(env.target instanceof Npc))
		{
			return false;
		}
		
		final Npc target = (Npc) env.target;
		if ((target.getTemplate() != null) && (target.getTemplate().getRace() != null))
		{
			return _raceIds.contains(((Npc) env.target).getTemplate().getRace().ordinal() + 1);
		}
		return false;
	}
}
