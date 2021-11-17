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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;

public class RequestQuestAbort implements IClientIncomingPacket
{
	private int _questId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_questId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		Quest qe = null;
		if (!Config.ALT_DEV_NO_QUESTS)
		{
			qe = QuestManager.getInstance().getQuest(_questId);
		}
		
		if (qe != null)
		{
			if ((_questId == 503) && (player.getClan() != null) && player.isClanLeader())
			{
				qe.finishQuestToClan(player);
			}
			
			final QuestState qs = player.getQuestState(qe.getName());
			if (qs != null)
			{
				qs.exitQuest(true);
				player.sendMessage("Quest aborted.");
				player.sendPacket(new QuestList(player));
			}
		}
	}
}
