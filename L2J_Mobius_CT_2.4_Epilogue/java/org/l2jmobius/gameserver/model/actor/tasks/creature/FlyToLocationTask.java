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
package org.l2jmobius.gameserver.model.actor.tasks.creature;

import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.skills.Skill;
import org.l2jmobius.gameserver.network.serverpackets.FlyToLocation;

/**
 * Task dedicated to fly a player to the location
 * @author xban1x
 */
public class FlyToLocationTask implements Runnable
{
	private final Creature _creature;
	private final WorldObject _target;
	private final Skill _skill;
	
	public FlyToLocationTask(Creature creature, WorldObject target, Skill skill)
	{
		_creature = creature;
		_target = target;
		_skill = skill;
	}
	
	@Override
	public void run()
	{
		if (_creature != null)
		{
			_creature.broadcastPacket(new FlyToLocation(_creature, _target, _skill.getFlyType()));
			_creature.setXYZ(_target.getX(), _target.getY(), _target.getZ());
		}
	}
}
