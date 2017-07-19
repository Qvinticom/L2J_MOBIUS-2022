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
package com.l2jmobius.gameserver.skills.l2skills;

import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.templates.StatsSet;
import com.l2jmobius.util.Rnd;

/**
 * @author Nemesiss
 */
public class L2SkillCreateItem extends L2Skill
{
	private final int create_item_id;
	private final int create_item_count;
	private final int random_count;
	
	public L2SkillCreateItem(StatsSet set)
	{
		super(set);
		
		create_item_id = set.getInteger("create_item_id", 0);
		create_item_count = set.getInteger("create_item_count", 0);
		random_count = set.getInteger("random_count", 1);
		
	}
	
	/**
	 * @see com.l2jmobius.gameserver.model.L2Skill#useSkill(com.l2jmobius.gameserver.model.L2Character, com.l2jmobius.gameserver.model.L2Object[])
	 */
	@Override
	public void useSkill(L2Character activeChar, L2Object[] targets)
	{
		if (activeChar.isAlikeDead())
		{
			return;
		}
		
		if ((create_item_id == 0) || (create_item_count == 0))
		{
			final SystemMessage sm = new SystemMessage(SystemMessage.SKILL_NOT_AVAILABLE);
			activeChar.sendPacket(sm);
			return;
		}
		final L2PcInstance player = (L2PcInstance) activeChar;
		if (activeChar instanceof L2PcInstance)
		{
			final int rnd = Rnd.nextInt(random_count) + 1;
			final int count = create_item_count * rnd;
			
			giveItems(player, create_item_id, count);
		}
	}
	
	/**
	 * @param activeChar
	 * @param itemId
	 * @param count
	 */
	public void giveItems(L2PcInstance activeChar, int itemId, int count)
	{
		final L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setCount(count);
		activeChar.getInventory().addItem("Skill", item, activeChar, activeChar);
		
		if (count > 1)
		{
			final SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_S2_S1_s);
			smsg.addItemName(item.getItemId());
			smsg.addNumber(count);
			activeChar.sendPacket(smsg);
		}
		else
		{
			final SystemMessage smsg = new SystemMessage(SystemMessage.EARNED_ITEM);
			smsg.addItemName(item.getItemId());
			activeChar.sendPacket(smsg);
		}
		
		final ItemList il = new ItemList(activeChar, false);
		activeChar.sendPacket(il);
	}
}