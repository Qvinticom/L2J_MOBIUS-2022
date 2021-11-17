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
package instances.EvasHiddenSpace;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.quest.QuestState;

import instances.AbstractInstance;
import quests.Q10591_NobleMaterial.Q10591_NobleMaterial;

/**
 * Eva's Hidden Space instance zone.
 * @author Gladicek, St3eT
 */
public class EvasHiddenSpace extends AbstractInstance
{
	// NPCs
	private static final int EVAS_AVATAR = 33686;
	// Misc
	private static final int TEMPLATE_ID = 217;
	
	public EvasHiddenSpace()
	{
		super(TEMPLATE_ID);
		addStartNpc(EVAS_AVATAR);
		addTalkId(EVAS_AVATAR);
		addFirstTalkId(EVAS_AVATAR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		if (event.equals("enterInstance"))
		{
			enterInstance(player, npc, TEMPLATE_ID);
		}
		else
		{
			final Instance world = npc.getInstanceWorld();
			if (isInInstance(world))
			{
				switch (event)
				{
					case "33686-01.html":
					case "33686-04.html":
					{
						htmltext = event;
						break;
					}
					case "inter_quest_10591_NPC33686":
					{
						final QuestState qs = player.getQuestState(Q10591_NobleMaterial.class.getSimpleName());
						if ((qs != null) && qs.isCond(6))
						{
							qs.setCond(7, true);
							htmltext = "33686-02.html";
						}
						break;
					}
					case "exitInstance":
					{
						world.finishInstance(0);
						break;
					}
					case "endCinematic":
					{
						startQuestTimer("exitInstance", 250, npc, player);
						break;
					}
				}
			}
			else if (event.equals("exitInstance"))
			{
				teleportPlayerOut(player, world);
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String htmltext = null;
		final QuestState qs = player.getQuestState(Q10591_NobleMaterial.class.getSimpleName());
		if (qs != null)
		{
			if (qs.isCond(6))
			{
				htmltext = "33686.html";
			}
			else if (qs.isCond(7))
			{
				htmltext = "33686-03.html";
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new EvasHiddenSpace();
	}
}
