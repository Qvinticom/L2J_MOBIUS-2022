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

import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import org.l2jmobius.gameserver.model.SkillLearn;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.ItemHolder;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Sdw, Mobius
 * @version Classic 2.0
 */
public class AcquireSkillList implements IClientOutgoingPacket
{
	private PlayerInstance _player;
	private List<SkillLearn> _learnable;
	
	public AcquireSkillList(PlayerInstance player)
	{
		if (!player.isSubclassLocked()) // Changing class.
		{
			_player = player;
			_learnable = SkillTreesData.getInstance().getAvailableSkills(player, player.getClassId(), false, false);
			_learnable.addAll(SkillTreesData.getInstance().getNextAvailableSkills(player, player.getClassId(), false, false));
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player == null)
		{
			return false;
		}
		
		OutgoingPackets.ACQUIRE_SKILL_LIST.writeId(packet);
		
		packet.writeH(_learnable.size());
		for (SkillLearn skill : _learnable)
		{
			if (skill == null)
			{
				continue;
			}
			packet.writeD(skill.getSkillId());
			packet.writeH(skill.getSkillLevel());
			packet.writeQ(skill.getLevelUpSp());
			packet.writeC(skill.getGetLevel());
			packet.writeC(0x00);
			if (skill.getRequiredItems().size() > 0)
			{
				for (ItemHolder item : skill.getRequiredItems())
				{
					packet.writeC(0x01);
					packet.writeD(item.getId());
					packet.writeQ(item.getCount());
				}
			}
			else
			{
				packet.writeC(0x00);
			}
			packet.writeC(0x00);
		}
		return true;
	}
}
