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
package instances.JiniaGuildHideout4;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import org.l2jmobius.gameserver.model.quest.QuestState;

import instances.AbstractInstance;
import quests.Q10287_StoryOfThoseLeft.Q10287_StoryOfThoseLeft;

/**
 * Jinia Guild Hideout instance zone.
 * @author Adry_85
 */
public class JiniaGuildHideout4 extends AbstractInstance
{
	// NPC
	private static final int RAFFORTY = 32020;
	// Location
	private static final Location START_LOC = new Location(-23530, -8963, -5413, 0, 0);
	// Misc
	private static final int TEMPLATE_ID = 146;
	
	private JiniaGuildHideout4()
	{
		addStartNpc(RAFFORTY);
		addTalkId(RAFFORTY);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		final QuestState qs = talker.getQuestState(Q10287_StoryOfThoseLeft.class.getSimpleName());
		if ((qs != null) && qs.isMemoState(1))
		{
			enterInstance(talker, TEMPLATE_ID);
			qs.setCond(2, true);
		}
		return super.onTalk(npc, talker);
	}
	
	@Override
	public void onEnterInstance(Player player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player);
		}
		teleportPlayer(player, START_LOC, world.getInstanceId(), false);
	}
	
	public static void main(String[] args)
	{
		new JiniaGuildHideout4();
	}
}
