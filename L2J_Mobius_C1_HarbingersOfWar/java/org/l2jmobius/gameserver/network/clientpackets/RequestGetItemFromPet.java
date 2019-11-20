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

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.PetInventoryUpdate;

public class RequestGetItemFromPet extends ClientBasePacket
{
	private static final String REQUESTGETITEMFROMPET__C__8C = "[C] 8C RequestGetItemFromPet";
	
	public RequestGetItemFromPet(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int objectId = readD();
		int amount = readD();
		@SuppressWarnings("unused")
		int noidea = readD();
		Inventory petInventory = client.getActiveChar().getPet().getInventory();
		Inventory playerInventory = client.getActiveChar().getInventory();
		ItemInstance petItem = petInventory.getItem(objectId);
		if (petItem == null)
		{
			_log.warning("item requested from pet, but its not there.");
			return;
		}
		if (amount >= petItem.getCount())
		{
			playerInventory.addItem(petItem);
			petInventory.destroyItem(objectId, petItem.getCount());
			PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addRemovedItem(petItem);
			client.getActiveChar().sendPacket(petiu);
			ItemList playerUI = new ItemList(client.getActiveChar(), false);
			client.getActiveChar().sendPacket(playerUI);
		}
		else
		{
			int total = petItem.getCount();
			ItemInstance newPlayerItem = petInventory.dropItem(objectId, total);
			PetInventoryUpdate petiu = new PetInventoryUpdate();
			petiu.addModifiedItem(petItem);
			playerInventory.addItem(newPlayerItem);
			ItemList playerUI = new ItemList(client.getActiveChar(), false);
			client.getActiveChar().sendPacket(petiu);
			client.getActiveChar().sendPacket(playerUI);
		}
	}
	
	@Override
	public String getType()
	{
		return REQUESTGETITEMFROMPET__C__8C;
	}
}
