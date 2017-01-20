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
package com.l2jmobius.gameserver.network.serverpackets;

import static com.l2jmobius.gameserver.data.xml.impl.MultisellData.PAGE_SIZE;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.model.ensoul.EnsoulOption;
import com.l2jmobius.gameserver.model.items.enchant.attribute.AttributeHolder;
import com.l2jmobius.gameserver.model.multisell.Entry;
import com.l2jmobius.gameserver.model.multisell.Ingredient;
import com.l2jmobius.gameserver.model.multisell.ItemInfo;
import com.l2jmobius.gameserver.model.multisell.ListContainer;
import com.l2jmobius.gameserver.network.OutgoingPackets;

public final class MultiSellList implements IClientOutgoingPacket
{
	private int _size, _index;
	private final ListContainer _list;
	private final boolean _finished;
	
	public MultiSellList(ListContainer list, int index)
	{
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
		
		packet.writeD(_list.getListId()); // list id
		packet.writeC(0x00); // GOD Unknown
		packet.writeD(1 + (_index / PAGE_SIZE)); // page started from 1
		packet.writeD(_finished ? 0x01 : 0x00); // finished
		packet.writeD(PAGE_SIZE); // size of pages
		packet.writeD(_size); // list length
		packet.writeC(_list.isNewMultisell() ? 0x01 : 0x00); // new multisell window
		
		Entry ent;
		while (_size-- > 0)
		{
			ent = _list.getEntries().get(_index++);
			packet.writeD(ent.getEntryId());
			packet.writeC(ent.isStackable() ? 1 : 0);
			packet.writeH(0x00);
			packet.writeD(0x00);
			packet.writeD(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeH(0x00);
			packet.writeC(0); // Size of regular soul crystal options.
			// for (EnsoulOption option : item.getSoulCrystalOptions())
			// {
			// packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
			// }
			
			packet.writeC(0); // Size of special soul crystal options.
			// for (EnsoulOption option : item.getSoulCrystalSpecialOptions())
			// {
			// packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
			// }
			
			packet.writeH(ent.getProducts().size());
			packet.writeH(ent.getIngredients().size());
			
			for (Ingredient ing : ent.getProducts())
			{
				packet.writeD(ing.getItemId());
				if (ing.getTemplate() != null)
				{
					packet.writeQ(ing.getTemplate().getBodyPart());
					packet.writeH(ing.getTemplate().getType2());
				}
				else
				{
					packet.writeQ(0);
					packet.writeH(65535);
				}
				packet.writeQ(ing.getItemCount());
				if (ing.getItemInfo() != null)
				{
					final ItemInfo item = ing.getItemInfo();
					packet.writeH(item.getEnchantLevel()); // enchant level
					packet.writeD(_list.isNewMultisell() ? ing.getChance() : item.getAugmentId()); // augment id
					packet.writeD(0x00); // mana
					packet.writeD(0x00); // time ?
					packet.writeH(item.getElementId()); // attack element
					packet.writeH(item.getElementPower()); // element power
					
					for (int i = 0; i < 6; i++)
					{
						final AttributeHolder holder = item.getElementals()[i];
						packet.writeH(holder != null ? holder.getValue() : 0);
					}
					
					packet.writeC(item.getSpecialAbilities().size()); // Size of regular soul crystal options.
					for (EnsoulOption option : item.getSpecialAbilities())
					{
						packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
					}
					
					packet.writeC(item.getAdditionalSpecialAbilities().size()); // Size of special soul crystal options.
					for (EnsoulOption option : item.getAdditionalSpecialAbilities())
					{
						packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
					}
				}
				else
				{
					packet.writeH(ing.getEnchantLevel()); // enchant level
					packet.writeD(ing.getChance()); // augment id
					packet.writeD(0x00); // mana
					packet.writeD(0x00); // time ?
					packet.writeH(0x00); // attack element
					packet.writeH(0x00); // element power
					packet.writeH(0x00); // fire
					packet.writeH(0x00); // water
					packet.writeH(0x00); // wind
					packet.writeH(0x00); // earth
					packet.writeH(0x00); // holy
					packet.writeH(0x00); // dark
					packet.writeC(0); // Size of regular soul crystal options.
					// for (EnsoulOption option : item.getSoulCrystalOptions())
					// {
					// packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
					// }
					
					packet.writeC(0); // Size of special soul crystal options.
					// for (EnsoulOption option : item.getSoulCrystalSpecialOptions())
					// {
					// packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
					// }
				}
			}
			
			for (Ingredient ing : ent.getIngredients())
			{
				packet.writeD(ing.getItemId());
				packet.writeH(ing.getTemplate() != null ? ing.getTemplate().getType2() : 65535);
				packet.writeQ(ing.getItemCount());
				if (ing.getItemInfo() != null)
				{
					final ItemInfo item = ing.getItemInfo();
					packet.writeH(item.getEnchantLevel()); // enchant level
					packet.writeD(_list.isNewMultisell() ? ing.getChance() : item.getAugmentId()); // augment id
					packet.writeD(0x00); // mana
					packet.writeH(item.getElementId()); // attack element
					packet.writeH(item.getElementPower()); // element power
					for (int i = 0; i < 6; i++)
					{
						final AttributeHolder holder = item.getElementals()[i];
						packet.writeH(holder != null ? holder.getValue() : 0);
					}
					packet.writeC(item.getSpecialAbilities().size()); // Size of regular soul crystal options.
					for (EnsoulOption option : item.getSpecialAbilities())
					{
						packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
					}
					
					packet.writeC(item.getAdditionalSpecialAbilities().size()); // Size of special soul crystal options.
					for (EnsoulOption option : item.getAdditionalSpecialAbilities())
					{
						packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
					}
				}
				else
				{
					packet.writeH(ing.getEnchantLevel()); // enchant level
					packet.writeD(ing.getChance()); // augment id
					packet.writeD(0x00); // mana
					packet.writeH(0x00); // attack element
					packet.writeH(0x00); // element power
					packet.writeH(0x00); // fire
					packet.writeH(0x00); // water
					packet.writeH(0x00); // wind
					packet.writeH(0x00); // earth
					packet.writeH(0x00); // holy
					packet.writeH(0x00); // dark
					packet.writeC(0); // Size of regular soul crystal options.
					// for (EnsoulOption option : item.getSoulCrystalOptions())
					// {
					// packet.writeD(option.getId()); // Regular Soul Crystal Ability ID.
					// }
					
					packet.writeC(0); // Size of special soul crystal options.
					// for (EnsoulOption option : item.getSoulCrystalSpecialOptions())
					// {
					// packet.writeD(option.getId()); // Special Soul Crystal Ability ID.
					// }
				}
			}
		}
		return true;
	}
}
