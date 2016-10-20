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
package com.l2jmobius.gameserver.network.clientpackets.alchemy;

import com.l2jmobius.gameserver.datatables.SkillData;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.L2AlchemySkill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket;
import com.l2jmobius.gameserver.network.serverpackets.ItemList;
import com.l2jmobius.gameserver.network.serverpackets.alchemy.ExAlchemyConversion;
import com.l2jmobius.util.Rnd;

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
		_unk = readH(); // Unk = 10;
		_skillId = readD();
		_skillLevel = readD();
		readB(new byte[28]);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		final Skill skill = SkillData.getInstance().getSkill(_skillId, _skillLevel);
		final L2AlchemySkill alchemySkill = skill.getAlchemySkill();
		
		if ((activeChar == null) || (activeChar.getRace() != Race.ERTHEIA) || (_skillUseCount < 0))
		{
			return;
		}
		
		boolean hasIngidients = true;
		for (ItemHolder item : alchemySkill.getIngridientItems())
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
			if (Rnd.get(1, 100) < 90) // 90% ?
			{
				_resultItemCount += alchemySkill.getTransmutedItem().getCount();
			}
			else
			{
				_resultFailCount++;
			}
			alchemySkill.getIngridientItems().forEach(holder -> activeChar.getInventory().destroyItemByItemId("Alchemy", holder.getId(), holder.getCount(), activeChar, null));
		}
		
		if (_resultItemCount > 0)
		{
			activeChar.addItem("Alchemy", alchemySkill.getTransmutedItem(), activeChar, true);
		}
		if (_resultFailCount > 0)
		{
			for (ItemHolder item : alchemySkill.getIngridientItems())
			{
				activeChar.getInventory().destroyItemByItemId("Alchemy", item.getId(), _resultFailCount, activeChar, null);
				break; // FIXME: Takes only 1st ingredient (client has specific item and quantity).
			}
			activeChar.sendPacket(SystemMessageId.FAILURE_TO_TRANSMUTE_WILL_DESTROY_SOME_INGREDIENTS);
		}
		
		activeChar.sendPacket(new ExAlchemyConversion((int) _resultItemCount, (int) _resultFailCount));
		activeChar.sendPacket(new ItemList(activeChar, false));
	}
	
	@Override
	public String getType()
	{
		return getClass().getSimpleName();
	}
}
