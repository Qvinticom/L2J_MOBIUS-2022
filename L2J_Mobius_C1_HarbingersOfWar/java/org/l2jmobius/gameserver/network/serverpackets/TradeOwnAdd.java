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

import org.l2jmobius.gameserver.model.item.instance.Item;

public class TradeOwnAdd extends ServerBasePacket
{
	private final Item temp;
	private final int _amount;
	
	public TradeOwnAdd(Item x, int amount)
	{
		temp = x;
		_amount = amount;
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x30);
		writeH(1);
		final int type = temp.getItem().getType1();
		writeH(type);
		writeD(temp.getObjectId());
		writeD(temp.getItemId());
		writeD(_amount);
		writeH(temp.getItem().getType2());
		writeH(0);
		writeD(temp.getItem().getBodyPart());
		writeH(temp.getEnchantLevel());
		writeH(0);
		writeH(0);
	}
}
