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
package ai.others;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class RandomWalkingGuards extends AbstractNpcAI
{
	private static final int[] GUARDS =
	{
		31032, // talking island
		31033, // elf village
		31034, // dark elf village
		31036, // orc village
		31035, // dwarf village
	};
	// Others
	private static final int MIN_WALK_DELAY = 15000;
	private static final int MAX_WALK_DELAY = 45000;
	
	private RandomWalkingGuards()
	{
		addSpawnId(GUARDS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("RANDOM_WALK") && (npc != null))
		{
			if (!npc.isInCombat())
			{
				final Location randomLoc = Util.getRandomPosition(npc.getSpawn().getLocation(), 0, Config.MAX_DRIFT_RANGE);
				addMoveToDesire(npc, GeoEngine.getInstance().getValidLocation(npc.getX(), npc.getY(), npc.getZ(), randomLoc.getX(), randomLoc.getY(), randomLoc.getZ(), npc.getInstanceWorld()), 23);
			}
			startQuestTimer("RANDOM_WALK", getRandom(MIN_WALK_DELAY, MAX_WALK_DELAY), npc, null);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		startQuestTimer("RANDOM_WALK", getRandom(MIN_WALK_DELAY, MAX_WALK_DELAY), npc, null);
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new RandomWalkingGuards();
	}
}
