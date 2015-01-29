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
import com.l2jserver.gameserver.model.entity.AppearanceStone;
import com.l2jserver.gameserver.model.entity.AppearanceStone.StoneType;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.ExUserInfoEquipSlot;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.itemappearance.ExShape_Shifting_Result;

public final class RequestShape_Shifting_Item extends L2GameClientPacket
{
	private static final String _C__D0_C7_REQUESTSHAPE_SHIFTING_ITEM = "[C] D0:C7 RequestShape_Shifting_Item";
	
	@Override
	protected void readImpl()
	{
		readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		final L2ItemInstance stone = player.getUsingAppearanceStone();
		final L2ItemInstance item = player.getAppearanceItem();
		final L2ItemInstance targetItem = player.getTargetAppearanceItem();
		final boolean needTargetItem = (stone != null) && (stone.getEtcItem().getAppearanceStone() != null) && (stone.getEtcItem().getAppearanceStone().getType().equals(StoneType.BLESSED) || stone.getEtcItem().getAppearanceStone().getType().equals(StoneType.NORMAL)) ? true : false;
		player.setUsingAppearanceStone(null);
		player.setAppearanceItem(null);
		player.setTargetAppearanceItem(null);
		if ((stone == null) || (item == null) || ((needTargetItem && (targetItem == null))))
		{
			return;
		}
		final AppearanceStone st = stone.getEtcItem().getAppearanceStone();
		if (st == null)
		{
			return;
		}
		final long cost = st.getPrice();
		if (cost > player.getAdena())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MODIFY_AS_YOU_DO_NOT_HAVE_ENOUGH_ADENA);
			return;
		}
		int targetItemId = 0;
		int time = -1;
		switch (st.getType())
		{
			case NORMAL:
				targetItemId = targetItem.getId();
				player.destroyItem("AppearanceStone", targetItem, null, true);
				break;
			case BLESSED:
				targetItemId = targetItem.getId();
				break;
			case FIXED:
				targetItemId = st.getTargetItem();
				time = (int) st.getTimeForAppearance();
				break;
			default:
				break;
		}
		if (cost > 0)
		{
			player.reduceAdena("AppearanceStone", cost, null, true);
		}
		player.destroyItem("AppearanceStone", stone.getObjectId(), 1, null, true);
		item.setAppearanceId(targetItemId);
		item.setAppearanceTime(time);
		InventoryUpdate iu = new InventoryUpdate();
		iu.addModifiedItem(item);
		player.sendPacket(iu);
		player.sendPacket(new ExUserInfoEquipSlot(player));
		player.broadcastUserInfo();
		player.sendPacket(new ExShape_Shifting_Result(1, item.getId(), targetItemId, time));
	}
	
	@Override
	public String getType()
	{
		return _C__D0_C7_REQUESTSHAPE_SHIFTING_ITEM;
	}
}
