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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.actor.Creature;

public class MagicSkillUse extends GameServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _targetId;
	private final int _targetx;
	private final int _targety;
	private final int _targetz;
	private final int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	
	public MagicSkillUse(Creature creature, Creature target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_objectId = creature.getObjectId();
		_x = creature.getX();
		_y = creature.getY();
		_z = creature.getZ();
		if (target != null)
		{
			_targetId = target.getObjectId();
			_targetx = target.getX();
			_targety = target.getY();
			_targetz = target.getZ();
		}
		else
		{
			_targetId = creature.getTargetId();
			_targetx = creature.getX();
			_targety = creature.getY();
			_targetz = creature.getZ();
		}
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_objectId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		// if (_critical) // ?
		// {
		// writeD(0x01);
		// writeH(0x00);
		// }
		// else
		// {
		writeD(0x00);
		// }
		writeD(_targetx);
		writeD(_targety);
		writeD(_targetz);
	}
}