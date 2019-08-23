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

import org.l2jmobius.gameserver.datatables.sql.NpcTable;
import org.l2jmobius.gameserver.handler.IItemHandler;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Playable;
import org.l2jmobius.gameserver.model.actor.instance.GourdInstance;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class JackpotSeed implements IItemHandler
{
	private GourdInstance _gourd = null;
	
	private static int[] _itemIds =
	{
		6389, // small seed
		6390, // large seed
	};
	
	private static int[] _npcIds =
	{
		12774, // Young Pumpkin
		12777, // Large Young Pumpkin
	};
	
	@Override
	public void useItem(Playable playable, ItemInstance item)
	{
		PlayerInstance player = (PlayerInstance) playable;
		NpcTemplate template1 = null;
		final int itemId = item.getItemId();
		for (int i = 0; i < _itemIds.length; i++)
		{
			if (_itemIds[i] == itemId)
			{
				template1 = NpcTable.getInstance().getTemplate(_npcIds[i]);
				break;
			}
		}
		
		if (template1 == null)
		{
			return;
		}
		
		try
		{
			final Spawn spawn = new Spawn(template1);
			spawn.setId(IdFactory.getInstance().getNextId());
			spawn.setX(player.getX());
			spawn.setY(player.getY());
			spawn.setZ(player.getZ());
			_gourd = (GourdInstance) spawn.doSpawn();
			World.getInstance().storeObject(_gourd);
			_gourd.setOwner(player.getName());
			player.destroyItem("Consume", item.getObjectId(), 1, null, false);
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Created " + template1.name + " at x: " + spawn.getX() + " y: " + spawn.getY() + " z: " + spawn.getZ());
			player.sendPacket(sm);
		}
		catch (Exception e)
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
			sm.addString("Target is not ingame.");
			player.sendPacket(sm);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
