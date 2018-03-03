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
package com.l2jmobius.gameserver.skills.conditions;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.skills.Env;

/**
 * @author eX1steam
 */
public class ConditionTargetPvp extends Condition
{
	private final int _pvp;
	
	public ConditionTargetPvp(int pvp)
	{
		_pvp = pvp;
	}
	
	@Override
	public boolean testImpl(Env env)
	{
		final L2Character target = env.target;
		if ((target instanceof L2PcInstance) && (((L2PcInstance) target).getPvpFlag() != 0))
		{
			return ((L2PcInstance) target).getPvpFlag() == _pvp;
		}
		return false;
	}
}
