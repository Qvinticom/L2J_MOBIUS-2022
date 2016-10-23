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
package handlers.bypasshandlers;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.l2jmobius.gameserver.handler.IBypassHandler;
import com.l2jmobius.gameserver.instancemanager.QuestManager;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.listeners.AbstractEventListener;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

public class QuestLink implements IBypassHandler
{
	private static final int MAX_QUEST_COUNT = 40;
	private static final String[] COMMANDS =
	{
		"Quest"
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		String quest = "";
		try
		{
			quest = command.substring(5).trim();
		}
		catch (IndexOutOfBoundsException ioobe)
		{
		}
		if (quest.length() == 0)
		{
			showQuestWindow(activeChar, (L2Npc) target);
		}
		else
		{
			final int questNameEnd = quest.indexOf(" ");
			if (questNameEnd == -1)
			{
				showQuestWindow(activeChar, (L2Npc) target, quest);
			}
			else
			{
				activeChar.processQuestEvent(quest.substring(0, questNameEnd), quest.substring(questNameEnd).trim());
			}
		}
		return true;
	}
	
	/**
	 * Open a choose quest window on client with all quests available of the L2NpcInstance.<br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * @param player The L2PcInstance that talk with the L2NpcInstance
	 * @param npc The table containing quests of the L2NpcInstance
	 * @param quests
	 */
	private static void showQuestChooseWindow(L2PcInstance player, L2Npc npc, Collection<Quest> quests)
	{
		final StringBuilder sb = new StringBuilder(128);
		final StringBuilder qStarted = new StringBuilder(128);
		final StringBuilder qCanStart = new StringBuilder(128);
		final StringBuilder qCannotstart = new StringBuilder(128);
		final StringBuilder qComplete = new StringBuilder(128);
		sb.append("<html><body>");
		String state = "";
		String color = "";
		
		//@formatter:off
		final Set<Quest> startingQuests = npc.getListeners(EventType.ON_NPC_QUEST_START).stream()
			.map(AbstractEventListener::getOwner)
			.filter(Quest.class::isInstance)
			.map(Quest.class::cast)
			.distinct()
			.collect(Collectors.toSet());
		//@formatter:on
		
		for (Quest quest : quests)
		{
			final QuestState qs = player.getQuestState(quest.getName());
			if ((qs == null) || qs.isCreated())
			{
				state = quest.isCustomQuest() ? "" : "01";
				if (startingQuests.contains(quest) && quest.canStartQuest(player))
				{
					color = "bbaa88";
				}
				else
				{
					color = "a62f31";
				}
			}
			else if (qs.isStarted())
			{
				state = quest.isCustomQuest() ? " (In Progress)" : "02";
				color = "ffdd66";
			}
			else if (qs.isCompleted())
			{
				state = quest.isCustomQuest() ? " (Done)" : "03";
				color = "787878";
			}
			
			switch (color)
			{
				case "ffdd66": // started
				{
					qStarted.append("<font color=\"" + color + "\">");
					qStarted.append("<button icon=\"quest\" align=\"left\" action=\"bypass -h npc_" + String.valueOf(npc.getObjectId()) + "_Quest " + quest.getName() + "\">");
					appendToText(quest, qStarted, state);
					break;
				}
				case "bbaa88": // can start
				{
					qCanStart.append("<font color=\"" + color + "\">");
					qCanStart.append("<button icon=\"quest\" align=\"left\" action=\"bypass -h npc_" + String.valueOf(npc.getObjectId()) + "_Quest " + quest.getName() + "\">");
					appendToText(quest, qCanStart, state);
					break;
				}
				case "a62f31": // cannot start
				{
					qCannotstart.append("<font color=\"" + color + "\">");
					qCannotstart.append("<button icon=\"quest\" align=\"left\" action=\"bypass -h npc_" + String.valueOf(npc.getObjectId()) + "_Quest " + quest.getName() + "\">");
					appendToText(quest, qCannotstart, state);
					break;
				}
				case "787878": // complete
				{
					qComplete.append("<font color=\"" + color + "\">");
					qComplete.append("<button icon=\"quest\" align=\"left\" action=\"bypass -h npc_" + String.valueOf(npc.getObjectId()) + "_Quest " + quest.getName() + "\">");
					appendToText(quest, qComplete, state);
					break;
				}
			}
		}
		sb.append(qStarted);
		sb.append(qCanStart);
		sb.append(qCannotstart);
		sb.append(qComplete);
		sb.append("</body></html>");
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		npc.insertObjectIdAndShowChatWindow(player, sb.toString());
	}
	
	private static void appendToText(Quest quest, StringBuilder sb, String state)
	{
		if (quest.isCustomQuest())
		{
			sb.append(quest.getDescr() + state);
		}
		else
		{
			sb.append("<fstring>" + String.valueOf(quest.getNpcStringId()) + state + "</fstring>");
		}
		sb.append("</button></font>");
	}
	
	/**
	 * Open a quest window on client with the text of the L2NpcInstance.<br>
	 * <b><u>Actions</u>:</b><br>
	 * <ul>
	 * <li>Get the text of the quest state in the folder scripts/quests/questId/stateId.htm</li>
	 * <li>Send a Server->Client NpcHtmlMessage containing the text of the L2NpcInstance to the L2PcInstance</li>
	 * <li>Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet</li>
	 * </ul>
	 * @param player the L2PcInstance that talk with the {@code npc}
	 * @param npc the L2NpcInstance that chats with the {@code player}
	 * @param questId the Id of the quest to display the message
	 */
	private static void showQuestWindow(L2PcInstance player, L2Npc npc, String questId)
	{
		String content = null;
		
		final Quest q = QuestManager.getInstance().getQuest(questId);
		
		// Get the state of the selected quest
		final QuestState qs = player.getQuestState(questId);
		
		if (q != null)
		{
			if ((q.getId() >= 1) && (q.getId() < 20000) && ((player.getWeightPenalty() >= 3) || !player.isInventoryUnder90(true)))
			{
				player.sendPacket(SystemMessageId.UNABLE_TO_PROCESS_THIS_REQUEST_UNTIL_YOUR_INVENTORY_S_WEIGHT_AND_SLOT_COUNT_ARE_LESS_THAN_80_PERCENT_OF_CAPACITY);
				return;
			}
			
			if ((qs == null) && (q.getId() >= 1) && (q.getId() < 20000) && (player.getAllActiveQuests().size() >= MAX_QUEST_COUNT))
			{
				final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
				html.setFile(player.getHtmlPrefix(), "html/fullquest.html");
				player.sendPacket(html);
				return;
			}
			
			q.notifyTalk(npc, player);
		}
		else
		{
			content = Quest.getNoQuestMsg(player); // no quests found
		}
		
		// Send a Server->Client packet NpcHtmlMessage to the L2PcInstance in order to display the message of the L2NpcInstance
		if (content != null)
		{
			npc.insertObjectIdAndShowChatWindow(player, content);
		}
		
		// Send a Server->Client ActionFailed to the L2PcInstance in order to avoid that the client wait another packet
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Collect awaiting quests/start points and display a QuestChooseWindow (if several available) or QuestWindow.
	 * @param player the L2PcInstance that talk with the {@code npc}.
	 * @param npc the L2NpcInstance that chats with the {@code player}.
	 */
	private static void showQuestWindow(L2PcInstance player, L2Npc npc)
	{
		//@formatter:off
		final Set<Quest> quests = npc.getListeners(EventType.ON_NPC_TALK).stream()
			.map(AbstractEventListener::getOwner)
			.filter(Quest.class::isInstance)
			.map(Quest.class::cast)
			.filter(quest -> (quest.getId() > 0) && (quest.getId() < 20000))
			.distinct()
			.collect(Collectors.toSet());
		//@formatter:on
		
		if (quests.size() > 1)
		{
			showQuestChooseWindow(player, npc, quests);
		}
		else if (quests.size() == 1)
		{
			showQuestWindow(player, npc, quests.stream().findFirst().get().getName());
		}
		else
		{
			showQuestWindow(player, npc, "");
		}
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}
