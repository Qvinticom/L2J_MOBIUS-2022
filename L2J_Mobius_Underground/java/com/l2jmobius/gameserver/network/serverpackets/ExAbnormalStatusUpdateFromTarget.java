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

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.OutgoingPackets;

public class ExAbnormalStatusUpdateFromTarget implements IClientOutgoingPacket
{
	private final L2Character _character;
	private List<Effect> _effects = new ArrayList<>();
	
	private static class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _subLevel;
		protected int _abnormalType;
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
			_subLevel = skill.getSubLevel();
			_abnormalType = skill.getAbnormalType().getClientId();
			_duration = skill.isAura() ? -1 : info.getTime();
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
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_ABNORMAL_STATUS_UPDATE_FROM_TARGET.writeId(packet);
		
		packet.writeD(_character.getObjectId());
		packet.writeH(_effects.size());
		
		for (Effect info : _effects)
		{
			packet.writeD(info._skillId);
			packet.writeH(info._level);
			packet.writeH(info._subLevel);
			packet.writeH(info._abnormalType);
			writeOptionalD(packet, info._duration);
			packet.writeD(info._caster);
		}
		return true;
	}
}
