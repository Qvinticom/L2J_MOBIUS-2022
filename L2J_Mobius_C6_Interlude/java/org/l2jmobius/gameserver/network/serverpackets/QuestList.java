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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class QuestList implements IClientOutgoingPacket
{
	private final Collection<QuestState> _questStates;
	
	public QuestList(Player player)
	{
		_questStates = player.getAllQuestStates();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.QUEST_LIST.writeId(packet);
		packet.writeH(_questStates.size());
		for (QuestState qs : _questStates)
		{
			packet.writeD(qs.getQuest().getQuestId());
			
			final int states = qs.getInt("__compltdStateFlags");
			if (states != 0)
			{
				packet.writeD(states);
			}
			else
			{
				packet.writeD(qs.getCond());
			}
		}
		return true;
	}
}
