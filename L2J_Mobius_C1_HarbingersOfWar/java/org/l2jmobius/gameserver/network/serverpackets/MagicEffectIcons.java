/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.Vector;

public class MagicEffectIcons extends ServerBasePacket
{
	private static final String _S__97_MAGICEFFECTICONS = "[S] 97 MagicEffectIcons";
	private final Vector<Effect> _effects = new Vector<>();
	
	public void addEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(151);
		writeH(_effects.size());
		for (int i = 0; i < _effects.size(); ++i)
		{
			Effect temp = _effects.get(i);
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.duration / 1000);
		}
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__97_MAGICEFFECTICONS;
	}
	
	class Effect
	{
		int skillId;
		int dat;
		int duration;
		
		public Effect(int skillId, int dat, int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.duration = duration;
		}
	}
	
}
