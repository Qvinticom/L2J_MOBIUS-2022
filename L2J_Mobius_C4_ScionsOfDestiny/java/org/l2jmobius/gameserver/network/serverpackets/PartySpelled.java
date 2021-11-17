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
package org.l2jmobius.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.Pet;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.3.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class PartySpelled implements IClientOutgoingPacket
{
	private final List<Effect> _effects;
	private final Creature _creature;
	
	private class Effect
	{
		protected int _skillId;
		protected int _dat;
		protected int _duration;
		
		public Effect(int pSkillId, int pDat, int pDuration)
		{
			_skillId = pSkillId;
			_dat = pDat;
			_duration = pDuration;
		}
	}
	
	public PartySpelled(Creature creature)
	{
		_effects = new ArrayList<>();
		_creature = creature;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_creature == null)
		{
			return false;
		}
		OutgoingPackets.PARTY_SPELLED.writeId(packet);
		packet.writeD(_creature instanceof Servitor ? 2 : _creature instanceof Pet ? 1 : 0);
		packet.writeD(_creature.getObjectId());
		
		// C4 does not support more than 20 effects in party window, so limiting them makes no difference.
		// This check ignores first effects, so there is space for last effects to be viewable by party members.
		// It may also help healers be aware of cursed members.
		int size = 0;
		if (_effects.size() > 20)
		{
			packet.writeD(20);
			size = _effects.size() - 20;
		}
		else
		{
			packet.writeD(_effects.size());
		}
		
		for (; size < _effects.size(); size++)
		{
			final Effect temp = _effects.get(size);
			if (temp == null)
			{
				continue;
			}
			
			packet.writeD(temp._skillId);
			packet.writeH(temp._dat);
			packet.writeD(temp._duration / 1000);
		}
		return true;
	}
	
	public void addPartySpelledEffect(int skillId, int dat, int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}
}
