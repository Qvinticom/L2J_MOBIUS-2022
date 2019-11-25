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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.templates.Item;

public class EquipUpdate extends ServerBasePacket
{
	private final ItemInstance _item;
	private final int _change;
	
	public EquipUpdate(ItemInstance item, int change)
	{
		_item = item;
		_change = change;
	}
	
	@Override
	public void writeImpl()
	{
		int bodypart = 0;
		writeC(0x5E);
		writeD(_change);
		writeD(_item.getObjectId());
		switch (_item.getItem().getBodyPart())
		{
			case Item.SLOT_L_EAR:
			{
				bodypart = 1;
				break;
			}
			case Item.SLOT_R_EAR:
			{
				bodypart = 2;
				break;
			}
			case Item.SLOT_NECK:
			{
				bodypart = 3;
				break;
			}
			case Item.SLOT_R_FINGER:
			{
				bodypart = 4;
				break;
			}
			case Item.SLOT_L_FINGER:
			{
				bodypart = 5;
				break;
			}
			case Item.SLOT_HEAD:
			{
				bodypart = 6;
				break;
			}
			case Item.SLOT_R_HAND:
			{
				bodypart = 7;
				break;
			}
			case Item.SLOT_L_HAND:
			{
				bodypart = 8;
				break;
			}
			case Item.SLOT_GLOVES:
			{
				bodypart = 9;
				break;
			}
			case Item.SLOT_CHEST:
			{
				bodypart = 10;
				break;
			}
			case Item.SLOT_LEGS:
			{
				bodypart = 11;
				break;
			}
			case Item.SLOT_FEET:
			{
				bodypart = 12;
				break;
			}
			case Item.SLOT_BACK:
			{
				bodypart = 13;
				break;
			}
			case Item.SLOT_LR_HAND:
			{
				bodypart = 14;
			}
		}
		writeD(bodypart);
	}
}
