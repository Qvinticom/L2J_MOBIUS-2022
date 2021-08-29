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
 **/
public class HomunculusTemplate
{
	private final int _id;
	private final int _type;
	private final int _basicSkillId;
	private final int _basicSkillLevel;
	private final int _skillId1;
	private final int _skillId2;
	private final int _skillId3;
	private final int _skillId4;
	private final int _skillId5;
	private final int _hpLevel1;
	private final int _atkLevel1;
	private final int _defLevel1;
	private final int _expToLevel2;
	private final int _hpLevel2;
	private final int _atkLevel2;
	private final int _defLevel2;
	private final int _expToLevel3;
	private final int _hpLevel3;
	private final int _atkLevel3;
	private final int _defLevel3;
	private final int _expToLevel4;
	private final int _hpLevel4;
	private final int _atkLevel4;
	private final int _defLevel4;
	private final int _expToLevel5;
	private final int _hpLevel5;
	private final int _atkLevel5;
	private final int _defLevel5;
	private final int _expToLevel6;
	private final int _critRate;
	
	public HomunculusTemplate(int id, int type, int basicSkillId, int basicSkillLevel, int skillId1, int skillId2, int skillId3, int skillId4, int skillId5, int hpLevel1, int atkLevel1, int defLevel1, int expToLevel2, int hpLevel2, int atkLevel2, int defLevel2, int expToLevel3, int hpLevel3, int atkLevel3, int defLevel3, int expToLevel4, int hpLevel4, int atkLevel4, int defLevel4, int expToLevel5, int hpLevel5, int atkLevel5, int defLevel5, int expToLevel6, int critRate)
	{
		_id = id;
		_type = type;
		_basicSkillId = basicSkillId;
		_basicSkillLevel = basicSkillLevel;
		_skillId1 = skillId1;
		_skillId2 = skillId2;
		_skillId3 = skillId3;
		_skillId4 = skillId4;
		_skillId5 = skillId5;
		_hpLevel1 = hpLevel1;
		_atkLevel1 = atkLevel1;
		_defLevel1 = defLevel1;
		_expToLevel2 = expToLevel2;
		_hpLevel2 = hpLevel2;
		_atkLevel2 = atkLevel2;
		_defLevel2 = defLevel2;
		_expToLevel3 = expToLevel3;
		_hpLevel3 = hpLevel3;
		_atkLevel3 = atkLevel3;
		_defLevel3 = defLevel3;
		_expToLevel4 = expToLevel4;
		_hpLevel4 = hpLevel4;
		_atkLevel4 = atkLevel4;
		_defLevel4 = defLevel4;
		_expToLevel5 = expToLevel5;
		_hpLevel5 = hpLevel5;
		_atkLevel5 = atkLevel5;
		_defLevel5 = defLevel5;
		_expToLevel6 = expToLevel6;
		_critRate = critRate;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public int getType()
	{
		return _type;
	}
	
	public int getBasicSkillId()
	{
		return _basicSkillId;
	}
	
	public int getBasicSkillLevel()
	{
		return _basicSkillLevel;
	}
	
	public int getSkillId1()
	{
		return _skillId1;
	}
	
	public int getSkillId2()
	{
		return _skillId2;
	}
	
	public int getSkillId3()
	{
		return _skillId3;
	}
	
	public int getSkillId4()
	{
		return _skillId4;
	}
	
	public int getSkillId5()
	{
		return _skillId5;
	}
	
	public int getHpLevel1()
	{
		return _hpLevel1;
	}
	
	public int getHpLevel2()
	{
		return _hpLevel2;
	}
	
	public int getHpLevel3()
	{
		return _hpLevel3;
	}
	
	public int getHpLevel4()
	{
		return _hpLevel4;
	}
	
	public int getHpLevel5()
	{
		return _hpLevel5;
	}
	
	public int getAtkLevel1()
	{
		return _atkLevel1;
	}
	
	public int getAtkLevel2()
	{
		return _atkLevel2;
	}
	
	public int getAtkLevel3()
	{
		return _atkLevel3;
	}
	
	public int getAtkLevel4()
	{
		return _atkLevel4;
	}
	
	public int getAtkLevel5()
	{
		return _atkLevel5;
	}
	
	public int getDefLevel1()
	{
		return _defLevel1;
	}
	
	public int getDefLevel2()
	{
		return _defLevel2;
	}
	
	public int getDefLevel3()
	{
		return _defLevel3;
	}
	
	public int getDefLevel4()
	{
		return _defLevel4;
	}
	
	public int getDefLevel5()
	{
		return _defLevel5;
	}
	
	public int getExpToLevel2()
	{
		return _expToLevel2;
	}
	
	public int getExpToLevel3()
	{
		return _expToLevel3;
	}
	
	public int getExpToLevel4()
	{
		return _expToLevel4;
	}
	
	public int getExpToLevel5()
	{
		return _expToLevel5;
	}
	
	public int getExpToLevel6()
	{
		return _expToLevel6;
	}
	
	public int getCritRate()
	{
		return _critRate;
	}
}