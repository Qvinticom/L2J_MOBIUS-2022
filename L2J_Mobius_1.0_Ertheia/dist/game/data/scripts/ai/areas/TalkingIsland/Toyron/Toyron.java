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
package ai.areas.TalkingIsland.Toyron;

import org.l2jmobius.gameserver.instancemanager.InstanceManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.instancezone.Instance;
import org.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;
import quests.Q10327_IntruderWhoWantsTheBookOfGiants.Q10327_IntruderWhoWantsTheBookOfGiants;

/**
 * Toyron AI.
 * @author Gladicek
 */
public class Toyron extends AbstractNpcAI
{
	// NPC
	private static final int TOYRON = 33004;
	// Misc
	private static final int TEMPLATE_ID = 182;
	// Location
	private static final Location MUSEUM_OUT = new Location(-114359, 260120, -1192);
	
	private Toyron()
	{
		addStartNpc(TOYRON);
		addFirstTalkId(TOYRON);
		addTalkId(TOYRON);
		addSpawnId(TOYRON);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		final Instance world = InstanceManager.getInstance().getPlayerInstance(player, true);
		if (event.equals("museum_teleport"))
		{
			if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
			{
				world.finishInstance(0);
			}
			player.teleToLocation(MUSEUM_OUT);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setInvul(true);
		return super.onSpawn(npc);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		String htmltext = "33004.html";
		final Instance world = npc.getInstanceWorld();
		if ((world != null) && (world.getTemplateId() == TEMPLATE_ID))
		{
			final QuestState qs = player.getQuestState(Q10327_IntruderWhoWantsTheBookOfGiants.class.getSimpleName());
			if (qs != null)
			{
				switch (qs.getCond())
				{
					case 1:
					{
						htmltext = "33004-01.html";
						break;
					}
					case 2:
					{
						htmltext = "33004-02.html";
						break;
					}
				}
			}
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Toyron();
	}
}