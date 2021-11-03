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
package org.l2jmobius.gameserver.model.holders;

/**
 * @author JoeAlisson
 */
public class ElementalSpiritDataHolder
{
	private int _charId;
	private int _level = 1;
	private byte _type;
	private byte _stage = 1;
	private long _experience;
	private byte _attackPoints;
	private byte _defensePoints;
	private byte _critRatePoints;
	private byte _critDamagePoints;
	private boolean _inUse;
	
	public ElementalSpiritDataHolder()
	{
	}
	
	public ElementalSpiritDataHolder(byte type, int objectId)
	{
		_charId = objectId;
		_type = type;
	}
	
	public int getCharId()
	{
		return _charId;
	}
	
	public void setCharId(int charId)
	{
		_charId = charId;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public byte getType()
	{
		return _type;
	}
	
	public void setType(byte type)
	{
		_type = type;
	}
	
	public byte getStage()
	{
		return _stage;
	}
	
	public void setStage(byte stage)
	{
		_stage = stage;
	}
	
	public long getExperience()
	{
		return _experience;
	}
	
	public void setExperience(long experience)
	{
		_experience = experience;
	}
	
	public byte getAttackPoints()
	{
		return _attackPoints;
	}
	
	public void setAttackPoints(byte attackPoints)
	{
		_attackPoints = attackPoints;
	}
	
	public byte getDefensePoints()
	{
		return _defensePoints;
	}
	
	public void setDefensePoints(byte defensePoints)
	{
		_defensePoints = defensePoints;
	}
	
	public byte getCritRatePoints()
	{
		return _critRatePoints;
	}
	
	public void setCritRatePoints(byte critRatePoints)
	{
		_critRatePoints = critRatePoints;
	}
	
	public byte getCritDamagePoints()
	{
		return _critDamagePoints;
	}
	
	public void setCritDamagePoints(byte critDamagePoints)
	{
		_critDamagePoints = critDamagePoints;
	}
	
	public void addExperience(long experience)
	{
		_experience += experience;
	}
	
	public void increaseLevel()
	{
		_level++;
	}
	
	public boolean isInUse()
	{
		return _inUse;
	}
	
	public void setInUse(boolean value)
	{
		_inUse = value;
	}
	
	public void addAttackPoints(byte attackPoints)
	{
		_attackPoints += attackPoints;
	}
	
	public void addDefensePoints(byte defensePoints)
	{
		_defensePoints += defensePoints;
	}
	
	public void addCritRatePoints(byte critRatePoints)
	{
		_critRatePoints = critRatePoints;
	}
	
	public void addCritDamagePoints(byte critDamagePoints)
	{
		_critDamagePoints += critDamagePoints;
	}
	
	public void increaseStage()
	{
		_stage++;
	}
}
