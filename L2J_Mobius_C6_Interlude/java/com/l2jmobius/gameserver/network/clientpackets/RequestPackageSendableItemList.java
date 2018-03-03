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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.PackageSendableList;

/**
 * Format: (c)d d: char object id (?)
 * @author -Wooden-
 */
public final class RequestPackageSendableItemList extends L2GameClientPacket
{
	private int _objectID;
	
	@Override
	protected void readImpl()
	{
		_objectID = readD();
	}
	
	@Override
	public void runImpl()
	{
		
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		if (player.getObjectId() == _objectID)
		{
			return;
		}
		
		if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("deposit"))
		{
			player.sendMessage("You depositing items too fast.");
			return;
		}
		
		/*
		 * L2PcInstance target = (L2PcInstance) L2World.getInstance().findObject(_objectID); if(target == null) return;
		 */
		final L2ItemInstance[] items = getClient().getActiveChar().getInventory().getAvailableItems(true);
		// build list...
		sendPacket(new PackageSendableList(items, _objectID));
	}
}
