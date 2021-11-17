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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.ManufactureItem;
import org.l2jmobius.gameserver.model.ManufactureList;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class RecipeShopSellList implements IClientOutgoingPacket
{
	private final Player _buyer;
	private final Player _player;
	
	public RecipeShopSellList(Player buyer, Player player)
	{
		_buyer = buyer;
		_player = player;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		final ManufactureList createList = _player.getCreateList();
		if (createList != null)
		{
			OutgoingPackets.RECIPE_SHOP_SELL_LIST.writeId(packet);
			packet.writeD(_player.getObjectId());
			packet.writeD((int) _player.getCurrentMp()); // Creator's MP
			packet.writeD(_player.getMaxMp()); // Creator's MP
			packet.writeD(_buyer.getAdena()); // Buyer Adena
			packet.writeD(createList.size());
			
			for (ManufactureItem item : createList.getList())
			{
				packet.writeD(item.getRecipeId());
				packet.writeD(0x00); // unknown
				packet.writeD(item.getCost());
			}
		}
		return true;
	}
}
