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
package org.l2jmobius.gameserver.model.quest;

import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.spawn.Spawn;

/**
 * @author programmos
 */
public class QuestSpawn
{
	private static final Logger LOGGER = Quest.LOGGER;
	
	private class DeSpawnScheduleTimerTask implements Runnable
	{
		Npc _npc = null;
		
		public DeSpawnScheduleTimerTask(Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			_npc.onDecay();
		}
	}
	
	/**
	 * Add spawn for player instance Will despawn after the spawn length expires Uses player's coords and heading. Adds a little randomization in the x y coords Return object id of newly spawned npc
	 * @param npcId
	 * @param creature
	 * @return
	 */
	public Npc addSpawn(int npcId, Creature creature)
	{
		return addSpawn(npcId, creature.getX(), creature.getY(), creature.getZ(), creature.getHeading(), false, 0);
	}
	
	/**
	 * Add spawn for player instance Return object id of newly spawned npc
	 * @param npcId
	 * @param xValue
	 * @param yValue
	 * @param zValue
	 * @param heading
	 * @param randomOffset
	 * @param despawnDelay
	 * @return
	 */
	public Npc addSpawn(int npcId, int xValue, int yValue, int zValue, int heading, boolean randomOffset, int despawnDelay)
	{
		Npc result = null;
		
		try
		{
			final NpcTemplate template = NpcTable.getInstance().getTemplate(npcId);
			if (template != null)
			{
				// Sometimes, even if the quest script specifies some xyz (for example npc.getX() etc) by the time the code reaches here, xyz have become 0! Also, a questdev might have purposely set xy to 0,0...however,
				// the spawn code is coded such that if x=y=0, it looks into location for the spawn loc! This will NOT work with quest spawns! For both of the above cases, we need a fail-safe spawn. For this, we use the default spawn location, which is at the player's loc.
				if ((xValue == 0) && (yValue == 0))
				{
					LOGGER.warning("Failed to adjust bad locks for quest spawn!  Spawn aborted!");
					return null;
				}
				
				int x = xValue;
				int y = yValue;
				if (randomOffset)
				{
					int offset;
					
					// Get the direction of the offset
					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					
					// make offset negative
					offset *= Rnd.get(50, 100);
					x += offset;
					
					// Get the direction of the offset
					offset = Rnd.get(2);
					if (offset == 0)
					{
						offset = -1;
					}
					
					// make offset negative
					offset *= Rnd.get(50, 100);
					y += offset;
				}
				
				final Spawn spawn = new Spawn(template);
				spawn.setHeading(heading);
				spawn.setX(x);
				spawn.setY(y);
				spawn.setZ(zValue + 20);
				spawn.stopRespawn();
				result = spawn.doSpawn();
				if (despawnDelay > 0)
				{
					ThreadPool.schedule(new DeSpawnScheduleTimerTask(result), despawnDelay);
				}
				
				return result;
			}
		}
		catch (Exception e1)
		{
			LOGGER.warning("Could not spawn Npc " + npcId);
		}
		
		return null;
	}
	
	public static QuestSpawn getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final QuestSpawn INSTANCE = new QuestSpawn();
	}
}
