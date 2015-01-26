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
package com.l2jserver.gameserver.network.clientpackets.itemappearance;

import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.itemappearance.ExPut_Shape_Shifting_Extraction_Item_Result;
import com.l2jserver.gameserver.network.serverpackets.itemappearance.ExPut_Shape_Shifting_Target_Item_Result;

public final class RequestExTryToPut_Shape_Shifting_EnchantSupportItem extends L2GameClientPacket
{
	private static final String _C__D0_C5_REQUESTEXTRYTOPUT_SHAPE_SHIFTING_ENCHANTSUPPORTITEM = "[C] D0:C5 RequestExTryToPut_Shape_Shifting_EnchantSupportItem";
	
	private int _itemOID;
	
	@Override
	protected void readImpl()
	{
		readD();
		_itemOID = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		L2ItemInstance stone = player.getUsingAppearanceStone();
		if ((stone == null) || (stone.getEtcItem().getAppearanceStone() == null))
		{
			player.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0));
			return;
		}
		L2ItemInstance targetItem = player.getInventory().getItemByObjectId(_itemOID);
		if (targetItem == null)
		{
			player.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0));
			return;
		}
		if (stone.getEtcItem().getAppearanceStone().getMaxGrade() < targetItem.getItem().getCrystalType().getId())
		{
			player.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0));
			return;
		}
		L2ItemInstance item = player.getAppearanceItem();
		if (item == null)
		{
			player.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(0));
			return;
		}
		if (!item.getItemType().equals(targetItem.getItemType()))
		{
			player.sendPacket(new ExPut_Shape_Shifting_Extraction_Item_Result(0));
			return;
		}
		if ((item.isWeapon() && (item.getWeaponItem().getItemType() != targetItem.getWeaponItem().getItemType())) || (item.isArmor() && (item.getArmorItem().getItemType() != targetItem.getArmorItem().getItemType())))
		{
			player.sendPacket(new ExPut_Shape_Shifting_Extraction_Item_Result(0));
			return;
		}
		player.sendPacket(new ExPut_Shape_Shifting_Target_Item_Result(1));
		player.setTargetAppearanceItem(targetItem);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_C5_REQUESTEXTRYTOPUT_SHAPE_SHIFTING_ENCHANTSUPPORTITEM;
	}
}
