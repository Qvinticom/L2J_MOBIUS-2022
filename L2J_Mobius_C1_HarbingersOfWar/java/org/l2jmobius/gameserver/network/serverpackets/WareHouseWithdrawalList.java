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

import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

public class WareHouseWithdrawalList extends ServerBasePacket
{
	private static Logger _log = Logger.getLogger(WareHouseWithdrawalList.class.getName());
	private static final String _S__54_WAREHOUSEWITHDRAWALLIST = "[S] 54 WareHouseWithdrawalList";
	private final PlayerInstance _cha;
	private final int _money;
	
	public WareHouseWithdrawalList(PlayerInstance cha)
	{
		_cha = cha;
		_money = cha.getAdena();
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(84);
		writeD(_money);
		int count = _cha.getWarehouse().getSize();
		writeH(count);
		List<ItemInstance> items = _cha.getWarehouse().getItems();
		for (int i = 0; i < count; ++i)
		{
			ItemInstance temp = items.get(i);
			_log.fine("item:" + temp.getItem().getName() + " type1:" + temp.getItem().getType1() + " type2:" + temp.getItem().getType2());
			writeH(temp.getItem().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getItem().getType2());
			writeH(100);
			writeD(400);
			writeH(temp.getEnchantLevel());
			writeH(300);
			writeH(200);
			writeD(temp.getItemId());
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__54_WAREHOUSEWITHDRAWALLIST;
	}
}
