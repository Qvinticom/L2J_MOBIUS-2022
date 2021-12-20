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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class MagicSkillUse implements IClientOutgoingPacket
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
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.MAGIC_SKILL_USE.writeId(packet);
		packet.writeD(_objectId);
		packet.writeD(_targetId);
		packet.writeD(_skillId);
		packet.writeD(_skillLevel);
		packet.writeD(_hitTime);
		packet.writeD(_reuseDelay);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		// if (_critical) // ?
		// {
		// writeD(1);
		// writeH(0);
		// }
		// else
		// {
		packet.writeD(0);
		// }
		packet.writeD(_targetx);
		packet.writeD(_targety);
		packet.writeD(_targetz);
		return true;
	}
}