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
package com.l2jserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * @author UnAfraid
 */
public class ExAcquireSkillInfo extends L2GameServerPacket
{
	private final int _id;
	private final int _level;
	private final int _spCost;
	private final int _minLevel;
	private final List<ItemHolder> _itemReq = new ArrayList<>();
	private final List<Integer> _skillRem;
	
	/**
	 * Special constructor for Alternate Skill Learning system.<br>
	 * Sets a custom amount of SP.
	 * @param player
	 * @param skillLearn the skill learn.
	 * @param sp the custom SP amount.
	 */
	public ExAcquireSkillInfo(L2PcInstance player, L2SkillLearn skillLearn, int sp)
	{
		_id = skillLearn.getSkillId();
		_level = skillLearn.getSkillLevel();
		_spCost = sp;
		_minLevel = skillLearn.getGetLevel();
		_itemReq.addAll(skillLearn.getRequiredItems());
		_skillRem = Collections.emptyList();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xFE);
		writeH(0xFC);
		
		writeD(_id);
		writeD(_level);
		writeQ(_spCost);
		writeH(_minLevel);
		writeH(0x00); // TODO: Find me !
		writeD(_itemReq.size());
		for (ItemHolder holder : _itemReq)
		{
			writeD(holder.getId());
			writeQ(holder.getCount());
		}
		
		writeD(_skillRem.size());
		for (int skillId : _skillRem)
		{
			writeD(skillId);
			writeD(SkillData.getInstance().getMaxLevel(skillId));
		}
	}
}
