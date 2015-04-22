/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets.commission;

import com.l2jserver.gameserver.model.ItemInfo;
import com.l2jserver.gameserver.model.commission.CommissionItem;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author NosBit
 */
public class ExResponseCommissionBuyItem extends L2GameServerPacket
{
	public static final ExResponseCommissionBuyItem FAILED = new ExResponseCommissionBuyItem(null);
	
	private final CommissionItem _commissionItem;
	
	public ExResponseCommissionBuyItem(CommissionItem commissionItem)
	{
		_commissionItem = commissionItem;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xF9);
		writeD(_commissionItem != null ? 1 : 0);
		if (_commissionItem != null)
		{
			final ItemInfo itemInfo = _commissionItem.getItemInfo();
			writeD(itemInfo.getEnchant());
			writeD(itemInfo.getItem().getId());
			writeQ(itemInfo.getCount());
		}
	}
}
