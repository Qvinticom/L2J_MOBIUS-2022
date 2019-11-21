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

import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.PetItemList;

public class RequestGiveItemToPet extends ClientBasePacket
{
	private static final String REQUESTGETITEMFROMPET__C__8C = "[C] 8C RequestGetItemFromPet";
	
	public RequestGiveItemToPet(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int objectId = readD();
		int amount = readD();
		Inventory petInventory = client.getActiveChar().getPet().getInventory();
		Inventory playerInventory = client.getActiveChar().getInventory();
		ItemInstance playerItem = playerInventory.getItem(objectId);
		if (amount >= playerItem.getCount())
		{
			playerInventory.dropItem(objectId, playerItem.getCount());
			petInventory.addItem(playerItem);
			InventoryUpdate playerUI = new InventoryUpdate();
			playerUI.addRemovedItem(playerItem);
			client.getActiveChar().sendPacket(playerUI);
			PetItemList petiu = new PetItemList(client.getActiveChar().getPet());
			client.getActiveChar().sendPacket(petiu);
		}
		else
		{
			ItemInstance newPetItem = playerInventory.dropItem(objectId, amount);
			petInventory.addItem(newPetItem);
			PetItemList petiu = new PetItemList(client.getActiveChar().getPet());
			InventoryUpdate playerUI = new InventoryUpdate();
			playerUI.addModifiedItem(playerItem);
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
