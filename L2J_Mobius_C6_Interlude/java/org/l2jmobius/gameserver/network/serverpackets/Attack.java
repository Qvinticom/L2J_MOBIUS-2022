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
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * sample 06 8f19904b 2522d04b 00000000 80 950c0000 4af50000 08f2ffff 0000 - 0 damage (missed 0x80) 06 85071048 bc0e504b 32000000 10 fc41ffff fd240200 a6f5ffff 0100 bc0e504b 33000000 10 3.... format dddc dddh (ddc)
 * @version $Revision: 1.3.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class Attack implements IClientOutgoingPacket
{
	private class Hit
	{
		protected int _targetId;
		protected int _damage;
		protected int _flags;
		
		Hit(WorldObject target, int damage, boolean miss, boolean crit, boolean shld)
		{
			_targetId = target.getObjectId();
			_damage = damage;
			if (soulshot)
			{
				_flags |= 0x10 | _grade;
			}
			if (crit)
			{
				_flags |= 0x20;
			}
			if (shld)
			{
				_flags |= 0x40;
			}
			if (miss)
			{
				_flags |= 0x80;
			}
		}
	}
	
	protected final int _attackerObjId;
	public boolean soulshot;
	protected int _grade;
	private final int _x;
	private final int _y;
	private final int _z;
	private Hit[] _hits;
	
	/**
	 * @param attacker the attacker Creature
	 * @param ss true if useing SoulShots
	 * @param grade
	 */
	public Attack(Creature attacker, boolean ss, int grade)
	{
		_attackerObjId = attacker.getObjectId();
		soulshot = ss;
		_grade = grade;
		_x = attacker.getX();
		_y = attacker.getY();
		_z = attacker.getZ();
		_hits = new Hit[0];
	}
	
	/**
	 * Add this hit (target, damage, miss, critical, shield) to the Server-Client packet Attack.
	 * @param target
	 * @param damage
	 * @param miss
	 * @param crit
	 * @param shld
	 */
	public void addHit(WorldObject target, int damage, boolean miss, boolean crit, boolean shld)
	{
		// Get the last position in the hits table
		final int pos = _hits.length;
		// Create a new Hit object
		final Hit[] tmp = new Hit[pos + 1];
		// Add the new Hit object to hits table
		System.arraycopy(_hits, 0, tmp, 0, _hits.length);
		tmp[pos] = new Hit(target, damage, miss, crit, shld);
		_hits = tmp;
	}
	
	/**
	 * Return True if the Server-Client packet Attack contains at least 1 hit.
	 * @return
	 */
	public boolean hasHits()
	{
		return _hits.length > 0;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ATTACK.writeId(packet);
		packet.writeD(_attackerObjId);
		packet.writeD(_hits[0]._targetId);
		packet.writeD(_hits[0]._damage);
		packet.writeC(_hits[0]._flags);
		packet.writeD(_x);
		packet.writeD(_y);
		packet.writeD(_z);
		packet.writeH(_hits.length - 1);
		for (int i = 1; i < _hits.length; i++)
		{
			packet.writeD(_hits[i]._targetId);
			packet.writeD(_hits[i]._damage);
			packet.writeC(_hits[i]._flags);
		}
		return true;
	}
}
