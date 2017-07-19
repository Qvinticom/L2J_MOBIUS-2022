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

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import javolution.util.FastList;

/**
 * This class ...
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 * @author godson
 */
public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private static final String _S__FE_2A_OLYMPIADSPELLEDINFO = "[S] FE:2A ExOlympiadSpelledInfo";
	private final L2PcInstance _player;
	private final List<Effect> _effects;
	private int _extraSlot = 0;
	
	class Effect
	{
		protected int skillId;
		protected int dat;
		protected int duration;
		
		public Effect(int pSkillId, int pDat, int pDuration)
		{
			skillId = pSkillId;
			dat = pDat;
			duration = pDuration;
		}
	}
	
	public ExOlympiadSpelledInfo(L2PcInstance player)
	{
		_effects = new FastList<>();
		_player = player;
	}
	
	public void addEffect(int skillId, int dat, int duration)
	{
		// override slots if effects exceed 30 :)
		// It's better than reaching 31 effects and breaking etc slots
		// Might be very useful for toggles, since they are switchable
		if (_effects.size() > 29)
		{
			_effects.set(_extraSlot++, new Effect(skillId, dat, duration));
		}
		else
		{
			_effects.add(new Effect(skillId, dat, duration));
		}
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
		
		for (final Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.duration / 1000);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _S__FE_2A_OLYMPIADSPELLEDINFO;
	}
}