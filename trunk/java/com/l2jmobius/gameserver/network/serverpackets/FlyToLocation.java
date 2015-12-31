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

import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.interfaces.ILocational;

/**
 * @author KenM
 */
public final class FlyToLocation extends L2GameServerPacket
{
	private final int _destX, _destY, _destZ;
	private final int _chaObjId, _chaX, _chaY, _chaZ;
	private final FlyType _type;
	private int _flySpeed;
	private int _flyDelay;
	private int _animationSpeed;
	
	public enum FlyType
	{
		THROW_UP,
		THROW_HORIZONTAL,
		DUMMY,
		CHARGE,
		PUSH_HORIZONTAL,
		JUMP_EFFECTED,
		NOT_USED,
		PUSH_DOWN_HORIZONTAL,
		WARP_BACK,
		WARP_FORWARD;
	}
	
	public FlyToLocation(L2Character cha, int destX, int destY, int destZ, FlyType type)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
	}
	
	public FlyToLocation(L2Character cha, int destX, int destY, int destZ, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		_chaObjId = cha.getObjectId();
		_chaX = cha.getX();
		_chaY = cha.getY();
		_chaZ = cha.getZ();
		_destX = destX;
		_destY = destY;
		_destZ = destZ;
		_type = type;
		_flySpeed = flySpeed;
		_flyDelay = flyDelay;
		_animationSpeed = animationSpeed;
	}
	
	public FlyToLocation(L2Character cha, ILocational dest, FlyType type)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type);
	}
	
	public FlyToLocation(L2Character cha, ILocational dest, FlyType type, int flySpeed, int flyDelay, int animationSpeed)
	{
		this(cha, dest.getX(), dest.getY(), dest.getZ(), type, flySpeed, flyDelay, animationSpeed);
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xD4);
		writeD(_chaObjId);
		writeD(_destX);
		writeD(_destY);
		writeD(_destZ);
		writeD(_chaX);
		writeD(_chaY);
		writeD(_chaZ);
		writeD(_type.ordinal());
		writeD(_flySpeed);
		writeD(_flyDelay);
		writeD(_animationSpeed);
	}
}
