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
import org.l2jmobius.gameserver.instancemanager.QuestManager;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.QuestList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestQuestAbort extends GameClientPacket
{
	private int _questId;
	
	@Override
	protected void readImpl()
	{
		_questId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final PlayerInstance player = getClient().getPlayer();
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
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
				sm.addString("Quest aborted.");
				player.sendPacket(sm);
				final QuestList ql = new QuestList();
				player.sendPacket(ql);
			}
		}
	}
}
