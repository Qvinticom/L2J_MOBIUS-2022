/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

/**
 * @author Sdw
 */
public class ExMagicAttackInfo extends L2GameServerPacket
{
	public final static int CRITICAL = 1;
	public final static int CRITICAL_HEAL = 2;
	public final static int OVERHIT = 3;
	public final static int EVADED = 4;
	public final static int BLOCKED = 5;
	public final static int RESISTED = 6;
	public final static int IMMUNE = 7;
	
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
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xFB);
		writeD(_caster);
		writeD(_target);
		writeD(_type);
	}
}