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

import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Handermonkey AI.
 * @author Gladicek
 */
public class Handermonkey extends AbstractNpcAI
{
	// NPC
	private static final int HANDERMONKEY = 33203;
	
	private Handermonkey()
	{
		addSpawnId(HANDERMONKEY);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("NPC_MOVE") && (npc != null))
		{
			if (getRandom(100) < 70)
			{
				final int x = npc.getSpawn().getX() + (getRandom(-100, 100));
				final int y = npc.getSpawn().getY() + (getRandom(-100, 100));
				final Location loc = GeoEngine.getInstance().getValidLocation(npc.getX(), npc.getY(), npc.getZ(), x, y, npc.getZ(), npc.getInstanceWorld());
				addMoveToDesire(npc, loc, 0);
			}
			else
			{
				npc.broadcastSocialAction(9);
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		npc.setRunning();
		cancelQuestTimer("NPC_MOVE", npc, null);
		startQuestTimer("NPC_MOVE", 5000, npc, null, true);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new Handermonkey();
	}
}