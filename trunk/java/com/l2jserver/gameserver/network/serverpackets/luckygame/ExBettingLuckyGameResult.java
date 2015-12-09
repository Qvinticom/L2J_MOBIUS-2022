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
package com.l2jserver.gameserver.network.serverpackets.luckygame;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.data.xml.impl.LuckyGameData;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.util.Rnd;

/**
 * @author Mobius
 */
public class ExBettingLuckyGameResult extends L2GameServerPacket
{
	private static final int FORTUNE_READING_TICKET = 23767;
	private static final int LUXURY_FORTUNE_READING_TICKET = 23768;
	private int _count = 0;
	private int _type = 0;
	
	public ExBettingLuckyGameResult(int type, int count)
	{
		_count = count;
		_type = type;
	}
	
	@Override
	protected void writeImpl()
	{
		final L2PcInstance _activeChar = getClient().getActiveChar();
		
		// Calculate rewards
		List<ItemHolder> rewards = new ArrayList<>();
		int totalWeight = 0;
		for (int rewardCounter = 0; rewardCounter < _count; rewardCounter++)
		{
			if (Rnd.get(3) == 0) // 1 out of 3 chance
			{
				ItemHolder reward = null;
				if (_type == 2)
				{
					if (_count >= 40)
					{
						reward = LuckyGameData.getRandomRareReward();
					}
					else
					{
						reward = LuckyGameData.getRandomLuxuryReward();
					}
				}
				else
				{
					reward = LuckyGameData.getRandomNormalReward();
				}
				rewards.add(reward);
				totalWeight += new L2ItemInstance(reward.getId()).getItem().getWeight() * reward.getCount();
			}
		}
		
		// Check inventory capacity
		if ((rewards.size() > 0) && ((!_activeChar.getInventory().validateCapacity(rewards.size())) || (!_activeChar.getInventory().validateWeight(totalWeight))))
		{
			_activeChar.sendPacket(new ExStartLuckyGame(_type));
			_activeChar.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_EITHER_FULL_OR_OVERWEIGHT);
			return;
		}
		
		if (_activeChar.getInventory().getInventoryItemCount(_type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, -1) < _count)
		{
			_activeChar.sendPacket(new ExStartLuckyGame(_type));
			_activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_TICKETS);
			return;
		}
		
		// Remove tickets
		_activeChar.getInventory().destroyItemByItemId("FortuneTelling", _type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, _count, _activeChar, "FortuneTelling");
		
		writeC(0xFE);
		writeH(0x161);
		writeD(0x01); // 0 disabled, 1 enabled
		writeD(0x01); // ?
		writeD((int) _activeChar.getInventory().getInventoryItemCount(_type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, -1)); // Count remaining tickets
		
		if (rewards.size() > 0)
		{
			writeD(rewards.size());
			for (ItemHolder reward : rewards)
			{
				writeD(0x02); // normal = 1, rare = 2 (forcing 2)
				writeD(reward.getId());
				writeD((int) reward.getCount());
				
				if (_type == 2)
				{
					_activeChar.addItem("LuxuryFortuneTelling", reward, _activeChar, false);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_IN_THE_LUXURY_FORTUNE_READING);
					sm.addPcName(_activeChar);
					sm.addLong(reward.getCount());
					sm.addItemName(new L2ItemInstance(reward.getId()));
					_activeChar.broadcastPacket(sm, 1000);
				}
				else
				{
					_activeChar.addItem("FortuneTelling", reward, _activeChar, false);
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_THROUGH_FORTUNE_READING);
					sm.addPcName(_activeChar);
					sm.addLong(reward.getCount());
					sm.addItemName(new L2ItemInstance(reward.getId()));
					_activeChar.broadcastPacket(sm, 1000);
				}
			}
		}
		else
		{
			writeD(0x00);
		}
	}
}
