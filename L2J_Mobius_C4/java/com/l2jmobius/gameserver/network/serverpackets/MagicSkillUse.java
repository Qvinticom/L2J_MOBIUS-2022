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

import com.l2jmobius.gameserver.model.L2Character;

/**
 * sample 0000: 5a d8 a8 10 48 d8 a8 10 48 10 04 00 00 01 00 00 Z...H...H....... 0010: 00 f0 1a 00 00 68 28 00 00 .....h(.. format dddddd dddh (h)
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class MagicSkillUse extends L2GameServerPacket
{
	private static final String _S__5A_MAGICSKILLUSER = "[S] 5A MagicSkillUse";
	private final int _targetId;
	private int _skillId;
	private final int _skillLevel;
	private final int _hitTime;
	private final int _reuseDelay;
	private final int _chaId, _x, _y, _z, _targetx, _targety, _targetz;
	private final boolean _crit;
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		this(cha, target, skillId, skillLevel, hitTime, reuseDelay, false);
	}
	
	public MagicSkillUse(L2Character cha, L2Character target, int skillId, int skillLevel, int hitTime, int reuseDelay, boolean crit)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		
		// fake death client fix
		if (skillId == 60)
		{
			_skillId = 8;
		}
		else
		{
			_skillId = skillId;
		}
		
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
		
		_crit = crit;
	}
	
	public MagicSkillUse(L2Character cha, int skillId, int skillLevel, int hitTime, int reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = cha.getX();
		_targety = cha.getY();
		_targetz = cha.getZ();
		
		_crit = false;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(_chaId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD(_reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		
		// Critical Attack Sound & Animation
		if (_crit)
		{
			writeD(0x01);
			writeH(0x00);
		}
		else
		{
			writeD(0x00);
		}
		
		writeD(_targetx);
		writeD(_targety);
		writeD(_targetz);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__5A_MAGICSKILLUSER;
	}
}