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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * sample 27 00 00 01 00 // item count 04 00 // itemType1 0-weapon/ring/earring/necklace 1-armor/shield 4-item/questitem/adena c6 37 50 40 // objectId cd 09 00 00 // itemId 05 00 00 00 // count 05 00 // itemType2 0-weapon 1-shield/armor 2-ring/earring/necklace 3-questitem 4-adena 5-item 00 00 //
 * always 0 ?? 00 00 // equipped 1-yes 00 00 // slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand 00 00 // always 0 ?? 00 00 // always 0 ?? format h (h dddhhhh hh) revision 377 format h (h dddhhhd hh)
 * revision 415
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:57 $
 */
public class ItemList implements IClientOutgoingPacket
{
	private final Collection<Item> _items;
	private final boolean _showWindow;
	
	public ItemList(Player player, boolean showWindow)
	{
		_items = player.getInventory().getItems();
		_showWindow = showWindow;
	}
	
	public ItemList(Collection<Item> items, boolean showWindow)
	{
		_items = items;
		_showWindow = showWindow;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.ITEM_LIST.writeId(packet);
		packet.writeH(_showWindow ? 0x01 : 0x00);
		packet.writeH(_items.size());
		for (Item temp : _items)
		{
			if ((temp == null) || (temp.getItem() == null))
			{
				continue;
			}
			packet.writeH(temp.getItem().getType1());
			packet.writeD(temp.getObjectId());
			packet.writeD(temp.getItemId());
			packet.writeD(temp.getCount());
			packet.writeH(temp.getItem().getType2());
			packet.writeH(temp.getCustomType1());
			packet.writeH(temp.isEquipped() ? 0x01 : 0x00);
			packet.writeD(temp.getItem().getBodyPart());
			packet.writeH(temp.getEnchantLevel());
			// race tickets
			packet.writeH(temp.getCustomType2());
		}
		return true;
	}
}