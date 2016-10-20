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
package com.l2jmobius.gameserver.network.clientpackets.ensoul;

import com.l2jmobius.gameserver.model.holders.SkillHolder;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Mathael
 */
public class SoulCrystalOption
{
	private int _objectId;
	private int _effect;
	private boolean _special;
	private int _slot;
	private SkillHolder _skill;
	
	public SoulCrystalOption(int effect, boolean special, SkillHolder skill)
	{
		setSoulCrystalObjectId(0);
		setSpecial(special);
		setEffect(effect);
		setSkill(skill);
	}
	
	public int getSoulCrystalObjectId()
	{
		return _objectId;
	}
	
	public void setSoulCrystalObjectId(int soulCrystalObjectId)
	{
		_objectId = soulCrystalObjectId;
	}
	
	public int getEffect()
	{
		return _effect;
	}
	
	private void setEffect(int effect)
	{
		_effect = effect;
	}
	
	public void setSlot(int slot)
	{
		_slot = slot;
	}
	
	public int getSlot()
	{
		return _slot;
	}
	
	public boolean isSpecial()
	{
		return _special;
	}
	
	public void setSpecial(boolean special)
	{
		_special = special;
	}
	
	public SkillHolder getSkillHolder()
	{
		return _skill;
	}
	
	public Skill getSkill()
	{
		return _skill.getSkill();
	}
	
	private void setSkill(SkillHolder skill)
	{
		_skill = skill;
	}
}
