/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package quests.Q00255_Tutorial;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;

/**
 * Tutorial Quest
 * @author Mobius
 */
public class Q00255_Tutorial extends Quest
{
	// Npcs
	private final static int STARTING_HELPER_HF = 30009;
	private final static int STARTING_HELPER_HM = 30019;
	private final static int STARTING_HELPER_EL = 30400;
	private final static int STARTING_HELPER_DE = 30131;
	private final static int STARTING_HELPER_OR = 30575;
	private final static int STARTING_HELPER_DW = 30530;
	private final static int NEWBIE_BUFFER_HU = 30598;
	private final static int NEWBIE_BUFFER_EL = 30599;
	private final static int NEWBIE_BUFFER_DE = 30600;
	private final static int NEWBIE_BUFFER_OR = 30602;
	private final static int NEWBIE_BUFFER_DW = 30601;
	// Monster
	private final static int GREMLIN = 20001;
	// Item
	private final static int BLUE_GEM = 6353;
	private final static ItemHolder SOULSHOT_REWARD = new ItemHolder(5789, 500);
	private final static ItemHolder SPIRITSHOT_REWARD = new ItemHolder(5790, 500);
	// Others
	private static final Map<Integer, QuestSoundHtmlHolder> STARTING_VOICE_HTML = new HashMap<>();
	{
		STARTING_VOICE_HTML.put(0, new QuestSoundHtmlHolder("tutorial_voice_001a", "tutorial_human_fighter001.html"));
		STARTING_VOICE_HTML.put(10, new QuestSoundHtmlHolder("tutorial_voice_001b", "tutorial_human_mage001.html"));
		STARTING_VOICE_HTML.put(18, new QuestSoundHtmlHolder("tutorial_voice_001c", "tutorial_elven_fighter001.html"));
		STARTING_VOICE_HTML.put(25, new QuestSoundHtmlHolder("tutorial_voice_001d", "tutorial_elven_mage001.html"));
		STARTING_VOICE_HTML.put(31, new QuestSoundHtmlHolder("tutorial_voice_001e", "tutorial_delf_fighter001.html"));
		STARTING_VOICE_HTML.put(38, new QuestSoundHtmlHolder("tutorial_voice_001f", "tutorial_delf_mage001.html"));
		STARTING_VOICE_HTML.put(44, new QuestSoundHtmlHolder("tutorial_voice_001g", "tutorial_orc_fighter001.html"));
		STARTING_VOICE_HTML.put(49, new QuestSoundHtmlHolder("tutorial_voice_001h", "tutorial_orc_mage001.html"));
		STARTING_VOICE_HTML.put(53, new QuestSoundHtmlHolder("tutorial_voice_001i", "tutorial_dwarven_fighter001.html"));
	}
	private static final Map<Integer, Location> COMPLETE_LOCATION = new HashMap<>();
	{
		COMPLETE_LOCATION.put(0, new Location(-84081, 243227, -3723));
		COMPLETE_LOCATION.put(10, new Location(-84081, 243227, -3723));
		COMPLETE_LOCATION.put(18, new Location(45475, 48359, -3060));
		COMPLETE_LOCATION.put(25, new Location(45475, 48359, -3060));
		COMPLETE_LOCATION.put(31, new Location(12111, 16686, -4582));
		COMPLETE_LOCATION.put(38, new Location(12111, 16686, -4582));
		COMPLETE_LOCATION.put(44, new Location(-45032, -113598, -192));
		COMPLETE_LOCATION.put(49, new Location(-45032, -113598, -192));
		COMPLETE_LOCATION.put(53, new Location(115632, -177996, -905));
	}
	
	public Q00255_Tutorial()
	{
		super(255, Q00255_Tutorial.class.getSimpleName(), "Tutorial");
		setIsCustom(true);
		addTalkId(STARTING_HELPER_HF, STARTING_HELPER_HM, STARTING_HELPER_EL, STARTING_HELPER_DE, STARTING_HELPER_OR, STARTING_HELPER_DW);
		addFirstTalkId(NEWBIE_BUFFER_HU, NEWBIE_BUFFER_EL, NEWBIE_BUFFER_DE, NEWBIE_BUFFER_OR, NEWBIE_BUFFER_DW);
		addKillId(GREMLIN);
		registerQuestItems(BLUE_GEM);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "tutorial_02.html":
			{
				htmltext = event;
				break;
			}
			case "user_connected":
			{
				// Start Newbie Tutorial
				if ((player.getLevel() < 6) && !qs.isCompleted() && STARTING_VOICE_HTML.containsKey(player.getClassId().getId()))
				{
					startQuestTimer("start_newbie_tutorial", 5000, null, player);
				}
				break;
			}
			case "start_newbie_tutorial":
			{
				qs.playTutorialVoice(STARTING_VOICE_HTML.get(player.getClassId().getId()).getSound());
				htmltext = STARTING_VOICE_HTML.get(player.getClassId().getId()).getHtml();
				// TODO: To hear the next tutorial click the button...
				// startQuestTimer("start_newbie_tutorial_2", 30000, null, player);
				break;
			}
			case "newbie_pick_up_items":
			{
				if (qs.getMemoState() < 1)
				{
					qs.setMemoState(1);
					qs.playTutorialVoice("tutorial_voice_013");
					qs.playSound("ItemSound.quest_tutorial");
					htmltext = "tutorial_11.html";
				}
				// TODO: Fix client events.
				// htmltext = "tutorial_10.html";
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, true);
		String htmltext = getNoQuestMsg(player);
		
		switch (npc.getId())
		{
			case STARTING_HELPER_HF:
			case STARTING_HELPER_HM:
			case STARTING_HELPER_EL:
			case STARTING_HELPER_DE:
			case STARTING_HELPER_OR:
			case STARTING_HELPER_DW:
			{
				if (qs.getMemoState() == 2)
				{
					final int classId = player.getClassId().getId();
					qs.addRadar(COMPLETE_LOCATION.get(classId).getX(), COMPLETE_LOCATION.get(classId).getY(), COMPLETE_LOCATION.get(classId).getZ());
					qs.playSound("ItemSound.quest_tutorial");
					htmltext = "tutorial_15.html";
				}
				else if (!hasQuestItems(player, BLUE_GEM))
				{
					htmltext = "tutorial_09.html";
				}
				else if (hasQuestItems(player, BLUE_GEM))
				{
					final int classId = player.getClassId().getId();
					if (!STARTING_VOICE_HTML.containsKey(classId))
					{
						return htmltext;
					}
					takeItems(player, BLUE_GEM, -1);
					qs.setMemoState(2);
					qs.addRadar(COMPLETE_LOCATION.get(classId).getX(), COMPLETE_LOCATION.get(classId).getY(), COMPLETE_LOCATION.get(classId).getZ());
					qs.playSound("ItemSound.quest_tutorial");
					htmltext = "go_to_newbie_helper.html";
				}
				break;
			}
		}
		
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && !qs.isCompleted() && (qs.getMemoState() == 2))
		{
			if (player.isMageClass() && (player.getRace() != Race.ORC))
			{
				giveItems(player, SPIRITSHOT_REWARD);
			}
			else
			{
				giveItems(player, SOULSHOT_REWARD);
			}
			qs.setMemoState(3);
			qs.exitQuest(false, true);
		}
		return npc.getId() + ".html";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.getMemoState() < 1) && !hasQuestItems(killer, BLUE_GEM) && getRandomBoolean())
		{
			npc.dropItem(killer, BLUE_GEM, 1);
			startQuestTimer("newbie_pick_up_items", 100, null, killer);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	private class QuestSoundHtmlHolder
	{
		private final String _sound;
		private final String _html;
		
		QuestSoundHtmlHolder(String sound, String html)
		{
			_sound = sound;
			_html = html;
		}
		
		String getSound()
		{
			return _sound;
		}
		
		String getHtml()
		{
			return _html;
		}
	}
}
