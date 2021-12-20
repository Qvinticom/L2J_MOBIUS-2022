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
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.multisell.MultiSellEntry;
import org.l2jmobius.gameserver.model.multisell.MultiSellIngredient;
import org.l2jmobius.gameserver.model.multisell.MultiSellListContainer;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 */
public class MultiSellList implements IClientOutgoingPacket
{
	protected int _listId;
	protected int _page;
	protected int _finished;
	protected MultiSellListContainer _list;
	
	public MultiSellList(MultiSellListContainer list, int page, int finished)
	{
		_list = list;
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		// [ddddd] [dchh] [hdhdh] [hhdh]
		OutgoingPackets.MULTI_SELL_LIST.writeId(packet);
		packet.writeD(_listId); // list id
		packet.writeD(_page); // page
		packet.writeD(_finished); // finished
		packet.writeD(0x28); // size of pages
		packet.writeD(_list == null ? 0 : _list.getEntries().size()); // list length
		if (_list != null)
		{
			for (MultiSellEntry ent : _list.getEntries())
			{
				packet.writeD(ent.getEntryId());
				packet.writeD(0); // C6
				packet.writeD(0); // C6
				packet.writeC(1);
				packet.writeH(ent.getProducts().size());
				packet.writeH(ent.getIngredients().size());
				for (MultiSellIngredient i : ent.getProducts())
				{
					packet.writeH(i.getItemId());
					packet.writeD(ItemTable.getInstance().getTemplate(i.getItemId()).getBodyPart());
					packet.writeH(ItemTable.getInstance().getTemplate(i.getItemId()).getType2());
					packet.writeD(i.getItemCount());
					packet.writeH(i.getEnchantmentLevel()); // enchtant level
					packet.writeD(0); // C6
					packet.writeD(0); // C6
				}
				for (MultiSellIngredient i : ent.getIngredients())
				{
					final int items = i.getItemId();
					int typeE = 65335;
					if ((items != 65336) && (items != 65436))
					{
						typeE = ItemTable.getInstance().getTemplate(i.getItemId()).getType2();
					}
					packet.writeH(items); // ID
					packet.writeH(typeE);
					packet.writeD(i.getItemCount()); // Count
					packet.writeH(i.getEnchantmentLevel()); // Enchant Level
					packet.writeD(0); // C6
					packet.writeD(0); // C6
				}
			}
		}
		return true;
	}
}
