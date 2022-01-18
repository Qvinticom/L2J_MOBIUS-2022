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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ItemLocation;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.4.2.3.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class SellList implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _money;
	private final List<Item> _selllist = new ArrayList<>();
	
	public SellList(Player player)
	{
		_player = player;
		_money = _player.getAdena();
		for (Item item : _player.getInventory().getItems())
		{
			if ((item != null) && !item.isEquipped() && // Not equipped
				(item.getItemLocation() == ItemLocation.INVENTORY) && // exploit fix
				item.getItem().isSellable() && // Item is sellable
				(item.getItem().getItemId() != 57) && // Adena is not sellable
				((_player.getPet() == null) || // Pet not summoned or
					(item.getObjectId() != _player.getPet().getControlItemId()))) // Pet is summoned and not the item that summoned the pet
			{
				_selllist.add(item);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SELL_LIST.writeId(packet);
		packet.writeD(_money);
		packet.writeD(0);
		packet.writeH(_selllist.size());
		for (Item item : _selllist)
		{
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItemId());
			packet.writeD(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(0);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getEnchantLevel());
			packet.writeH(0);
			packet.writeH(0);
			packet.writeD(Config.MERCHANT_ZERO_SELL_PRICE ? 0 : item.getItem().getReferencePrice() / 2);
		}
		return true;
	}
}