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
import com.l2jmobius.gameserver.model.L2Skill;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.skills.effects.EffectCharge;
import com.l2jmobius.gameserver.skills.l2skills.L2SkillCharge;

public class EnergyStone implements IItemHandler
{
	private static final int ITEM_IDS[] =
	{
		5589
	};
	private EffectCharge _effect;
	private L2SkillCharge _skill;
	
	public EnergyStone()
	{
	}
	
	@Override
	public void useItem(L2Playable playable, L2ItemInstance item)
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
		if (item.getItemId() != 5589)
		{
			return;
		}
		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			return;
		}
		_skill = getChargeSkill(activeChar);
		if (_skill == null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(5589);
			activeChar.sendPacket(sm);
			return;
		}
		
		final SystemMessage sm1 = new SystemMessage(SystemMessageId.USE_S1_);
		sm1.addItemName(5589);
		activeChar.sendPacket(sm1);
		
		_effect = activeChar.getChargeEffect();
		if (_effect == null)
		{
			final L2Skill dummy = SkillTable.getInstance().getInfo(_skill.getId(), _skill.getLevel());
			if (dummy != null)
			{
				dummy.getEffects(activeChar, activeChar);
				final MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, _skill.getId(), 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
			}
			return;
		}
		
		if (_effect.numCharges < 2)
		{
			_effect.addNumCharges(1);
			final SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
			sm.addNumber(_effect.getLevel());
			activeChar.sendPacket(sm);
		}
		else if (_effect.numCharges == 2)
		{
			activeChar.sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		}
		
		final MagicSkillUse MSU = new MagicSkillUse(playable, activeChar, _skill.getId(), 1, 1, 0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
	
	private L2SkillCharge getChargeSkill(L2PcInstance activeChar)
	{
		final L2Skill skills[] = activeChar.getAllSkills();
		final L2Skill arr$[] = skills;
		for (L2Skill s : arr$)
		{
			if ((s.getId() == 50) || (s.getId() == 8))
			{
				return (L2SkillCharge) s;
			}
		}
		
		return null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}