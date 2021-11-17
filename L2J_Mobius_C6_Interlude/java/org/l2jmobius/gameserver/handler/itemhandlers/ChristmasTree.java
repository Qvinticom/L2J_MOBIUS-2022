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
package org.l2jmobius.gameserver.handler.itemhandlers;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.model.spawn.Spawn;

public class ChristmasTree implements IItemHandler
{
	private static final int[] ITEM_IDS =
	{
		5560, /* x-mas tree */
		5561, /* Special x-mas tree */
	};
	
	private static final int[] NPC_IDS =
	{
		13006, /* Christmas tree w. flashing lights and snow */
		13007
	};
	
	@Override
	public void useItem(Playable playable, Item item)
	{
		final Player player = (Player) playable;
		NpcTemplate template1 = null;
		
		final int itemId = item.getItemId();
		for (int i = 0; i < ITEM_IDS.length; i++)
		{
			if (ITEM_IDS[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(NPC_IDS[i]);
				break;
			}
		}
		
		if (template1 == null)
		{
			return;
		}
		
		WorldObject target = player.getTarget();
		if (target == null)
		{
			target = player;
		}
		
		try
		{
			final Spawn spawn = new Spawn(template1);
			spawn.setId(IdManager.getInstance().getNextId());
			spawn.setX(target.getX());
			spawn.setY(target.getY());
			spawn.setZ(target.getZ());
			final Npc result = spawn.doSpawn();
			player.destroyItem("Consume", item.getObjectId(), 1, null, false);
			ThreadPool.schedule(new DeSpawn(result), 3600000);
		}
		catch (Exception e)
		{
			player.sendMessage("Target is not ingame.");
		}
	}
	
	public class DeSpawn implements Runnable
	{
		Npc _npc = null;
		
		public DeSpawn(Npc npc)
		{
			_npc = npc;
		}
		
		@Override
		public void run()
		{
			_npc.onDecay();
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}
