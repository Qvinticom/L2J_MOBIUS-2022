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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.network.client.OutgoingPackets;

/**
 * @author Sdw
 */
public class ExMagicAttackInfo implements IClientOutgoingPacket
{
	// TODO: Enum
	public final static int CRITICAL = 1;
	public final static int CRITICAL_HEAL = 2;
	public final static int OVERHIT = 3;
	public final static int EVADED = 4;
	public final static int BLOCKED = 5;
	public final static int RESISTED = 6;
	public final static int IMMUNE = 7;
	public final static int IMMUNE2 = 8;
	
	private final int _caster;
	private final int _target;
	private final int _type;
	
	public ExMagicAttackInfo(int caster, int target, int type)
	{
		_caster = caster;
		_target = target;
		_type = type;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_MAGIC_ATTACK_INFO.writeId(packet);
		
		packet.writeD(_caster);
		packet.writeD(_target);
		packet.writeD(_type);
		return true;
	}
}