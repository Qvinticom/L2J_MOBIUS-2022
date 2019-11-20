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
import org.l2jmobius.gameserver.model.actor.instance.PetInstance;

public class PetItemList extends ServerBasePacket
{
	private static final String _S__cb_PETITEMLIST = "[S] cb  PetItemList";
	private final PetInstance _cha;
	
	public PetItemList(PetInstance cha)
	{
		_cha = cha;
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(203);
		ItemInstance[] items = _cha.getInventory().getItems();
		int count = items.length;
		writeH(count);
		for (int i = 0; i < count; ++i)
		{
			ItemInstance temp = items[i];
			// _log.fine("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(255);
			if (temp.isEquipped())
			{
				writeH(1);
			}
			else
			{
				writeH(0);
			}
			writeD(temp.getItem().getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(0);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__cb_PETITEMLIST;
	}
}
