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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ExTryMixCube;
import com.l2jserver.gameserver.network.serverpackets.ItemList;

/**
 * @author GenCloud
 */
public class RequestAlchemyTryMixCube extends L2GameClientPacket
{
	private int _activeItems;
	private int[] _objId;
	private long[] _itemCounts;
	private long _allPrice = 0;
	private long _stoneCount;
	private final int _stoneID = 39461;
	@SuppressWarnings("unused")
	private final boolean _isActiveMegaStone = false;
	
	@Override
	protected void readImpl()
	{
		_activeItems = readD();
		
		_objId = new int[5];
		_itemCounts = new long[5];
		
		switch (_activeItems)
		{
			case 1:
			{
				_objId[1] = readD();
				_itemCounts[1] = readQ();
				break;
			}
			case 2:
			{
				_objId[1] = readD();
				_itemCounts[1] = readQ();
				_objId[2] = readD();
				_itemCounts[2] = readQ();
				break;
			}
			case 3:
			{
				_objId[1] = readD();
				_itemCounts[1] = readQ();
				_objId[2] = readD();
				_itemCounts[2] = readQ();
				_objId[3] = readD();
				_itemCounts[3] = readQ();
				break;
			}
			case 4:
			{
				_objId[1] = readD();
				_itemCounts[1] = readQ();
				_objId[2] = readD();
				_itemCounts[2] = readQ();
				_objId[3] = readD();
				_itemCounts[3] = readQ();
				_objId[4] = readD();
				_itemCounts[4] = readQ();
				break;
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if ((activeChar == null) || (activeChar.getRace() != Race.ERTHEIA))
		{
			return;
		}
		
		for (int i = 1; i <= _activeItems; i++)
		{
			_allPrice = _allPrice + (_itemCounts[i] * activeChar.getInventory().getItemByObjectId(_objId[i]).getReferencePrice());
			activeChar.getInventory().destroyItem("Alchemy", _objId[i], _itemCounts[i], activeChar, null);
		}
		
		_stoneCount = _allPrice / 10000; // TODO: formula is not the correct ratio
		
		activeChar.getInventory().addItem("Alchemy", _stoneID, _stoneCount, activeChar, null);
		
		activeChar.sendPacket(new ExTryMixCube(_stoneCount, _stoneID, 0));
		activeChar.sendPacket(new ItemList(activeChar, false));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
