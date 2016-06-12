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
package ai.group;

import com.l2jmobius.gameserver.instancemanager.WalkingManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.AbstractNpcAI;

/**
 * Ruins Of Ye Sagira AI.
 * @author St3eT
 */
public final class RuinsOfYeSagira extends AbstractNpcAI
{
	// NPC
	private static final int GUARD = 33119;
	// Locations
	private static final Location GUARD_LOC = new Location(-115201, 237363, -3088);
	// Misc
	private static final String ROUTE_NAME1 = "ye_segira_guard1";
	private static final String ROUTE_NAME2 = "ye_segira_guard2";
	
	private RuinsOfYeSagira()
	{
		addRouteFinishedId(GUARD);
		startQuestTimer("SPAWN_FIRST", 15000, null, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "SPAWN_FIRST":
			{
				final L2Npc guard = addSpawn(GUARD, GUARD_LOC);
				WalkingManager.getInstance().startMoving(guard, ROUTE_NAME1);
				startQuestTimer("SPAWN_SECOND", 4000, null, null);
				break;
			}
			case "SPAWN_SECOND":
			{
				final L2Npc guard = addSpawn(GUARD, GUARD_LOC);
				WalkingManager.getInstance().startMoving(guard, ROUTE_NAME2);
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public void onRouteFinished(L2Npc npc)
	{
		npc.deleteMe();
	}
	
	public static void main(String[] args)
	{
		new RuinsOfYeSagira();
	}
}
