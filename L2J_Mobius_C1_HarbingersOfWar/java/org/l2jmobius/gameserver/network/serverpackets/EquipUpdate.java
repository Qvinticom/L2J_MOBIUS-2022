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

public class EquipUpdate extends ServerBasePacket
{
	private static final String _S__5E_EQUIPUPDATE = "[S] 5E EquipUpdate";
	private final ItemInstance _item;
	private final int _change;
	
	public EquipUpdate(ItemInstance item, int change)
	{
		_item = item;
		_change = change;
	}
	
	@Override
	public byte[] getContent()
	{
		int bodypart = 0;
		writeC(94);
		writeD(_change);
		writeD(_item.getObjectId());
		switch (_item.getItem().getBodyPart())
		{
			case 4:
			{
				bodypart = 1;
				break;
			}
			case 2:
			{
				bodypart = 2;
				break;
			}
			case 8:
			{
				bodypart = 3;
				break;
			}
			case 16:
			{
				bodypart = 4;
				break;
			}
			case 32:
			{
				bodypart = 5;
				break;
			}
			case 64:
			{
				bodypart = 6;
				break;
			}
			case 128:
			{
				bodypart = 7;
				break;
			}
			case 256:
			{
				bodypart = 8;
				break;
			}
			case 512:
			{
				bodypart = 9;
				break;
			}
			case 1024:
			{
				bodypart = 10;
				break;
			}
			case 2048:
			{
				bodypart = 11;
				break;
			}
			case 4096:
			{
				bodypart = 12;
				break;
			}
			case 8192:
			{
				bodypart = 13;
				break;
			}
			case 16384:
			{
				bodypart = 14;
			}
		}
		writeD(bodypart);
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__5E_EQUIPUPDATE;
	}
}
