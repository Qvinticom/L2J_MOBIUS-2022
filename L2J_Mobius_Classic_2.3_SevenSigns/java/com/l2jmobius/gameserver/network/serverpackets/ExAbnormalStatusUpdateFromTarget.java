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
import java.util.stream.Collectors;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author proGenitor <br>
 *         Experimental packet compatible for L2Classic 2.0.
 */
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
			final L2Character caster = info.getEffector();
			int casterId = 0;
			if (caster != null)
			{
				casterId = caster.getObjectId();
			}
			_caster = casterId;
		}
	}
	
	public ExAbnormalStatusUpdateFromTarget(L2Character character)
	{
		//@formatter:off
		_character = character;
		_effects = character.getEffectList().getEffects()
					.stream()
					.map(Effect::new)
					.collect(Collectors.toList());
		//@formatter:on
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
