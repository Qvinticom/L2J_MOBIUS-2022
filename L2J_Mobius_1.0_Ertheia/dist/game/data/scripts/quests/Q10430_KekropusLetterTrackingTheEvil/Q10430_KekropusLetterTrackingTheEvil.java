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
package quests.Q10430_KekropusLetterTrackingTheEvil;

import org.l2jmobius.gameserver.enums.CategoryType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import quests.LetterQuest;

/**
 * Kekropus' Letter: Tracking The Evil (10430)
 * @author Stayway
 */
public class Q10430_KekropusLetterTrackingTheEvil extends LetterQuest
{
	// NPCs
	private static final int VISHOTSKY = 31981;
	private static final int JOKEL = 33868;
	private static final int INVISIBLE_NPC = 19543;
	// Items
	private static final int SOE_TOWN_OF_SCHUTTGART = 37123; // Scroll of Escape: Town of Schuttgart
	private static final int SOE_DEN_OF_EVIL = 37036; // Scroll of Escape: Den of Evil
	private static final int EWS = 959; // Scroll: Enchant Weapon (S-grade)
	private static final int EAS = 960; // Scroll: Enchant Armor (S-grade)
	// Location
	private static final Location TELEPORT_LOC = new Location(86404, -142221, -1341);
	// Misc
	private static final int MIN_LEVEL = 81;
	private static final int MAX_LEVEL = 84;
	
	public Q10430_KekropusLetterTrackingTheEvil()
	{
		super(10430);
		addTalkId(VISHOTSKY, JOKEL);
		addSeeCreatureId(INVISIBLE_NPC);
		setIsErtheiaQuest(false);
		setLevel(MIN_LEVEL, MAX_LEVEL);
		setStartQuestSound("Npcdialog1.kekrops_quest_9");
		setStartLocation(SOE_TOWN_OF_SCHUTTGART, TELEPORT_LOC);
		registerQuestItems(SOE_TOWN_OF_SCHUTTGART, SOE_DEN_OF_EVIL);
		addCondInCategory(CategoryType.MAGE_GROUP, "nocond.html");
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return null;
		}
		
		String htmltext = null;
		switch (event)
		{
			case "31981-02.html":
			{
				htmltext = event;
				break;
			}
			case "31981-03.html":
			{
				if (qs.isCond(1))
				{
					takeItems(player, SOE_TOWN_OF_SCHUTTGART, -1);
					giveItems(player, SOE_DEN_OF_EVIL, 1);
					qs.setCond(2, true);
					htmltext = event;
				}
				break;
			}
			case "33868-02.html":
			{
				if (qs.isCond(2))
				{
					qs.exitQuest(false, true);
					giveItems(player, EWS, 1);
					giveItems(player, EAS, 10);
					giveStoryQuestReward(player, 235);
					if ((player.getLevel() >= MIN_LEVEL) && (player.getLevel() <= MAX_LEVEL))
					{
						addExpAndSp(player, 1_412_040, 338);
					}
					htmltext = event;
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = getNoQuestMsg(player);
		final QuestState qs = getQuestState(player, false);
		if (qs == null)
		{
			return htmltext;
		}
		
		if (qs.isStarted())
		{
			if ((npc.getId() == VISHOTSKY) && qs.isCond(1))
			{
				htmltext = "31981-01.html";
			}
			else if (qs.isCond(2))
			{
				htmltext = npc.getId() == VISHOTSKY ? "31981-04.html" : "33868-01.html";
			}
		}
		return htmltext;
	}
	
	@Override
	public String onSeeCreature(Npc npc, Creature creature, boolean isSummon)
	{
		if (creature.isPlayer())
		{
			final PlayerInstance player = creature.getActingPlayer();
			final QuestState qs = getQuestState(player, false);
			if ((qs != null) && qs.isCond(2))
			{
				showOnScreenMsg(player, NpcStringId.DEN_OF_EVIL_IS_A_GOOD_HUNTING_ZONE_FOR_LV_81_OR_ABOVE, ExShowScreenMessage.TOP_CENTER, 6000);
			}
		}
		return super.onSeeCreature(npc, creature, isSummon);
	}
	
	@Override
	public boolean canShowTutorialMark(PlayerInstance player)
	{
		return player.isInCategory(CategoryType.FIGHTER_GROUP);
	}
}