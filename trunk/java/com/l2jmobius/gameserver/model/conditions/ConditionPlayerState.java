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
package com.l2jmobius.gameserver.model.conditions;

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.PlayerState;
import com.l2jmobius.gameserver.model.items.L2Item;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * The Class ConditionPlayerState.
 * @author mkizub
 */
public class ConditionPlayerState extends Condition
{
	private final PlayerState _check;
	private final boolean _required;
	
	/**
	 * Instantiates a new condition player state.
	 * @param check the player state to be verified.
	 * @param required the required value.
	 */
	public ConditionPlayerState(PlayerState check, boolean required)
	{
		_check = check;
		_required = required;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		final L2Character character = effector;
		final L2PcInstance player = effector.getActingPlayer();
		switch (_check)
		{
			case RESTING:
			{
				return player != null ? player.isSitting() == _required : !_required;
			}
			case MOVING:
			{
				return character.isMoving() == _required;
			}
			case RUNNING:
			{
				return character.isRunning() == _required;
			}
			case STANDING:
			{
				return player != null ? _required != (player.isSitting() || player.isMoving()) : _required != character.isMoving();
			}
			case FLYING:
			{
				return (character.isFlying() == _required);
			}
			case BEHIND:
			{
				return (character.isBehindTarget() == _required);
			}
			case FRONT:
			{
				return (character.isInFrontOfTarget() == _required);
			}
			case CHAOTIC:
			{
				return player != null ? (player.getReputation() < 0) == _required : !_required;
			}
			case OLYMPIAD:
			{
				return player != null ? player.isInOlympiadMode() == _required : !_required;
			}
		}
		return !_required;
	}
}
