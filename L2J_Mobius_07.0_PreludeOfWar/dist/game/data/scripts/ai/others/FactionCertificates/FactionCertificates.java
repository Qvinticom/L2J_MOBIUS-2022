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
package ai.others.FactionCertificates;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;
import quests.Q10875_ForReputation.Q10875_ForReputation;
import quests.Q10881_ForThePride.Q10881_ForThePride;

/**
 * Custom Faction Certificates AI.
 * @author CostyKiller
 */
public class FactionCertificates extends AbstractNpcAI
{
	// NPCs
	private static final int LEONA_BLACKBIRD = 31595; // Blackbird Guild Leader
	private static final int KEKROPUS = 34222; // Giant Trackers Guild Leader
	private static final int IRENE = 34233; // Mother Tree Guardians Guild Leader
	private static final int FERIN = 34054; // Unwordly Visitors Guild Leader
	private static final int LOGART_VAN_DIKE = 34235; // Royal Kingdom Guards Guild Leader
	// Certificates
	private static final int BLACKBIRD_CLAN_CERTIFICATION = 47840;
	private static final int GIANT_TRACKERS_CERTIFICATION = 47841;
	private static final int MOTHER_TREE_GUARDIANS_CERTIFICATION = 47844;
	private static final int UNWORDLY_VISITORS_CERTIFICATION = 47845;
	private static final int KINGDOM_ROYAL_GUARDS_CERTIFICATION = 47846;
	// Other
	private static final int FACTION_LEVEL = 6;
	
	private FactionCertificates()
	{
		addStartNpc(LEONA_BLACKBIRD, KEKROPUS, IRENE, FERIN, LOGART_VAN_DIKE);
		addTalkId(LEONA_BLACKBIRD, KEKROPUS, IRENE, FERIN, LOGART_VAN_DIKE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs1 = player.getQuestState(Q10875_ForReputation.class.getSimpleName());
		final QuestState qs2 = player.getQuestState(Q10881_ForThePride.class.getSimpleName());
		if (((qs1 != null) && qs1.isStarted()) || ((qs2 != null) && qs2.isStarted()))
		{
			if (event.equals("getCertificate"))
			{
				switch (npc.getId())
				{
					case LEONA_BLACKBIRD:
					{
						if (!hasQuestItems(player, BLACKBIRD_CLAN_CERTIFICATION) && (player.getFactionLevel(Faction.BLACKBIRD_CLAN) >= FACTION_LEVEL))
						{
							giveItems(player, BLACKBIRD_CLAN_CERTIFICATION, 1);
							htmltext = "certificate.html";
						}
						break;
					}
					case KEKROPUS:
					{
						if (!hasQuestItems(player, GIANT_TRACKERS_CERTIFICATION) && (player.getFactionLevel(Faction.GIANT_TRACKERS) >= FACTION_LEVEL))
						{
							giveItems(player, GIANT_TRACKERS_CERTIFICATION, 1);
							htmltext = "certificate.html";
						}
						break;
					}
					case IRENE:
					{
						if (!hasQuestItems(player, MOTHER_TREE_GUARDIANS_CERTIFICATION) && (player.getFactionLevel(Faction.MOTHER_TREE_GUARDIANS) >= FACTION_LEVEL))
						{
							giveItems(player, MOTHER_TREE_GUARDIANS_CERTIFICATION, 1);
							htmltext = "certificate.html";
						}
						break;
					}
					case FERIN:
					{
						if (!hasQuestItems(player, UNWORDLY_VISITORS_CERTIFICATION) && (player.getFactionLevel(Faction.UNWORLDLY_VISITORS) >= FACTION_LEVEL))
						{
							giveItems(player, UNWORDLY_VISITORS_CERTIFICATION, 1);
							htmltext = "certificate.html";
						}
						break;
					}
					case LOGART_VAN_DIKE:
					{
						if (!hasQuestItems(player, KINGDOM_ROYAL_GUARDS_CERTIFICATION) && (player.getFactionLevel(Faction.KINGDOM_ROYAL_GUARDS) >= FACTION_LEVEL))
						{
							giveItems(player, KINGDOM_ROYAL_GUARDS_CERTIFICATION, 1);
							htmltext = "certificate.html";
						}
						break;
					}
				}
			}
		}
		htmltext = "not-ready.html";
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new FactionCertificates();
	}
}
