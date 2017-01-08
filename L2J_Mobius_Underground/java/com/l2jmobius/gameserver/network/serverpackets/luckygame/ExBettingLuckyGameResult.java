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
package com.l2jmobius.gameserver.network.serverpackets.luckygame;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.commons.network.PacketWriter;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.data.xml.impl.LuckyGameData;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.OutgoingPackets;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Mobius
 */
public class ExBettingLuckyGameResult implements IClientOutgoingPacket
{
	private static final int FORTUNE_READING_TICKET = 23767;
	private static final int LUXURY_FORTUNE_READING_TICKET = 23768;
	private int _count = 0;
	private int _type = 0;
	private final L2PcInstance _activeChar;
	
	public ExBettingLuckyGameResult(L2PcInstance activeChar, int type, int count)
	{
		_count = count;
		_type = type;
		_activeChar = activeChar;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		// Calculate rewards
		final List<ItemHolder> rewards = new ArrayList<>();
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
		if ((rewards.size() > 0) && (!_activeChar.getInventory().validateCapacity(rewards.size()) || !_activeChar.getInventory().validateWeight(totalWeight)))
		{
			_activeChar.sendPacket(new ExStartLuckyGame(_activeChar, _type));
			_activeChar.sendPacket(SystemMessageId.YOUR_INVENTORY_IS_EITHER_FULL_OR_OVERWEIGHT);
			return false;
		}
		
		if (_activeChar.getInventory().getInventoryItemCount(_type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, -1) < _count)
		{
			_activeChar.sendPacket(new ExStartLuckyGame(_activeChar, _type));
			_activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_TICKETS);
			return false;
		}
		
		// Remove tickets
		_activeChar.getInventory().destroyItemByItemId("FortuneTelling", _type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, _count, _activeChar, "FortuneTelling");
		
		OutgoingPackets.EX_BETTING_LUCKY_GAME_RESULT.writeId(packet);
		packet.writeD(0x01); // 0 disabled, 1 enabled
		packet.writeD(0x01); // ?
		packet.writeD((int) _activeChar.getInventory().getInventoryItemCount(_type == 2 ? LUXURY_FORTUNE_READING_TICKET : FORTUNE_READING_TICKET, -1)); // Count remaining tickets
		
		if (rewards.size() > 0)
		{
			packet.writeD(rewards.size());
			for (ItemHolder reward : rewards)
			{
				packet.writeD(0x02); // normal = 1, rare = 2 (forcing 2)
				packet.writeD(reward.getId());
				packet.writeD((int) reward.getCount());
				final SystemMessage sm;
				if (_type == 2)
				{
					_activeChar.addItem("LuxuryFortuneTelling", reward, _activeChar, false);
					sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_IN_THE_LUXURY_FORTUNE_READING);
				}
				else
				{
					_activeChar.addItem("FortuneTelling", reward, _activeChar, false);
					sm = SystemMessage.getSystemMessage(SystemMessageId.CONGRATULATIONS_C1_HAS_OBTAINED_S2_OF_S3_THROUGH_FORTUNE_READING);
				}
				sm.addPcName(_activeChar);
				sm.addLong(reward.getCount());
				sm.addItemName(new L2ItemInstance(reward.getId()));
				_activeChar.broadcastPacket(sm, 1000);
			}
		}
		else
		{
			packet.writeD(0x00);
		}
		return true;
	}
}
