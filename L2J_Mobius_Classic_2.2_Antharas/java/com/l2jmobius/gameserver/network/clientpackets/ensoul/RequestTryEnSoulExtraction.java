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
package com.l2jmobius.gameserver.network.clientpackets.ensoul;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.EnsoulData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.ensoul.EnsoulOption;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.ensoul.ExEnSoulExtractionResult;

/**
 * @author Mobius
 */
public class RequestTryEnSoulExtraction implements IClientIncomingPacket
{
	private int _itemObjectId;
	private int _type;
	private int _position;
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_itemObjectId = packet.readD();
		_type = packet.readC();
		_position = packet.readC() - 1;
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		L2PcInstance player = client.getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2ItemInstance item = player.getInventory().getItemByObjectId(_itemObjectId);
		if (item == null)
		{
			return;
		}
		
		EnsoulOption option = null;
		if (_type == 1)
		{
			option = item.getSpecialAbility(_position);
		}
		if (_type == 2)
		{
			option = item.getAdditionalSpecialAbility(_position);
		}
		if (option == null)
		{
			return;
		}
		
		boolean success = false;
		
		// TODO: Move to XML.
		switch (item.getItem().getItemGrade())
		{
			case D:
			{
				if (player.getInventory().getInventoryItemCount(2130, -1) >= 89)
				{
					player.destroyItemByItemId("Rune Extract", 2130, 89, player, true);
					success = true;
				}
				break;
			}
			case C:
			{
				if (player.getInventory().getInventoryItemCount(2131, -1) >= 89)
				{
					player.destroyItemByItemId("Rune Extract", 2131, 89, player, true);
					success = true;
				}
				break;
			}
			case B:
			{
				if ((player.getInventory().getInventoryItemCount(2132, -1) >= 19) //
					&& (player.getInventory().getInventoryItemCount(57, -1) >= 700000))
				{
					player.destroyItemByItemId("Rune Extract", 2132, 19, player, true);
					player.reduceAdena("Rune Extract", 700000, player, true);
					success = true;
				}
				break;
			}
			case A:
			{
				if ((player.getInventory().getInventoryItemCount(2133, -1) >= 5) //
					&& (player.getInventory().getInventoryItemCount(57, -1) >= 3500000))
				{
					player.destroyItemByItemId("Rune Extract", 2133, 5, player, true);
					player.reduceAdena("Rune Extract", 3500000, player, true);
					success = true;
				}
				break;
			}
		}
		
		if (success)
		{
			item.removeSpecialAbility(_position, _type);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(item);
			
			final int runeId = EnsoulData.getInstance().getStone(_type, option.getId());
			if (runeId > 0)
			{
				iu.addItem(player.addItem("Rune Extract", runeId, 1, player, true));
			}
			
			player.sendInventoryUpdate(iu);
		}
		else
		{
			player.sendPacket(SystemMessageId.INCORRECT_ITEM_COUNT);
		}
		
		player.sendPacket(new ExEnSoulExtractionResult(success, item));
	}
}