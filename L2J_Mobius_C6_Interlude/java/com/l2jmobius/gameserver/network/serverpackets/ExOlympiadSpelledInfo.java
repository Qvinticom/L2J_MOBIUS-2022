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

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author godson
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private final L2PcInstance _player;
	private final List<Effect> _effects;
	
	private class Effect
	{
		protected int _skillId;
		protected int _level;
		protected int _duration;
		
		public Effect(int pSkillId, int pLevel, int pDuration)
		{
			_skillId = pSkillId;
			_level = pLevel;
			_duration = pDuration;
		}
	}
	
	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_effects = new ArrayList<>();
		_player = player;
	}
	
	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	protected final void writeImpl()
	{
		if (_player == null)
		{
			return;
		}
		writeC(0xfe);
		writeH(0x2a);
		writeD(_player.getObjectId());
		writeD(_effects.size());
		for (Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration / 1000);
		}
	}
}
