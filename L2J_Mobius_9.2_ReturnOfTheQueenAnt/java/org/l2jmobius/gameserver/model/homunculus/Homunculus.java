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
package org.l2jmobius.gameserver.model.homunculus;

/**
 * @author nexvill
 */
public class Homunculus implements Comparable<Homunculus>
{
	private final HomunculusTemplate _template;
	private int _slot;
	private int _level;
	private int _exp;
	private int _skillLevel1;
	private int _skillLevel2;
	private int _skillLevel3;
	private int _skillLevel4;
	private int _skillLevel5;
	private boolean _isActive;
	
	public Homunculus(HomunculusTemplate template, int slot, int level, int exp, int skillLevel1, int skillLevel2, int skillLevel3, int skillLevel4, int skillLevel5, boolean isActive)
	{
		_template = template;
		_slot = slot;
		_level = level;
		_exp = exp;
		_skillLevel1 = skillLevel1;
		_skillLevel2 = skillLevel2;
		_skillLevel3 = skillLevel3;
		_skillLevel4 = skillLevel4;
		_skillLevel5 = skillLevel5;
		_isActive = isActive;
	}
	
	public HomunculusTemplate getTemplate()
	{
		return _template;
	}
	
	public int getId()
	{
		return _template.getId();
	}
	
	public int getType()
	{
		return _template.getType();
	}
	
	public void setSlot(int slot)
	{
		_slot = slot;
	}
	
	public int getSlot()
	{
		return _slot;
	}
	
	public void setLevel(int level)
	{
		_level = level;
	}
	
	public int getLevel()
	{
		return _level;
	}
	
	public void setExp(int exp)
	{
		_exp = exp;
	}
	
	public int getExp()
	{
		return _exp;
	}
	
	public void setSkillLevel1(int level)
	{
		_skillLevel1 = level;
	}
	
	public int getSkillLevel1()
	{
		return _skillLevel1;
	}
	
	public void setSkillLevel2(int level)
	{
		_skillLevel2 = level;
	}
	
	public int getSkillLevel2()
	{
		return _skillLevel2;
	}
	
	public void setSkillLevel3(int level)
	{
		_skillLevel3 = level;
	}
	
	public int getSkillLevel3()
	{
		return _skillLevel3;
	}
	
	public void setSkillLevel4(int level)
	{
		_skillLevel4 = level;
	}
	
	public int getSkillLevel4()
	{
		return _skillLevel4;
	}
	
	public void setSkillLevel5(int level)
	{
		_skillLevel5 = level;
	}
	
	public int getSkillLevel5()
	{
		return _skillLevel5;
	}
	
	public int getHp()
	{
		switch (_level)
		{
			case 1:
			{
				return _template.getHpLevel1();
			}
			case 2:
			{
				return _template.getHpLevel2();
			}
			case 3:
			{
				return _template.getHpLevel3();
			}
			case 4:
			{
				return _template.getHpLevel4();
			}
			case 5:
			{
				return _template.getHpLevel5();
			}
		}
		return _template.getHpLevel1();
	}
	
	public int getAtk()
	{
		switch (_level)
		{
			case 1:
			{
				return _template.getAtkLevel1();
			}
			case 2:
			{
				return _template.getAtkLevel2();
			}
			case 3:
			{
				return _template.getAtkLevel3();
			}
			case 4:
			{
				return _template.getAtkLevel4();
			}
			case 5:
			{
				return _template.getAtkLevel5();
			}
		}
		return _template.getAtkLevel1();
	}
	
	public int getDef()
	{
		switch (_level)
		{
			case 1:
			{
				return _template.getDefLevel1();
			}
			case 2:
			{
				return _template.getDefLevel2();
			}
			case 3:
			{
				return _template.getDefLevel3();
			}
			case 4:
			{
				return _template.getDefLevel4();
			}
			case 5:
			{
				return _template.getDefLevel5();
			}
		}
		return _template.getDefLevel1();
	}
	
	public int getCritRate()
	{
		return _template.getCritRate();
	}
	
	public void setActive(boolean active)
	{
		_isActive = active;
	}
	
	public boolean isActive()
	{
		return _isActive;
	}
	
	@Override
	public String toString()
	{
		return "Homunculus[id=" + _template.getId() + ", isActive=" + _isActive + "]";
	}
	
	@Override
	public int compareTo(Homunculus o)
	{
		return getId() - o.getId();
	}
}