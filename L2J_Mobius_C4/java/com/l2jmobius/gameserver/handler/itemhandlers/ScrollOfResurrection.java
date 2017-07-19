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
package com.l2jmobius.gameserver.handler.itemhandlers;

import com.l2jmobius.gameserver.datatables.SkillTable;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.7 $ $Date: 2005/04/05 19:41:13 $
 */
public class ScrollOfResurrection implements IItemHandler
{
	// all the items ids that this handler knows
	private final static int[] _itemIds =
	{
		737,
		3936,
		3959,
		6387
	};
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.handler.IItemHandler#useItem(com.l2jmobius.gameserver.model.L2PcInstance, com.l2jmobius.gameserver.model.L2ItemInstance)
	 */
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		final L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (activeChar.isAllSkillsDisabled())
		{
			return;
		}
		
		final L2Character target = (L2Character) activeChar.getTarget();
		boolean res = true;
		
		if (target == null)
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_CANT_FOUND));
			res = false;
		}
		
		if (target != null)
		{
			if (!target.isDead())
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				res = false;
			}
			else if (!(target instanceof L2PcInstance) && !(target instanceof L2PetInstance))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.TARGET_IS_INCORRECT));
				res = false;
			}
		}
		
		if (activeChar.isSitting())
		{
			res = false;
		}
		
		if (activeChar.isFestivalParticipant())
		{
			res = false;
			activeChar.sendMessage("Resurrection inside festival is prohibited.");
		}
		
		if (activeChar.isInOlympiadMode())
		{
			res = false;
			activeChar.sendMessage("You cannot use this item during an Olympiad match.");
		}
		
		int skillId = 0;
		
		switch (item.getItemId())
		{
			
			case 737:
				skillId = 2014; // Scroll of Resurrection
				break;
			
			case 3936:
				skillId = 2049; // Blessed Scroll of Resurrection
				break;
			
			case 3959:
				skillId = 2062; // L2Day - Blessed Scroll of Resurrection
				break;
			
			case 6387:
				if (!(target instanceof L2PetInstance))
				{
					res = false;
				}
				skillId = 2179; // Blessed Scroll of Resurrection: For Pets
				break;
			
		}
		
		if (res)
		{
			if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, true))
			{
				return;
			}
			
			final L2Skill skill = SkillTable.getInstance().getInfo(skillId, 1);
			
			if (skill != null)
			{
				activeChar.useMagic(skill, true, true);
			}
		}
		
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}