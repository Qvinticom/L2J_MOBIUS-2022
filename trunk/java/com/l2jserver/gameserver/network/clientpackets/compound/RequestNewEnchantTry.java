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
package com.l2jserver.gameserver.network.clientpackets.compound;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jserver.gameserver.network.serverpackets.compound.ExEnchantFail;
import com.l2jserver.gameserver.network.serverpackets.compound.ExEnchantSucess;
import com.l2jserver.util.Rnd;

/**
 * @author Erlandys
 */
public final class RequestNewEnchantTry extends L2GameClientPacket
{
	private static final String _C__D0_F9_REQUESTNEWENCHANTTRY = "[C] D0:F9 RequestNewEnchantTry";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		final L2ItemInstance firstItem = activeChar.getInventory().getItemByObjectId(activeChar.getFirstCompoundOID());
		final L2ItemInstance secondItem = activeChar.getInventory().getItemByObjectId(activeChar.getSecondCompoundOID());
		if ((firstItem == null) || (secondItem == null))
		{
			return;
		}
		int levelOfStone = 0;
		if (firstItem.getId() < 38900)
		{
			levelOfStone = (firstItem.getId() % 5) + 1;
		}
		else
		{
			levelOfStone = (firstItem.getId() - 38926);
		}
		if ((levelOfStone == 0) || (levelOfStone == 5))
		{
			return;
		}
		int percent = 0;
		switch (levelOfStone)
		{
			case 1:
				percent = Config.SECOND_LEVEL_UPGRADE_CHANCE;
				break;
			case 2:
				percent = Config.THIRD_LEVEL_UPGRADE_CHANCE;
				break;
			case 3:
				percent = Config.FOURTH_LEVEL_UPGRADE_CHANCE;
				break;
			case 4:
				percent = Config.FITH_LEVEL_UPGRADE_CHANCE;
				break;
		}
		if (Rnd.get(100) <= percent)
		{
			int newItem = firstItem.getId() + 1;
			activeChar.destroyItem("FirstCompoundItem", firstItem, null, true);
			activeChar.destroyItem("SecondCompoundItem", secondItem, null, true);
			activeChar.addItem("CompoundItem", newItem, 1, null, true);
			activeChar.sendPacket(new ExEnchantSucess(newItem));
		}
		else
		{
			activeChar.sendPacket(new ExEnchantFail(firstItem.getId(), secondItem.getId()));
			activeChar.destroyItem("SecondCompoundItem", secondItem, null, true);
		}
		activeChar.setFirstCompoundOID(-1);
		activeChar.setSecondCompoundOID(-1);
	}
	
	@Override
	public String getType()
	{
		return _C__D0_F9_REQUESTNEWENCHANTTRY;
	}
}
