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

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExAlchemyConversion;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.ItemList;
import com.l2jserver.util.Rnd;

/**
 * @author GenCloud, Mobius
 */
public class RequestAlchemyConversion extends L2GameClientPacket
{
	@SuppressWarnings("unused")
	private int _unk;
	
	private int _skillId;
	private int _skillLevel;
	private int _skillUseCount;
	private long _resultItemCount = 0;
	private long _resultFailCount = 0;
	
	@Override
	protected void readImpl()
	{
		_skillUseCount = readD();
		_unk = readH(); // Unk = 10; xs is
		_skillId = readD();
		_skillLevel = readD();
		readB(new byte[28]);
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLevel);
		L2ItemInstance invitem = null;
		
		if ((activeChar == null) || (activeChar.getRace() != Race.ERTHEIA))
		{
			return;
		}
		
		double chance = 100; // 100% ?
		
		boolean hasIngidients = true;
		for (ItemHolder item : skill.getAlchemySkill().getIngridientItems())
		{
			if ((activeChar.getInventory().getInventoryItemCount(item.getId(), -1) * _skillUseCount) < (item.getCount() * _skillUseCount))
			{
				hasIngidients = false;
				break;
			}
		}
		if (!hasIngidients)
		{
			activeChar.sendPacket(SystemMessageId.PLEASE_ENTER_THE_COMBINATION_INGREDIENTS);
			return;
		}
		
		for (int i = 0; i < _skillUseCount; i++)
		{
			boolean ok = Rnd.get(1, 100) < chance;
			skill.getAlchemySkill().getIngridientItems().forEach(holder -> activeChar.getInventory().destroyItemByItemId("Alchemy", holder.getId(), holder.getCount(), activeChar, null));
			
			if (ok)
			{
				_resultItemCount = skill.getAlchemySkill().getTransmutedItem().getCount() * _skillUseCount;
			}
			else
			{
				_resultFailCount++; // ?
			}
		}
		if (_resultItemCount > 0)
		{
			invitem = activeChar.getInventory().addItem("Alchemy", skill.getAlchemySkill().getTransmutedItem().getId(), _resultItemCount, activeChar, null);
		}
		
		if (_resultFailCount > 0) // ?
		{
			invitem = activeChar.getInventory().destroyItemByItemId("Alchemy", skill.getAlchemySkill().getTransmutedItem().getId(), _resultFailCount, activeChar, null);
			activeChar.sendPacket(SystemMessageId.FAILURE_TO_TRANSMUTE_WILL_DESTROY_SOME_INGREDIENTS);
		}
		
		activeChar.sendPacket(new ExAlchemyConversion((int) _resultItemCount, (int) _resultFailCount));
		
		if (Config.FORCE_INVENTORY_UPDATE)
		{
			activeChar.sendPacket(new ItemList(activeChar, false));
		}
		else
		{
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
		}
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
