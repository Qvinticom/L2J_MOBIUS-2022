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
package com.l2jserver.gameserver.network.serverpackets.itemappearance;

import com.l2jserver.gameserver.model.entity.AppearanceStone;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;

/**
 * @author Erlandys
 */
public class ExChoose_Shape_Shifting_Item extends L2GameServerPacket
{
	private final int _itemId;
	private final int _type;
	private final int _itemType;
	
	public ExChoose_Shape_Shifting_Item(AppearanceStone stone)
	{
		_itemId = stone.getItemId();
		_type = stone.getType().ordinal();
		_itemType = stone.getItemType().ordinal();
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x129);
		writeD(_type);
		writeD(_itemType);
		writeD(_itemId);
	}
}
