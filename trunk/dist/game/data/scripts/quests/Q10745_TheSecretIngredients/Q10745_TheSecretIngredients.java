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
package quests.Q10745_TheSecretIngredients;

import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

/**
 * @author Neanrakyr
 */
public class Q10745_TheSecretIngredients extends Quest
{
	// Npcs
	private static final int DOLKIN = 33954;
	private static final int DOLKIN_INSTANCE = 34002;
	private static final int KARLA = 33933;
	// Monsters
	private static final int KARAPHON = 23459;
	private static final int KEEN_HONEYBEE = 23460;
	private static final int KEEN_GROWLER = 23461;
	// Locations
	private static final Location DOLKIN_INSTANCE_SPAWN = new Location(-82407, 246018, -14158);
	// Items
	private static final ItemHolder SECRET_INGREDIENTS = new ItemHolder(39533, 1);
	private static final ItemHolder DOLKIN_REPORT = new ItemHolder(39534, 1);
	private static final ItemHolder FAERON_SUPPORT_BOX = new ItemHolder(40262, 1);
	private static final ItemHolder FAERON_SUPPORT_BOX_MAGE = new ItemHolder(40263, 1);
	// Level Condition
	private static final int MIN_LEVEL = 17;
	private static final int MAX_LEVEL = 25;
	
	public Q10745_TheSecretIngredients()
	{
		super(10745, Q10745_TheSecretIngredients.class.getSimpleName(), "The Secret Ingredients");
		addStartNpc(DOLKIN);
		addTalkId(DOLKIN, DOLKIN_INSTANCE, KARLA);
		addKillId(KARAPHON, KEEN_HONEYBEE, KEEN_GROWLER);
		registerQuestItems(SECRET_INGREDIENTS.getId(), DOLKIN_REPORT.getId());
		addCondLevel(MIN_LEVEL, MAX_LEVEL, "no_quest.html");
		addCondRace(Race.ERTHEIA, "no_quest.html");
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
			case "33954-02.html":
			{
				qs.startQuest();
				htmltext = event;
				break;
			}
			case "33954-04.html":
			{
				if (qs.isCond(2))
				{
					qs.setCond(3);
					takeItem(player, SECRET_INGREDIENTS);
					giveItems(player, DOLKIN_REPORT);
					htmltext = event;
				}
				break;
			}
			case "33933-02.html":
			{
				if (qs.isCond(3))
				{
					giveAdena(player, 48000, true);
					addExpAndSp(player, 241076, 5);
					if (player.isMageClass())
					{
						giveItems(player, FAERON_SUPPORT_BOX_MAGE);
					}
					else
					{
						giveItems(player, FAERON_SUPPORT_BOX);
					}
					showOnScreenMsg(player, NpcStringId.CHECK_YOUR_EQUIPMENT_IN_YOUR_INVENTORY, ExShowScreenMessage.TOP_CENTER, 4500);
					qs.exitQuest(false, true);
					htmltext = event;
				}
				break;
			}
			case "spawn_dolkin":
			{
				showOnScreenMsg(player, NpcStringId.TALK_TO_DOLKIN_AND_LEAVE_THE_KARAPHON_HABITAT, ExShowScreenMessage.TOP_CENTER, 4500);
				addSpawn(DOLKIN_INSTANCE, DOLKIN_INSTANCE_SPAWN, false, 0, false, player.getInstanceId());
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
		
		if (qs.isCompleted())
		{
			htmltext = getAlreadyCompletedMsg(player);
		}
		
		switch (npc.getId())
		{
			case DOLKIN:
			{
				if (qs.isCreated())
				{
					htmltext = "33954-01.htm";
				}
				else if (qs.isCond(2))
				{
					htmltext = "33954-03.html";
				}
				break;
			}
			case KARLA:
			{
				switch (qs.getCond())
				{
					case 3:
					{
						htmltext = "33933-01.html";
						break;
					}
				}
			}
			case DOLKIN_INSTANCE:
			{
				switch (qs.getCond())
				{
					case 2:
					{
						htmltext = "34002.html";
						break;
					}
				}
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final QuestState qs = getQuestState(killer, false);
		
		if ((qs != null) && qs.isCond(1))
		{
			switch (npc.getId())
			{
				case KARAPHON:
				{
					qs.set("KARAPHON", 1);
					break;
				}
				case KEEN_HONEYBEE:
				{
					qs.set("KEEN_HONEYBEE", 1);
					break;
				}
				case KEEN_GROWLER:
				{
					qs.set("KEEN_GROWLER", 1);
					break;
				}
			}
			
			if ((qs.get("KARAPHON") != null) && (qs.get("KEEN_HONEYBEE") != null) && (qs.get("KEEN_GROWLER") != null))
			{
				giveItems(killer, SECRET_INGREDIENTS);
				qs.setCond(2, true);
				startQuestTimer("spawn_dolkin", 5000, npc, killer);
			}
		}
		
		return super.onKill(npc, killer, isSummon);
	}
}