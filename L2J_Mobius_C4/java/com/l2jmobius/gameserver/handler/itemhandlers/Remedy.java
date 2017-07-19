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

import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.L2ItemInstance;
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.4 $ $Date: 2005/04/06 16:13:51 $
 */
public class Remedy implements IItemHandler
{
	private static int[] _itemIds =
	{
		1831,
		1832,
		1833,
		1834,
		3889,
		6654
	};
	
	@Override
	public void useItem(L2PlayableInstance playable, L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			return;
		}
		
		if (activeChar.getEventTeam() > 0)
		{
			return;
		}
		
		final int itemId = item.getItemId();
		if (itemId == 1831) // antidote
		{
			activeChar.negateEffects(L2Skill.SkillType.POISON, 3, 0);
			final MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1832) // advanced antidote
		{
			activeChar.negateEffects(L2Skill.SkillType.POISON, 7, 0);
			final MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2043, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1833) // bandage
		{
			activeChar.negateEffects(L2Skill.SkillType.BLEED, 3, 0);
			final MagicSkillUse MSU = new MagicSkillUse(playable, playable, 34, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 1834) // emergency dressing
		{
			activeChar.negateEffects(L2Skill.SkillType.BLEED, 7, 0);
			final MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2045, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 3889) // potion of recovery
		{
			activeChar.stopSkillEffects(4082);
			final MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2042, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
		else if (itemId == 6654) // Amulet: Flames of Valakas
		{
			activeChar.stopSkillEffects(4683);
			activeChar.stopSkillEffects(4684);
			final MagicSkillUse MSU = new MagicSkillUse(playable, playable, 2233, 1, 0, 0);
			activeChar.sendPacket(MSU);
			activeChar.broadcastPacket(MSU);
			playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
		}
	}
	
	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}