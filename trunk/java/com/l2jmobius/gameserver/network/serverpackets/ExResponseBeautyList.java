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
package com.l2jmobius.gameserver.network.serverpackets;

import java.util.Map;

import com.l2jmobius.gameserver.data.xml.impl.BeautyShopData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.beautyshop.BeautyItem;

/**
 * @author Sdw
 */
public class ExResponseBeautyList extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	private final int _type;
	private final Map<Integer, BeautyItem> _beautyItem;
	
	public final static int SHOW_FACESHAPE = 1;
	public final static int SHOW_HAIRSTYLE = 0;
	
	public ExResponseBeautyList(L2PcInstance activeChar, int type)
	{
		_activeChar = activeChar;
		_type = type;
		if (type == SHOW_HAIRSTYLE)
		{
			_beautyItem = BeautyShopData.getInstance().getBeautyData(activeChar.getRace(), activeChar.getAppearance().getSexType()).getHairList();
		}
		else
		{
			_beautyItem = BeautyShopData.getInstance().getBeautyData(activeChar.getRace(), activeChar.getAppearance().getSexType()).getFaceList();
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x135);
		
		writeQ(_activeChar.getAdena());
		writeQ(_activeChar.getBeautyTickets());
		writeD(_type);
		writeD(_beautyItem.size());
		for (BeautyItem item : _beautyItem.values())
		{
			writeD(item.getId());
			writeD(1); // Limit
		}
		writeD(0);
	}
}
