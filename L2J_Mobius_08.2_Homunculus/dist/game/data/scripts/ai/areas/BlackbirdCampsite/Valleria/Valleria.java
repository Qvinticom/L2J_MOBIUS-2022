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
package ai.areas.BlackbirdCampsite.Valleria;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Valleria AI.
 * @author CostyKiller
 */
public final class Valleria extends AbstractNpcAI
{
	// NPC
	private static final int VALLERIA = 34435;
	// Items
	private static final int MEDAL = 48516; // Medal of Honor
	private static final int GRAND_MEDAL = 48517; // Grand Medal of Honor
	// Misc
	private static final int MEDAL_POINTS = 100;
	private static final int GRAND_MEDAL_POINTS = 1000;
	private static final int MIN_LEVEL = 99;
	
	private Valleria()
	{
		addStartNpc(VALLERIA);
		addFirstTalkId(VALLERIA);
		addTalkId(VALLERIA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "34435.html":
			case "34435-01.html":
			case "34435-02.html":
			case "34435-03.html":
			case "34435-04.html":
			{
				htmltext = event;
				break;
			}
			case "medal_for_blackbird":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, MEDAL))
					{
						takeItems(player, 1, MEDAL);
						player.addFactionPoints(Faction.BLACKBIRD_CLAN, MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "medal_for_mother":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, MEDAL))
					{
						takeItems(player, 1, MEDAL);
						player.addFactionPoints(Faction.MOTHER_TREE_GUARDIANS, MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "medal_for_giant":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, MEDAL))
					{
						takeItems(player, 1, MEDAL);
						player.addFactionPoints(Faction.GIANT_TRACKERS, MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "medal_for_unworldly":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, MEDAL))
					{
						takeItems(player, 1, MEDAL);
						player.addFactionPoints(Faction.UNWORLDLY_VISITORS, MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "medal_for_kingdom":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, MEDAL))
					{
						takeItems(player, 1, MEDAL);
						player.addFactionPoints(Faction.KINGDOM_ROYAL_GUARDS, MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "grand_medal_for_blackbird":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, GRAND_MEDAL))
					{
						takeItems(player, 1, GRAND_MEDAL);
						player.addFactionPoints(Faction.BLACKBIRD_CLAN, GRAND_MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "grand_medal_for_mother":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, GRAND_MEDAL))
					{
						takeItems(player, 1, GRAND_MEDAL);
						player.addFactionPoints(Faction.MOTHER_TREE_GUARDIANS, GRAND_MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "grand_medal_for_giant":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, GRAND_MEDAL))
					{
						takeItems(player, 1, GRAND_MEDAL);
						player.addFactionPoints(Faction.GIANT_TRACKERS, GRAND_MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "grand_medal_for_unworldly":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, GRAND_MEDAL))
					{
						takeItems(player, 1, GRAND_MEDAL);
						player.addFactionPoints(Faction.UNWORLDLY_VISITORS, GRAND_MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
			case "grand_medal_for_kingdom":
			{
				if (player.getLevel() < MIN_LEVEL)
				{
					htmltext = "no_level.html";
				}
				else
				{
					if (hasAtLeastOneQuestItem(player, GRAND_MEDAL))
					{
						takeItems(player, 1, GRAND_MEDAL);
						player.addFactionPoints(Faction.KINGDOM_ROYAL_GUARDS, GRAND_MEDAL_POINTS);
						htmltext = "success.html";
					}
					else
					{
						htmltext = "no_medal.html";
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "34435.html";
	}
	
	public static void main(String[] args)
	{
		new Valleria();
	}
}