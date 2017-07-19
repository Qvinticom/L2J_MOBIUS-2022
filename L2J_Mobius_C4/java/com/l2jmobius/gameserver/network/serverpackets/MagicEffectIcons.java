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

import java.util.List;

import javolution.util.FastList;

/**
 * MagicEffectIcons format h (dhd)
 * @version $Revision: 1.3.2.1.2.6 $ $Date: 2005/04/05 19:41:08 $
 */
public class MagicEffectIcons extends L2GameServerPacket
{
	private static final String _S__97_MAGICEFFECTICONS = "[S] 7f MagicEffectIcons";
	public List<Effect> _effects;
	private int _extraSlot = 0;
	
	class Effect
	{
		int _skillId;
		int _level;
		int _duration;
		
		public Effect(int skillId, int level, int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;
		}
	}
	
	public MagicEffectIcons()
	{
		_effects = new FastList<>();
	}
	
	public void addEffect(int skillId, int level, int duration)
	{
		// override slots if effects exceed 30 :)
		// It's better than reaching 31 effects and breaking etc slots
		// Might be very useful for toggles, since they are switchable
		if (_effects.size() > 29)
		{
			_effects.set(_extraSlot++, new Effect(skillId, level, duration));
		}
		else
		{
			_effects.add(new Effect(skillId, level, duration));
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x7f);
		writeH(_effects.size());
		
		for (final Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			
			if (temp._duration == -1)
			{
				writeD(-1);
			}
			else
			{
				writeD(temp._duration / 1000);
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__97_MAGICEFFECTICONS;
	}
}