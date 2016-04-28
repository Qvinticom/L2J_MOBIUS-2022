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
package ai.npc.Teleports.CrumaTower;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.AbstractNpcAI;

/**
 * Cruma Tower teleport AI.
 * @author Stayway
 */
final class CrumaTower extends AbstractNpcAI
{
	// NPC
	private static final int CARSUS = 30483;
	// Locations
	private static final Location TELEPORT_LOC1 = new Location(17664, 108288, -9056);
	private static final Location TELEPORT_LOC2 = new Location(17729, 114808, -11696);
	// Misc
	private static final int MAX_LEVEL = 55;
	
	private CrumaTower()
	{
		super(CrumaTower.class.getSimpleName(), "ai/npc/Teleports");
		addFirstTalkId(CARSUS);
		addStartNpc(CARSUS);
		addTalkId(CARSUS);
	}
	
	@Override
	public final String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("chat1"))
		{
			if (player.getLevel() <= MAX_LEVEL)
			{
				player.teleToLocation(TELEPORT_LOC1, true);
				return null;
			}
			return "30483-1.html";
		}
		else if (event.equals("chat2"))
		{
			if (player.getLevel() <= MAX_LEVEL)
			{
				player.teleToLocation(TELEPORT_LOC2, true);
				return null;
			}
			return "30483-1.html";
		}
		return event;
	}
	
	public static void main(String[] args)
	{
		new CrumaTower();
	}
}