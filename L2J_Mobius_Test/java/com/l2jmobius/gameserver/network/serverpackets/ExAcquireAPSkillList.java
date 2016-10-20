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

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.xml.impl.AbilityPointsData;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * @author Sdw
 */
public class ExAcquireAPSkillList extends L2GameServerPacket
{
	private final int _abilityPoints, _usedAbilityPoints;
	private final long _price;
	private final boolean _enable;
	private final List<Skill> _skills = new ArrayList<>();
	
	public ExAcquireAPSkillList(L2PcInstance activeChar)
	{
		_abilityPoints = activeChar.getAbilityPoints();
		_usedAbilityPoints = activeChar.getAbilityPointsUsed();
		_price = AbilityPointsData.getInstance().getPrice(_abilityPoints);
		for (L2SkillLearn sk : SkillTreesData.getInstance().getAbilitySkillTree().values())
		{
			final Skill knownSkill = activeChar.getKnownSkill(sk.getSkillId());
			if ((knownSkill != null) && (knownSkill.getLevel() == sk.getSkillLevel()))
			{
				_skills.add(knownSkill);
			}
		}
		_enable = !activeChar.isSubClassActive() && (activeChar.getLevel() >= 99) && activeChar.isNoble();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x15F);
		
		writeD(_enable ? 1 : 0);
		writeQ(Config.ABILITY_POINTS_RESET_ADENA);
		writeQ(_price);
		writeD(Config.ABILITY_MAX_POINTS);
		writeD(_abilityPoints);
		writeD(_usedAbilityPoints);
		writeD(_skills.size());
		for (Skill skill : _skills)
		{
			writeD(skill.getId());
			writeD(skill.getLevel());
		}
	}
}