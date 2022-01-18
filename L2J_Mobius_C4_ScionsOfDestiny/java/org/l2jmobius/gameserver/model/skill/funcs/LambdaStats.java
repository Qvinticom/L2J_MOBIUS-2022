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
package org.l2jmobius.gameserver.model.skill.funcs;

import org.l2jmobius.gameserver.model.skill.Env;

/**
 * @author mkizub
 */
public class LambdaStats extends Lambda
{
	private final LambdaStatType _stat;
	
	public LambdaStats(LambdaStatType stat)
	{
		_stat = stat;
	}
	
	@Override
	public double calc(Env env)
	{
		switch (_stat)
		{
			case PLAYER_LEVEL:
			{
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getLevel();
			}
			case TARGET_LEVEL:
			{
				if (env.target == null)
				{
					return 1;
				}
				return env.target.getLevel();
			}
			case PLAYER_MAX_HP:
			{
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getMaxHp();
			}
			case PLAYER_MAX_MP:
			{
				if (env.player == null)
				{
					return 1;
				}
				return env.player.getMaxMp();
			}
		}
		return 0;
	}
}
