/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

public class Attack extends ServerBasePacket
{
	private final int _attackerId;
	private final int _defenderId;
	private final int _damage;
	private final boolean _miss;
	private final boolean _critical;
	private final boolean _soulshot;
	private final int _x;
	private final int _y;
	private final int _z;
	
	public Attack(int attackerId, int defenderId, int damage, boolean miss, boolean critical, boolean soulshot, int x, int y, int z)
	{
		_attackerId = attackerId;
		_defenderId = defenderId;
		_damage = damage;
		_miss = miss;
		_critical = critical;
		_soulshot = soulshot;
		_x = x;
		_y = y;
		_z = z;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x06);
		writeD(_attackerId);
		writeD(_defenderId);
		writeD(_damage);
		int flags = 0;
		if (_soulshot)
		{
			flags |= 0x10;
		}
		if (_critical)
		{
			flags |= 0x20;
		}
		if (_miss)
		{
			flags |= 0x80;
		}
		writeC(flags);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeH(0);
	}
}
