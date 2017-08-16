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
package ai.areas.TalkingIsland;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Rubentis AI.
 * @author St3eT
 */
public final class Rubentis extends AbstractNpcAI
{
	// NPC
	private static final int RUBENTIS = 33120;
	
	private Rubentis()
	{
		addSpawnId(RUBENTIS);
	}
	
	@Override
	public void onTimerEvent(String event, StatsSet params, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("NPC_MOVE"))
		{
			if (getRandomBoolean())
			{
				addMoveToDesire(npc, GeoData.getInstance().moveCheck(npc.getLocation(), Util.getRandomPosition(npc.getSpawn().getLocation(), 0, 500), npc.getInstanceWorld()), 23);
			}
			getTimers().addTimer("NPC_MOVE", 10000 + (getRandom(5) * 1000), npc, null);
		}
		else if (event.equals("NPC_SHOUT"))
		{
			final int rand = getRandom(3);
			if (rand == 0)
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.HUNTING_AT_THE_BEACH_IS_A_BAD_IDEA);
			}
			else if (rand == 1)
			{
				npc.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.ONLY_THE_STRONG_SURVIVE_AT_RUINS_OF_YE_SAGIRA);
			}
			getTimers().addTimer("NPC_SHOUT", 10000 + (getRandom(5) * 1000), npc, null);
		}
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		getTimers().addTimer("NPC_MOVE", 10000 + (getRandom(5) * 1000), npc, null);
		getTimers().addTimer("NPC_SHOUT", 10000 + (getRandom(5) * 1000), npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Rubentis();
	}
}