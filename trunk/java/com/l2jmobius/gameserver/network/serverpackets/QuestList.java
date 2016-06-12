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

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.client.OutgoingPackets;

public class QuestList implements IClientOutgoingPacket
{
	private final List<Quest> _quests;
	private final L2PcInstance _player;
	
	public QuestList(L2PcInstance player)
	{
		_player = player;
		_quests = player.getAllActiveQuests();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.QUEST_LIST.writeId(packet);
		packet.writeH(_quests.size());
		for (Quest quest : _quests)
		{
			packet.writeD(quest.getId());
			
			final QuestState qs = _player.getQuestState(quest.getName());
			if (qs == null)
			{
				packet.writeD(0);
				continue;
			}
			
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
		
		final byte[] oneTimeQuestMask = new byte[128];
		for (QuestState questState : _player.getAllQuestStates())
		{
			if (questState.isCompleted())
			{
				final int questId = questState.getQuest().getId();
				if ((questId < 0) || ((questId > 255) && (questId < 10256)) || (questId > 11023))
				{
					continue;
				}
				
				oneTimeQuestMask[(questId % 10000) / 8] |= 1 << (questId % 8);
			}
		}
		packet.writeB(oneTimeQuestMask);
		
		return true;
	}
}
