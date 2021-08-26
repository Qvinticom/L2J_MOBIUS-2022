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
package org.l2jmobius.gameserver.network.serverpackets.homunculus;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.homunculus.Homunculus;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

/**
 * @author Mobius
 */
public class ExShowHomunculusList implements IClientOutgoingPacket
{
	private final PlayerInstance _player;
	
	public ExShowHomunculusList(PlayerInstance player)
	{
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_SHOW_HOMUNCULUS_LIST.writeId(packet);
		
		if (_player.getHomunculusList().size() > 0)
		{
			packet.writeD(_player.getHomunculusList().size()); // homunculus count
			int counter = 0;
			for (int i = 0; i < Config.MAX_HOMUNCULUS_COUNT; i++)
			{
				final Homunculus homunculus = _player.getHomunculusList().get(i);
				if (homunculus == null)
				{
					continue;
				}
				
				packet.writeD(counter); // slot
				packet.writeD(homunculus.getId()); // homunculus id
				packet.writeD(homunculus.getType());
				packet.writeC(homunculus.isActive() ? 1 : 0);
				packet.writeD(homunculus.getTemplate().getBasicSkillId());
				packet.writeD(homunculus.getSkillLevel1() > 0 ? homunculus.getTemplate().getSkillId1() : 0);
				packet.writeD(homunculus.getSkillLevel2() > 0 ? homunculus.getTemplate().getSkillId2() : 0);
				packet.writeD(homunculus.getSkillLevel3() > 0 ? homunculus.getTemplate().getSkillId3() : 0);
				packet.writeD(homunculus.getSkillLevel4() > 0 ? homunculus.getTemplate().getSkillId4() : 0);
				packet.writeD(homunculus.getSkillLevel5() > 0 ? homunculus.getTemplate().getSkillId5() : 0);
				packet.writeD(homunculus.getTemplate().getBasicSkillLevel());
				packet.writeD(homunculus.getSkillLevel1());
				packet.writeD(homunculus.getSkillLevel2());
				packet.writeD(homunculus.getSkillLevel3());
				packet.writeD(homunculus.getSkillLevel4());
				packet.writeD(homunculus.getSkillLevel5());
				packet.writeD(homunculus.getLevel());
				packet.writeD(homunculus.getExp());
				packet.writeD(homunculus.getHp());
				packet.writeD(homunculus.getAtk());
				packet.writeD(homunculus.getDef());
				packet.writeD(homunculus.getCritRate());
				counter++;
			}
		}
		else
		{
			packet.writeD(0);
		}
		
		return true;
	}
}
