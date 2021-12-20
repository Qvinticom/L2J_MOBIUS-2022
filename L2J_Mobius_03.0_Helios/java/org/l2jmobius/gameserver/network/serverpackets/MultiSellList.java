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

import static org.l2jmobius.gameserver.data.xml.MultisellData.PAGE_SIZE;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.model.ItemInfo;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import org.l2jmobius.gameserver.model.holders.MultisellEntryHolder;
import org.l2jmobius.gameserver.model.holders.PreparedMultisellListHolder;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class MultiSellList extends AbstractItemPacket
{
	private final Player _player;
	private int _size;
	private int _index;
	private final PreparedMultisellListHolder _list;
	private final boolean _finished;
	
	public MultiSellList(Player player, PreparedMultisellListHolder list, int index)
	{
		_player = player;
		_list = list;
		_index = index;
		_size = list.getEntries().size() - index;
		if (_size > PAGE_SIZE)
		{
			_finished = false;
			_size = PAGE_SIZE;
		}
		else
		{
			_finished = true;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.MULTI_SELL_LIST.writeId(packet);
		packet.writeC(0); // Helios
		packet.writeD(_list.getId()); // list id
		packet.writeC(0); // GOD Unknown
		packet.writeD(1 + (_index / PAGE_SIZE)); // page started from 1
		packet.writeD(_finished ? 1 : 0); // finished
		packet.writeD(PAGE_SIZE); // size of pages
		packet.writeD(_size); // list length
		packet.writeC(_list.isChanceMultisell() ? 1 : 0); // new multisell window
		packet.writeD(32); // Helios - Always 32
		while (_size-- > 0)
		{
			ItemInfo itemEnchantment = _list.getItemEnchantment(_index);
			final MultisellEntryHolder entry = _list.getEntries().get(_index++);
			if ((itemEnchantment == null) && _list.isMaintainEnchantment())
			{
				SEARCH: for (ItemChanceHolder holder : entry.getIngredients())
				{
					final Item item = _player.getInventory().getItemByItemId(holder.getId());
					if ((item != null) && item.isEquipable())
					{
						itemEnchantment = new ItemInfo(item);
						break SEARCH;
					}
				}
			}
			packet.writeD(_index); // Entry ID. Start from 1.
			packet.writeC(entry.isStackable() ? 1 : 0);
			// Those values will be passed down to MultiSellChoose packet.
			packet.writeH(itemEnchantment != null ? itemEnchantment.getEnchantLevel() : 0); // enchant level
			writeItemAugment(packet, itemEnchantment);
			writeItemElemental(packet, itemEnchantment);
			writeItemEnsoulOptions(packet, itemEnchantment);
			packet.writeH(entry.getProducts().size());
			packet.writeH(entry.getIngredients().size());
			for (ItemChanceHolder product : entry.getProducts())
			{
				final ItemTemplate template = ItemTable.getInstance().getTemplate(product.getId());
				final ItemInfo displayItemEnchantment = _list.isMaintainEnchantment() && (itemEnchantment != null) && (template != null) && template.getClass().equals(itemEnchantment.getItem().getClass()) ? itemEnchantment : null;
				if (template != null)
				{
					packet.writeD(template.getDisplayId());
					packet.writeQ(template.getBodyPart());
					packet.writeH(template.getType2());
				}
				else
				{
					packet.writeD(product.getId());
					packet.writeQ(0);
					packet.writeH(65535);
				}
				packet.writeQ(_list.getProductCount(product));
				packet.writeH(product.getEnchantmentLevel() > 0 ? product.getEnchantmentLevel() : displayItemEnchantment != null ? displayItemEnchantment.getEnchantLevel() : 0); // enchant level
				packet.writeD((int) Math.ceil(product.getChance())); // chance
				writeItemAugment(packet, displayItemEnchantment);
				writeItemElemental(packet, displayItemEnchantment);
				writeItemEnsoulOptions(packet, displayItemEnchantment);
			}
			for (ItemChanceHolder ingredient : entry.getIngredients())
			{
				final ItemTemplate template = ItemTable.getInstance().getTemplate(ingredient.getId());
				final ItemInfo displayItemEnchantment = (itemEnchantment != null) && (template != null) && template.getClass().equals(itemEnchantment.getItem().getClass()) ? itemEnchantment : null;
				if (template != null)
				{
					packet.writeD(template.getDisplayId());
					packet.writeH(template.getType2());
				}
				else
				{
					packet.writeD(ingredient.getId());
					packet.writeH(65535);
				}
				packet.writeQ(_list.getIngredientCount(ingredient));
				packet.writeH(ingredient.getEnchantmentLevel() > 0 ? ingredient.getEnchantmentLevel() : displayItemEnchantment != null ? displayItemEnchantment.getEnchantLevel() : 0); // enchant level
				writeItemAugment(packet, displayItemEnchantment);
				writeItemElemental(packet, displayItemEnchantment);
				writeItemEnsoulOptions(packet, displayItemEnchantment);
			}
		}
		return true;
	}
}