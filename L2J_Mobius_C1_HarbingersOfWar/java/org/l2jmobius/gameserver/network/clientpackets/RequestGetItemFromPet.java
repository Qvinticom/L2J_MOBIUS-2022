/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.clientpackets;

import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.PetInventoryUpdate;

public class RequestGetItemFromPet extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestGetItemFromPet.class.getName());
	
	public RequestGetItemFromPet(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int objectId = readD();
		final int amount = readD();
		@SuppressWarnings("unused")
		final int noidea = readD();
		final Inventory petInventory = client.getActiveChar().getPet().getInventory();
		final Inventory playerInventory = client.getActiveChar().getInventory();
		final ItemInstance petItem = petInventory.getItem(objectId);
		if (petItem == null)
		{
			_log.warning("item requested from pet, but its not there.");
			return;
		}
		if (amount >= petItem.getCount())
		{
			playerInventory.addItem(petItem);
			petInventory.destroyItem(objectId, petItem.getCount());
			final PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addRemovedItem(petItem);
			client.getActiveChar().sendPacket(petiu);
			final ItemList playerUI = new ItemList(client.getActiveChar(), false);
			client.getActiveChar().sendPacket(playerUI);
		}
		else
		{
			final int total = petItem.getCount();
			final ItemInstance newPlayerItem = petInventory.dropItem(objectId, total);
			final PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addModifiedItem(petItem);
			playerInventory.addItem(newPlayerItem);
			final ItemList playerUI = new ItemList(client.getActiveChar(), false);
			client.getActiveChar().sendPacket(petiu);
			client.getActiveChar().sendPacket(playerUI);
		}
	}
}
