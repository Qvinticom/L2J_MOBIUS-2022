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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.ArrayList;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;

/**
 * @author Erlandys TODO: Fix this class to Infinity Odyssey !!!
 */
public class ExChangeAttributeItemList extends AbstractItemPacket
{
	private final ArrayList<L2ItemInstance> _itemsList;
	private final int _itemOID;
	
	public ExChangeAttributeItemList(L2PcInstance player, int itemOID)
	{
		_itemsList = new ArrayList<>();
		for (L2ItemInstance item : player.getInventory().getItems())
		{
			if (item.isWeapon())
			{
				if (item.getAttackElementPower() > 0)
				{
					_itemsList.add(item);
				}
			}
		}
		_itemOID = itemOID;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x113);
		writeD(_itemOID);
		writeD(_itemsList.size());
		for (L2ItemInstance item : _itemsList)
		{
			writeItem(item);
		}
	}
}