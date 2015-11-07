/*
 * Copyright (C) 2004-2015 L2J Server
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

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;

public class ExAbnormalStatusUpdateFromTarget extends L2GameServerPacket
{
	private final L2Character _character;
	private List<Effect> _effects = new ArrayList<>();
	
	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _maxlevel;
		protected int _duration;
		protected int _caster;
		
		public Effect(BuffInfo info)
		{
			final Skill skill = info.getSkill();
			final L2Character caster = info.getEffector();
			int casterId = 0;
			if (caster != null)
			{
				casterId = caster.getObjectId();
			}
			
			_skillId = skill.getDisplayId();
			_level = skill.getDisplayLevel();
			_maxlevel = SkillData.getInstance().getMaxLevel(_skillId);
			_duration = info.getTime();
			_caster = casterId;
		}
	}
	
	public ExAbnormalStatusUpdateFromTarget(L2Character character)
	{
		_character = character;
		_effects = new ArrayList<>();
		
		for (BuffInfo info : character.getEffectList().getEffects())
		{
			if ((info != null) && info.isInUse())
			{
				final Skill skill = info.getSkill();
				
				// TODO: Check on retail if all effects should be displayed
				if (skill != null)
				{
					_effects.add(new Effect(info));
				}
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xE6);
		
		writeD(_character.getObjectId());
		writeH(_effects.size());
		
		for (Effect info : _effects)
		{
			writeD(info._skillId);
			if (info._level < 100)
			{
				writeH(info._level);
				writeH(0x00);
			}
			else
			{
				writeH(info._maxlevel);
				writeH(info._level);
			}
			writeH(0x00); // Combo abnormal ?
			writeH(info._duration);
			writeD(info._caster);
		}
	}
}
