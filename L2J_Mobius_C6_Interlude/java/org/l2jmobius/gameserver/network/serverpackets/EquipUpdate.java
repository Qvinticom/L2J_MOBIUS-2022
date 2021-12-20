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
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * 5e 01 00 00 00 01 - added ? 02 - modified 7b 86 73 42 object id 08 00 00 00 body slot body slot 0000 ?? underwear 0001 ear 0002 ear 0003 neck 0004 finger (magic ring) 0005 finger (magic ring) 0006 head (l.cap) 0007 r.hand (dagger) 0008 l.hand (arrows) 0009 hands (short gloves) 000a chest (squire
 * shirt) 000b legs (squire pants) 000c feet 000d ?? back 000e lr.hand (bow) format ddd
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:40 $
 */
public class EquipUpdate implements IClientOutgoingPacket
{
	private final Item _item;
	private final int _change;
	
	public EquipUpdate(Item item, int change)
	{
		_item = item;
		_change = change;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		int bodypart = 0;
		OutgoingPackets.EQUIP_UPDATE.writeId(packet);
		packet.writeD(_change);
		packet.writeD(_item.getObjectId());
		switch (_item.getItem().getBodyPart())
		{
			case ItemTemplate.SLOT_L_EAR:
			{
				bodypart = 0x01;
				break;
			}
			case ItemTemplate.SLOT_R_EAR:
			{
				bodypart = 0x02;
				break;
			}
			case ItemTemplate.SLOT_NECK:
			{
				bodypart = 0x03;
				break;
			}
			case ItemTemplate.SLOT_R_FINGER:
			{
				bodypart = 0x04;
				break;
			}
			case ItemTemplate.SLOT_L_FINGER:
			{
				bodypart = 0x05;
				break;
			}
			case ItemTemplate.SLOT_HEAD:
			{
				bodypart = 0x06;
				break;
			}
			case ItemTemplate.SLOT_R_HAND:
			{
				bodypart = 0x07;
				break;
			}
			case ItemTemplate.SLOT_L_HAND:
			{
				bodypart = 0x08;
				break;
			}
			case ItemTemplate.SLOT_GLOVES:
			{
				bodypart = 0x09;
				break;
			}
			case ItemTemplate.SLOT_CHEST:
			{
				bodypart = 0x0a;
				break;
			}
			case ItemTemplate.SLOT_LEGS:
			{
				bodypart = 0x0b;
				break;
			}
			case ItemTemplate.SLOT_FEET:
			{
				bodypart = 0x0c;
				break;
			}
			case ItemTemplate.SLOT_BACK:
			{
				bodypart = 0x0d;
				break;
			}
			case ItemTemplate.SLOT_LR_HAND:
			{
				bodypart = 0x0e;
				break;
			}
			case ItemTemplate.SLOT_HAIR:
			{
				bodypart = 0x0f;
				break;
			}
		}
		packet.writeD(bodypart);
		return true;
	}
}
