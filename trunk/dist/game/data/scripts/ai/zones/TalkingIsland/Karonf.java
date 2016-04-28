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
package ai.zones.TalkingIsland;

import com.l2jmobius.gameserver.enums.ChatType;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Karonf AI.
 * @author Gladicek
 */
final class Karonf extends AbstractNpcAI
{
	// NPC
	private static final int KARONF = 33242;
	// Misc
	private static final NpcStringId[] KARONF_SHOUT =
	{
		NpcStringId.WHEN_YOU_GO_TO_THE_MUSEUM_SPEAK_TO_PANTHEON,
		NpcStringId.SOME_FOLKS_DON_T_KNOW_WHAT_THEY_ARE_DOING
	};
	private static final Location[] KARONF_LOC =
	{
		new Location(-113984, 259782, -1203),
		new Location(-113786, 259475, -1203),
		new Location(-113977, 259035, -1203),
		new Location(-114012, 259290, -1203),
		new Location(-113812, 259522, -1203),
		new Location(-113621, 259281, -1203),
		new Location(-114354, 259048, -1193),
		new Location(-113864, 259293, -1203),
		new Location(-114052, 259351, -1203),
		new Location(-114175, 259243, -1203),
	};
	
	private Karonf()
	{
		super(Karonf.class.getSimpleName(), "ai/zones/TalkingIsland");
		addSpawnId(KARONF);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("npc_move") && (npc != null))
		{
			if (getRandom(100) > 40)
			{
				broadcastNpcSay(npc, ChatType.NPC_GENERAL, KARONF_SHOUT[getRandom(2)], 1000);
				addMoveToDesire(npc, KARONF_LOC[getRandom(10)], 0);
			}
			else
			{
				broadcastNpcSay(npc, ChatType.NPC_GENERAL, KARONF_SHOUT[getRandom(2)]);
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("npc_move", 8000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Karonf();
	}
}