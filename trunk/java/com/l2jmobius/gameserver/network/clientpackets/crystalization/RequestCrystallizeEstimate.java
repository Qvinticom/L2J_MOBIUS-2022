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
package com.l2jmobius.gameserver.network.clientpackets.crystalization;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.xml.impl.ItemCrystalizationData;
import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.model.CrystalizationData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemChanceHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.items.type.CrystalType;
import com.l2jmobius.gameserver.model.skills.CommonSkill;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.crystalization.ExGetCrystalizingEstimation;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class RequestCrystallizeEstimate extends L2GameClientPacket
{
	private int _objectId;
	private long _count;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_count = readQ();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if ((activeChar == null) || activeChar.isInCrystallize())
		{
			return;
		}
		
		// if (!getClient().getFloodProtectors().getTransaction().tryPerformAction("crystallize"))
		// {
		// activeChar.sendMessage("You are crystallizing too fast.");
		// return;
		// }
		
		if (_count <= 0)
		{
			Util.handleIllegalPlayerAction(activeChar, "[RequestCrystallizeItem] count <= 0! ban! oid: " + _objectId + " owner: " + activeChar.getName(), Config.DEFAULT_PUNISH);
			return;
		}
		
		if ((activeChar.getPrivateStoreType() != PrivateStoreType.NONE) || activeChar.isInCrystallize())
		{
			activeChar.sendPacket(SystemMessageId.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		
		final int skillLevel = activeChar.getSkillLevel(CommonSkill.CRYSTALLIZE.getId());
		if (skillLevel <= 0)
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		final L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_objectId);
		if ((item == null) || item.isShadowItem() || item.isTimeLimitedItem())
		{
			sendActionFailed();
			return;
		}
		
		if (item.isHeroItem())
		{
			sendActionFailed();
			return;
		}
		
		if (!item.getItem().isCrystallizable() || (item.getItem().getCrystalCount() <= 0) || (item.getItem().getCrystalType() == CrystalType.NONE))
		{
			sendActionFailed();
			_log.warning(activeChar + ": tried to crystallize " + item.getItem());
			return;
		}
		
		if (_count > item.getCount())
		{
			_count = activeChar.getInventory().getItemByObjectId(_objectId).getCount();
		}
		
		if (!activeChar.getInventory().canManipulateWithItemId(item.getId()))
		{
			activeChar.sendMessage("You cannot use this item.");
			return;
		}
		
		// Check if the char can crystallize items and return if false;
		boolean canCrystallize = true;
		
		switch (item.getItem().getCrystalTypePlus())
		{
			case D:
			{
				if (skillLevel < 1)
				{
					canCrystallize = false;
				}
				break;
			}
			case C:
			{
				if (skillLevel < 2)
				{
					canCrystallize = false;
				}
				break;
			}
			case B:
			{
				if (skillLevel < 3)
				{
					canCrystallize = false;
				}
				break;
			}
			case A:
			{
				if (skillLevel < 4)
				{
					canCrystallize = false;
				}
				break;
			}
			case S:
			{
				if (skillLevel < 5)
				{
					canCrystallize = false;
				}
				break;
			}
			case R:
			{
				if (skillLevel < 6)
				{
					canCrystallize = false;
				}
				break;
			}
		}
		
		if (!canCrystallize)
		{
			activeChar.sendPacket(SystemMessageId.YOU_MAY_NOT_CRYSTALLIZE_THIS_ITEM_YOUR_CRYSTALLIZATION_SKILL_LEVEL_IS_TOO_LOW);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.setInCrystallize(true);
		
		// add crystals
		final int crystalId = item.getItem().getCrystalItemId();
		final int crystalAmount = item.getCrystalCount();
		final List<ItemChanceHolder> items = new ArrayList<>();
		items.add(new ItemChanceHolder(crystalId, 100, crystalAmount));
		
		final CrystalizationData data = ItemCrystalizationData.getInstance().getCrystalization(item.getId());
		if (data != null)
		{
			for (ItemChanceHolder holder : data.getItems())
			{
				if (holder.getId() != crystalId)
				{
					items.add(holder);
				}
			}
		}
		
		activeChar.sendPacket(new ExGetCrystalizingEstimation(items));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
