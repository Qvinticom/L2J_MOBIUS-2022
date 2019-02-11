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
package instances.MysticTavern;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.QuestState;

import ai.AbstractNpcAI;
import instances.MysticTavern.StoryOfFreya.StoryOfFreya;
import quests.Q10297_GrandOpeningComeToOurPub.Q10297_GrandOpeningComeToOurPub;

/**
 * This AI manages the entry to the Mystic Tavern instances.
 * @URL https://l2wiki.com/Mystic_Tavern
 * @VIDEO FREYA: https://www.youtube.com/watch?v=-pUB6ghrsLI
 * @VIDEO TAUTI: https://www.youtube.com/watch?v=_Wz-kxXzJK4
 * @VIDEO KELBIM: https://www.youtube.com/watch?v=wL1D49u6vxE
 * @author Mobius
 */
public class MysticTavern extends AbstractNpcAI
{
	// NPC
	private static final int GLOBE = 34200;
	// Instances
	private static final int INSTANCE_FREYA = 261;
	// private static final int INSTANCE_TAUTI = 262;
	// private static final int INSTANCE_KELBIM = 263;
	// Misc
	private static final int MINIMUM_PLAYER_LEVEL = 99;
	private static final int MINIMUM_PARTY_MEMBERS = 5;
	
	public MysticTavern()
	{
		addFirstTalkId(GLOBE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("tellStory"))
		{
			final List<Integer> availableInstances = new ArrayList<>();
			availableInstances.add(INSTANCE_FREYA);
			// availableInstances.add(INSTANCE_TAUTI);
			// availableInstances.add(INSTANCE_KELBIM);
			if (!player.isGM())
			{
				final L2Party party = player.getParty();
				if (party == null)
				{
					return "34200-no-party.html";
				}
				if (party.getLeader() != player)
				{
					return "34200-no-leader.html";
				}
				if (party.getMemberCount() < MINIMUM_PARTY_MEMBERS)
				{
					return "34200-not-enough-members.html";
				}
				for (L2PcInstance member : party.getMembers())
				{
					if ((member == null) || !member.isSitting() || (member.calculateDistance3D(player) > 500))
					{
						return "34200-not-sitting.html";
					}
					if (member.getLevel() < MINIMUM_PLAYER_LEVEL)
					{
						return "34200-no-level.html";
					}
					final QuestState qs = member.getQuestState(Q10297_GrandOpeningComeToOurPub.class.getSimpleName());
					if ((qs == null) || !qs.isCompleted())
					{
						return "34200-no-quest.html";
					}
					if (InstanceManager.getInstance().getInstanceTime(member, INSTANCE_FREYA) > 0)
					{
						availableInstances.remove(INSTANCE_FREYA);
					}
					// if (InstanceManager.getInstance().getInstanceTime(member, INSTANCE_TAUTI) > 0)
					// {
					// availableInstances.remove(INSTANCE_TAUTI);
					// }
					// if (InstanceManager.getInstance().getInstanceTime(member, INSTANCE_KELBIM) > 0)
					// {
					// availableInstances.remove(INSTANCE_KELBIM);
					// }
				}
				if (availableInstances.isEmpty())
				{
					return "34200-not-available.html";
				}
			}
			// enter randomly
			switch (availableInstances.get(getRandom(availableInstances.size())))
			{
				case INSTANCE_FREYA:
				{
					player.processQuestEvent(StoryOfFreya.class.getSimpleName(), "start_story");
					break;
				}
				// case INSTANCE_TAUTI:
				// {
				// player.processQuestEvent(StoryOfTauti.class.getSimpleName(), "start_story");
				// break;
				// }
				// case INSTANCE_KELBIM:
				// {
				// player.processQuestEvent(StoryOfKelbim.class.getSimpleName(), "start_story");
				// break;
				// }
			}
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "34200.html";
	}
	
	public static void main(String[] args)
	{
		new MysticTavern();
	}
}
