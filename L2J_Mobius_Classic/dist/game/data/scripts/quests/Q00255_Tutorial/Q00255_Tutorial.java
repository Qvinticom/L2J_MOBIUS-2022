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
package quests.Q00255_Tutorial;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.enums.HtmlActionScope;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.Id;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerBypass;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerItemPickup;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.l2jmobius.gameserver.model.events.impl.character.player.OnPlayerPressTutorialMark;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;
import com.l2jmobius.gameserver.network.serverpackets.TutorialCloseHtml;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jmobius.gameserver.network.serverpackets.TutorialShowQuestionMark;

/**
 * Tutorial Quest
 * @author Mobius
 */
public class Q00255_Tutorial extends Quest
{
	// NPCs
	private final static int STARTING_HELPER_HF = 30009;
	private final static int STARTING_HELPER_HM = 30019;
	private final static int STARTING_HELPER_EL = 30400;
	private final static int STARTING_HELPER_DE = 30131;
	private final static int STARTING_HELPER_OR = 30575;
	private final static int STARTING_HELPER_DW = 30530;
	// Monsters
	private final static int[] GREMLINS =
	{
		18342, // this is used for now
		20001
	};
	// Items
	private final static int BLUE_GEM = 6353;
	private final static ItemHolder SOULSHOT_REWARD = new ItemHolder(5789, 100);
	private final static ItemHolder SPIRITSHOT_REWARD = new ItemHolder(5790, 100);
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
	private static final Map<Integer, Location> HELPER_LOCATION = new HashMap<>();
	{
		HELPER_LOCATION.put(0, new Location(-71424, 258336, -3109));
		HELPER_LOCATION.put(10, new Location(-91036, 248044, -3568));
		HELPER_LOCATION.put(18, new Location(46112, 41200, -3504));
		HELPER_LOCATION.put(25, new Location(46112, 41200, -3504));
		HELPER_LOCATION.put(31, new Location(28384, 11056, -4233));
		HELPER_LOCATION.put(38, new Location(28384, 11056, -4233));
		HELPER_LOCATION.put(44, new Location(-56736, -113680, -672));
		HELPER_LOCATION.put(49, new Location(-56736, -113680, -672));
		HELPER_LOCATION.put(53, new Location(108567, -173994, -406));
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
	private static final String TUTORIAL_BUYPASS = "Quest Q00255_Tutorial ";
	
	public Q00255_Tutorial()
	{
		super(255);
		addTalkId(STARTING_HELPER_HF, STARTING_HELPER_HM, STARTING_HELPER_EL, STARTING_HELPER_DE, STARTING_HELPER_OR, STARTING_HELPER_DW);
		addKillId(GREMLINS);
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
				playTutorialVoice(player, "tutorial_voice_003");
				showTutorialHtml(player, event);
				break;
			}
			case "start_newbie_tutorial":
			{
				if (!qs.isCompleted())
				{
					qs.startQuest();
					qs.setMemoState(1);
					playTutorialVoice(player, STARTING_VOICE_HTML.get(player.getClassId().getId()).getSound());
					showTutorialHtml(player, STARTING_VOICE_HTML.get(player.getClassId().getId()).getHtml());
				}
				break;
			}
			case "goto_newbie_guide":
			{
				if (qs.getMemoState() == 2)
				{
					qs.setMemoState(3);
					final int classId = player.getClassId().getId();
					addRadar(player, HELPER_LOCATION.get(classId).getX(), HELPER_LOCATION.get(classId).getY(), HELPER_LOCATION.get(classId).getZ());
					playSound(player, "ItemSound.quest_tutorial");
					htmltext = "tutorial_11.html";
				}
				break;
			}
			case "close_tutorial":
			{
				player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
				player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && !qs.isCompleted() && (qs.getMemoState() > 1) && hasQuestItems(player, BLUE_GEM))
		{
			qs.exitQuest(false, false); // finish here!
			if (player.isMageClass() && (player.getRace() != Race.ORC))
			{
				giveItems(player, SPIRITSHOT_REWARD);
			}
			else
			{
				giveItems(player, SOULSHOT_REWARD);
			}
			final int classId = player.getClassId().getId();
			addRadar(player, COMPLETE_LOCATION.get(classId).getX(), COMPLETE_LOCATION.get(classId).getY(), COMPLETE_LOCATION.get(classId).getZ());
			playSound(player, "ItemSound.quest_tutorial");
		}
		if ((qs != null) && (qs.isCompleted() || (qs.getMemoState() > 1)))
		{
			return "tutorial_15.html";
		}
		return "tutorial_09.html";
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		if ((qs != null) && (qs.getMemoState() < 2) && !hasQuestItems(killer, BLUE_GEM))
		{
			// check for too many gems on ground
			int counter = 0;
			for (L2ItemInstance item : L2World.getInstance().getVisibleObjects(killer, L2ItemInstance.class, 1500))
			{
				if (item.getId() == BLUE_GEM)
				{
					counter++;
				}
			}
			if (counter < 10) // do not drop if more than 10
			{
				if (qs.getMemoState() <= 1)
				{
					playSound(killer, "ItemSound.quest_tutorial");
					playTutorialVoice(killer, "tutorial_voice_011");
					showTutorialHtml(killer, "tutorial_09t.html");
				}
				npc.dropItem(killer, BLUE_GEM, 1);
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_ITEM_PICKUP)
	@RegisterType(ListenerRegisterType.ITEM)
	@Id(BLUE_GEM)
	public void OnPlayerItemPickup(OnPlayerItemPickup event)
	{
		final L2PcInstance player = event.getActiveChar();
		final QuestState qs = getQuestState(player, false);
		if ((qs != null) && (qs.getMemoState() == 1))
		{
			player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
			player.clearHtmlActions(HtmlActionScope.TUTORIAL_HTML);
			qs.setMemoState(2);
			playSound(player, "ItemSound.quest_tutorial");
			playTutorialVoice(player, "tutorial_voice_013");
			player.sendPacket(new TutorialShowQuestionMark(0, 1));
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_PRESS_TUTORIAL_MARK)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerPressTutorialMark(OnPlayerPressTutorialMark event)
	{
		final QuestState qs = getQuestState(event.getActiveChar(), false);
		if ((qs != null) && (event.getMarkId() == 1)) // tutorial mark
		{
			notifyEvent("goto_newbie_guide", null, event.getActiveChar());
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerBypass(OnPlayerBypass event)
	{
		final L2PcInstance player = event.getActiveChar();
		if (event.getCommand().startsWith(TUTORIAL_BUYPASS))
		{
			notifyEvent(event.getCommand().replace(TUTORIAL_BUYPASS, ""), null, player);
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void OnPlayerLogin(OnPlayerLogin event)
	{
		if (Config.DISABLE_TUTORIAL)
		{
			return;
		}
		
		final L2PcInstance player = event.getActiveChar();
		if (player.getLevel() > 6)
		{
			return;
		}
		
		QuestState qs = getQuestState(player, true);
		if ((qs != null) && !qs.isCompleted() && STARTING_VOICE_HTML.containsKey(player.getClassId().getId()))
		{
			startQuestTimer("start_newbie_tutorial", 5000, null, player);
		}
	}
	
	private void showTutorialHtml(L2PcInstance player, String html)
	{
		player.sendPacket(new TutorialShowHtml(getHtm(player.getHtmlPrefix(), html)));
	}
	
	public void playTutorialVoice(L2PcInstance player, String voice)
	{
		player.sendPacket(new PlaySound(2, voice, 0, 0, player.getX(), player.getY(), player.getZ()));
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
