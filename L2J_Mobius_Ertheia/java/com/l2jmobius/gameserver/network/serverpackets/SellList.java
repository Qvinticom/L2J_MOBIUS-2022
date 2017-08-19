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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.gameserver.enums.AttributeType;
import com.l2jmobius.gameserver.enums.TaxType;
import com.l2jmobius.gameserver.model.actor.L2Summon;
import com.l2jmobius.gameserver.model.actor.instance.L2MerchantInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;

public class SellList implements IClientOutgoingPacket
{
	private final L2PcInstance _activeChar;
	private final L2MerchantInstance _merchant;
	private final long _money;
	private final List<L2ItemInstance> _sellList;
	
	public SellList(L2PcInstance player)
	{
		this(player, null);
	}
	
	public SellList(L2PcInstance player, L2MerchantInstance lease)
	{
		_activeChar = player;
		_merchant = lease;
		_money = _activeChar.getAdena();
		
		if (_merchant == null)
		{
			_sellList = new LinkedList<>();
			final L2Summon pet = _activeChar.getPet();
			for (L2ItemInstance item : _activeChar.getInventory().getItems())
			{
				if (!item.isEquipped() && item.isSellable() && ((pet == null) || (item.getObjectId() != pet.getControlObjectId()))) // Pet is summoned and not the item that summoned the pet
				{
					_sellList.add(item);
				}
			}
		}
		else
		{
			_sellList = Collections.emptyList();
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SELL_LIST.writeId(packet);
		
		packet.writeQ(_money);
		packet.writeD(_merchant == null ? 0x00 : 1000000 + _merchant.getTemplate().getId());
		packet.writeH(_sellList.size());
		
		for (L2ItemInstance item : _sellList)
		{
			int price = item.getItem().getReferencePrice() / 2;
			if (_merchant != null)
			{
				price -= (price * _merchant.getTotalTaxRate(TaxType.SELL));
			}
			
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getDisplayId());
			packet.writeQ(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(item.isEquipped() ? 0x01 : 0x00);
			packet.writeD(item.getItem().getBodyPart());
			packet.writeH(item.getEnchantLevel());
			packet.writeH(0x00); // TODO: Verify me
			packet.writeH(item.getCustomType2());
			packet.writeQ(price);
			// T1
			packet.writeH(item.getAttackAttributeType().getClientId());
			packet.writeH(item.getAttackAttributePower());
			for (AttributeType type : AttributeType.ATTRIBUTE_TYPES)
			{
				packet.writeH(item.getDefenceAttribute(type));
			}
			// Enchant Effects
			for (int op : item.getEnchantOptions())
			{
				packet.writeH(op);
			}
		}
		return true;
	}
}
