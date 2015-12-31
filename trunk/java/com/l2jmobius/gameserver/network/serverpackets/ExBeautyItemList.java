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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jmobius.gameserver.data.xml.impl.BeautyShopData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.beautyshop.BeautyData;
import com.l2jmobius.gameserver.model.beautyshop.BeautyItem;

/**
 * @author Sdw
 */
public class ExBeautyItemList extends L2GameServerPacket
{
	private int _colorCount;
	private final BeautyData _beautyData;
	private final Map<Integer, List<BeautyItem>> _colorData = new HashMap<>();
	private static final int HAIR_TYPE = 0;
	private static final int FACE_TYPE = 1;
	private static final int COLOR_TYPE = 2;
	
	public ExBeautyItemList(L2PcInstance activeChar)
	{
		_beautyData = BeautyShopData.getInstance().getBeautyData(activeChar.getRace(), activeChar.getAppearance().getSexType());
		
		for (BeautyItem hair : _beautyData.getHairList().values())
		{
			final List<BeautyItem> colors = new ArrayList<>();
			for (BeautyItem color : hair.getColors().values())
			{
				colors.add(color);
				_colorCount++;
			}
			_colorData.put(hair.getId(), colors);
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x177);
		writeD(HAIR_TYPE);
		writeD(_beautyData.getHairList().size());
		for (BeautyItem hair : _beautyData.getHairList().values())
		{
			writeD(0); // ?
			writeD(hair.getId());
			writeD(hair.getAdena());
			writeD(hair.getResetAdena());
			writeD(hair.getBeautyShopTicket());
			writeD(99999999); // Limit
		}
		
		writeD(FACE_TYPE);
		writeD(_beautyData.getFaceList().size());
		for (BeautyItem face : _beautyData.getFaceList().values())
		{
			writeD(0); // ?
			writeD(face.getId());
			writeD(face.getAdena());
			writeD(face.getResetAdena());
			writeD(face.getBeautyShopTicket());
			writeD(99999999); // Limit
		}
		
		writeD(COLOR_TYPE);
		writeD(_colorCount);
		for (int hairId : _colorData.keySet())
		{
			for (BeautyItem color : _colorData.get(hairId))
			{
				writeD(hairId);
				writeD(color.getId());
				writeD(color.getAdena());
				writeD(color.getResetAdena());
				writeD(color.getBeautyShopTicket());
				writeD(99999999);
			}
		}
	}
}
