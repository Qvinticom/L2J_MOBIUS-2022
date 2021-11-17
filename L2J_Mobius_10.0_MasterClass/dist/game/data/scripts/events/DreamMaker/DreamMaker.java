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
package events.DreamMaker;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;

/**
 * Dream Maker event AI.
 * @author CostyKiller
 */
public class DreamMaker extends LongTimeEvent
{
	// NPC
	private static final int MOO_COW = 34590;
	// Items
	private static final int LUCKY_POUCH_GROWTH = 81934; // Lucky Pouch Growth
	private static final int LUCKY_POUCH_PROSPERITY = 81935; // Lucky Pouch Prosperity
	private static final int LUCKY_POUCH_HARMONY = 81936; // Lucky Pouch Harmony
	// Buffs
	private static final SkillHolder[] BUFFS =
	{
		new SkillHolder(34032, 1), // Unchallenged Supremacy
		new SkillHolder(34032, 2), // Unchallenged Supremacy
		new SkillHolder(34033, 1), // Red Alert
		new SkillHolder(34033, 2), // Red Alert
		new SkillHolder(34034, 1), // Step Up
		new SkillHolder(34034, 2), // Step Up
		new SkillHolder(34035, 1), // Luck's Kindness
		new SkillHolder(34035, 2), // Luck's Kindness
	};
	
	private DreamMaker()
	{
		addStartNpc(MOO_COW);
		addFirstTalkId(MOO_COW);
		addTalkId(MOO_COW);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		switch (event)
		{
			case "get_gift":
			{
				if (!player.getVariables().getBoolean("DREAM_MAKER_GIFT_RECEIVED", false))
				{
					giveItems(player, Rnd.get(LUCKY_POUCH_GROWTH, LUCKY_POUCH_HARMONY), 1);
					player.getVariables().set("DREAM_MAKER_GIFT_RECEIVED", true);
					htmltext = "34590-successful.htm";
				}
				else
				{
					htmltext = "34590-already-received.htm";
				}
				break;
			}
			case "g_pouch":
			{
				if (player.getLevel() < 105)
				{
					htmltext = "34590-gift-no-level.htm";
				}
				else if (player.getVariables().getBoolean("DREAM_MAKER_GIFT_EXCHANGED", false))
				{
					htmltext = "34590-already-exchanged.htm";
				}
				else if (hasAtLeastOneQuestItem(player, LUCKY_POUCH_PROSPERITY, LUCKY_POUCH_HARMONY))
				{
					if (hasQuestItems(player, LUCKY_POUCH_PROSPERITY))
					{
						takeItems(player, LUCKY_POUCH_PROSPERITY, 1);
					}
					else if (hasQuestItems(player, LUCKY_POUCH_HARMONY))
					{
						takeItems(player, LUCKY_POUCH_HARMONY, 1);
					}
					giveItems(player, LUCKY_POUCH_GROWTH, 1);
					player.getVariables().set("DREAM_MAKER_GIFT_EXCHANGED", true);
					htmltext = "34590-successful.htm";
				}
				else
				{
					htmltext = "34590-no-pouch.htm";
				}
				break;
			}
			case "p_pouch":
			{
				if (player.getLevel() < 105)
				{
					htmltext = "34590-gift-no-level.htm";
				}
				else if (player.getVariables().getBoolean("DREAM_MAKER_GIFT_EXCHANGED", false))
				{
					htmltext = "34590-pouch-exchanged.htm";
				}
				else if (hasAtLeastOneQuestItem(player, LUCKY_POUCH_GROWTH, LUCKY_POUCH_HARMONY))
				{
					if (hasQuestItems(player, LUCKY_POUCH_GROWTH))
					{
						takeItems(player, LUCKY_POUCH_GROWTH, 1);
					}
					else if (hasQuestItems(player, LUCKY_POUCH_HARMONY))
					{
						takeItems(player, LUCKY_POUCH_HARMONY, 1);
					}
					giveItems(player, LUCKY_POUCH_PROSPERITY, 1);
					player.getVariables().set("DREAM_MAKER_GIFT_EXCHANGED", true);
					htmltext = "34590-successful.htm";
				}
				else
				{
					htmltext = "34590-no-pouch.htm";
				}
				break;
			}
			case "h_pouch":
			{
				if (player.getLevel() < 105)
				{
					htmltext = "34590-gift-no-level.htm";
				}
				else if (player.getVariables().getBoolean("DREAM_MAKER_GIFT_EXCHANGED", false))
				{
					htmltext = "34590-pouch-exchanged.htm";
				}
				else if (hasAtLeastOneQuestItem(player, LUCKY_POUCH_GROWTH, LUCKY_POUCH_PROSPERITY))
				{
					if (hasQuestItems(player, LUCKY_POUCH_GROWTH))
					{
						takeItems(player, LUCKY_POUCH_GROWTH, 1);
					}
					else if (hasQuestItems(player, LUCKY_POUCH_PROSPERITY))
					{
						takeItems(player, LUCKY_POUCH_PROSPERITY, 1);
					}
					giveItems(player, LUCKY_POUCH_HARMONY, 1);
					player.getVariables().set("DREAM_MAKER_GIFT_EXCHANGED", true);
					htmltext = "34590-successful.htm";
				}
				else
				{
					htmltext = "34590-no-pouch.htm";
				}
				break;
			}
			case "get_buff":
			{
				if (player.getLevel() < 105)
				{
					htmltext = "34590-buff-no-level.htm";
				}
				else if ((player.getVariables().getLong("DREAM_MAKER_BUFF_DELAY", 0) + 86400000) >= Chronos.currentTimeMillis())
				{
					npc.doCast(getRandomEntry(BUFFS).getSkill());
					player.getVariables().set("DREAM_MAKER_BUFF_DELAY", Chronos.currentTimeMillis());
					htmltext = "34590-buff-received.htm";
				}
				else
				{
					htmltext = "34590-buff-already-received.htm";
				}
				break;
			}
			case "alter_buff":
			{
				if (player.getAdena() < 5000000)
				{
					htmltext = "34590-buff-no-adena.htm";
				}
				else if (player.isAffectedBySkill(BUFFS[0].getSkillId()) || player.isAffectedBySkill(BUFFS[2].getSkillId()) || player.isAffectedBySkill(BUFFS[4].getSkillId()) || player.isAffectedBySkill(BUFFS[6].getSkillId()))
				{
					takeItems(player, 57, 5000000);
					npc.setTarget(player);
					npc.doCast(getRandomEntry(BUFFS).getSkill());
					htmltext = "34590-buff-received.htm";
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return npc.getId() + ".htm";
	}
	
	public static void main(String[] args)
	{
		new DreamMaker();
	}
}
