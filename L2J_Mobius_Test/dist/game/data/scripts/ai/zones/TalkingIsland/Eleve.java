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
 * Eleve AI.
 * @author Gladicek
 */
public final class Eleve extends AbstractNpcAI
{
	// NPC
	private static final int ELEVE = 33246;
	// Misc
	private static final NpcStringId[] ELEVE_SHOUT =
	{
		NpcStringId.DON_T_KNOW_WHAT_TO_DO_LOOK_AT_THE_MAP,
		NpcStringId.DO_YOU_SEE_A_SCROLL_ICON_GO_THAT_LOCATION
	};
	private static final Location[] ELEVE_LOC =
	{
		new Location(-114936, 259918, -1203),
		new Location(-114687, 259872, -1203),
		new Location(-114552, 259699, -1203),
		new Location(-114689, 259453, -1203),
		new Location(-114990, 259335, -1203),
		new Location(-115142, 259523, -1203),
		new Location(-114894, 259137, -1203),
		new Location(-114832, 259363, -1203),
		new Location(-114809, 259260, -1203),
		new Location(-115036, 260006, -1203),
	};
	
	private Eleve()
	{
		super(Eleve.class.getSimpleName(), "ai/zones/TalkingIsland");
		addSpawnId(ELEVE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equalsIgnoreCase("npc_move") && (npc != null))
		{
			broadcastNpcSay(npc, ChatType.NPC_GENERAL, ELEVE_SHOUT[getRandom(2)], 1000);
			if (getRandom(100) > 40)
			{
				addMoveToDesire(npc, ELEVE_LOC[getRandom(10)], 0);
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(L2Npc npc)
	{
		startQuestTimer("npc_move", 6000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Eleve();
	}
}