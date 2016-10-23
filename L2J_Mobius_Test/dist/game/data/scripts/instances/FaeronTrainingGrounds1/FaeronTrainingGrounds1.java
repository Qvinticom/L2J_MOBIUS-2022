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
package instances.FaeronTrainingGrounds1;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;
import quests.Q10735_ASpecialPower.Q10735_ASpecialPower;

/**
 * Fearon Training Grounds Instance Zone.
 * @author Sdw
 */
public final class FaeronTrainingGrounds1 extends AbstractInstance
{
	// NPCs
	private static final int AYANTHE = 33942;
	private static final int AYANTHE_2 = 33944;
	// Locations
	private static final Location START_LOC = new Location(-74903, 240618, -3584);
	private static final Location EXIT_LOC = new Location(-82088, 249880, -3392);
	// Misc
	private static final int TEMPLATE_ID = 251;
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		final QuestState qs = player.getQuestState(Q10735_ASpecialPower.class.getSimpleName());
		if (qs == null)
		{
			return null;
		}
		
		if (event.equals("enter_instance"))
		{
			enterInstance(player, "FaeronTrainingGrounds1.xml", TEMPLATE_ID);
		}
		else if (event.equals("exit_instance"))
		{
			final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
			world.removeAllowed(player.getObjectId());
			teleportPlayer(player, EXIT_LOC, 0);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	public FaeronTrainingGrounds1()
	{
		super(FaeronTrainingGrounds1.class.getSimpleName());
		addStartNpc(AYANTHE, AYANTHE_2);
		addTalkId(AYANTHE, AYANTHE_2);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.addAllowed(player.getObjectId());
			showOnScreenMsg(player, NpcStringId.TALK_TO_MAGISTER_AYANTHE, ExShowScreenMessage.TOP_CENTER, 4500);
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
}