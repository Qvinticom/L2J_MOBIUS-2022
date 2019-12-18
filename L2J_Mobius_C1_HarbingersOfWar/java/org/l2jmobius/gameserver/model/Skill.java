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
package org.l2jmobius.gameserver.model;

public class Skill
{
	public static final int OP_ALWAYS = 1;
	public static final int OP_ONCE = 2;
	public static final int OP_DURATION = 3;
	public static final int OP_TOGGLE = 4;
	public static final int TARGET_SELF = 0;
	public static final int TARGET_ONE = 1;
	public static final int TARGET_PARTY = 2;
	public static final int TARGET_CLAN = 3;
	public static final int TARGET_PET = 4;
	public static final int TARGET_ENEMY = 5;
	public static final int TARGET_FRIEND = 6;
	
	private int _id;
	private int _level;
	private String _name;
	private int _operateType;
	private boolean _magic;
	private int _mpConsume;
	private int _hpConsume;
	private int _itemConsume;
	private int _itemConsumeId;
	private int _castRange;
	private int _skillTime;
	private int _hitTime;
	private int _reuseDelay;
	private int _buffDuration;
	private int _targetType;
	private int _power;
	
	public int getPower()
	{
		return _power;
	}
	
	public void setPower(int power)
	{
		_power = power;
	}
	
	public int getBuffDuration()
	{
		return _buffDuration;
	}
	
	public void setBuffDuration(int buffDuration)
	{
		_buffDuration = buffDuration;
	}
	
	public int getCastRange()
	{
		return _castRange;
	}
	
	public void setCastRange(int castRange)
	{
		_castRange = castRange;
	}
	
	public int getHitTime()
	{
		return _hitTime;
	}
	
	public void setHitTime(int hitTime)
	{
		_hitTime = hitTime;
	}
	
	public int getHpConsume()
	{
		return _hpConsume;
	}
	
	public void setHpConsume(int hpConsume)
	{
		_hpConsume = hpConsume;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public void setId(int id)
	{
		_id = id;
	}
	
	public int getItemConsume()
	{
		return _itemConsume;
	}
	
	public void setItemConsume(int itemConsume)
	{
		_itemConsume = itemConsume;
	}
	
	public int getItemConsumeId()
	{
		return _itemConsumeId;
	}
	
	public void setItemConsumeId(int itemConsumeId)
	{
		_itemConsumeId = itemConsumeId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public boolean isMagic()
	{
		return _magic;
	}
	
	public void setMagic(boolean magic)
	{
		_magic = magic;
	}
	
	public int getMpConsume()
	{
		return _mpConsume;
	}
	
	public void setMpConsume(int mpConsume)
	{
		_mpConsume = mpConsume;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String name)
	{
		_name = name;
	}
	
	public int getOperateType()
	{
		return _operateType;
	}
	
	public void setOperateType(int operateType)
	{
		_operateType = operateType;
	}
	
	public int getReuseDelay()
	{
		return _reuseDelay;
	}
	
	public void setReuseDelay(int reuseDelay)
	{
		_reuseDelay = reuseDelay;
	}
	
	public int getSkillTime()
	{
		return _skillTime;
	}
	
	public void setSkillTime(int skillTime)
	{
		_skillTime = skillTime;
	}
	
	public int getTargetType()
	{
		return _targetType;
	}
	
	public void setTargetType(int targetType)
	{
		_targetType = targetType;
	}
	
	public boolean isPassive()
	{
		return _operateType == OP_ALWAYS;
	}
}
