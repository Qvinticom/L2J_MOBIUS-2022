/*
 * Copyright (C) 2004-2014 L2J Server
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
package com.l2jserver.gameserver.model.conditions;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.effects.EffectFlag;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author Sdw
 */
public class ConditionPlayerCanSwitchSubclass extends Condition
{
	private final int _subIndex;
	
	public ConditionPlayerCanSwitchSubclass(int subIndex)
	{
		_subIndex = subIndex;
	}
	
	@Override
	public boolean testImpl(L2Character effector, L2Character effected, Skill skill, L2Item item)
	{
		boolean canSwitchSub = true;
		
		final L2PcInstance player = effector.getActingPlayer();
		if ((player == null) || player.isAlikeDead())
		{
			canSwitchSub = false;
		}
		else if (((_subIndex != 0) && (player.getSubClasses().get(_subIndex) == null)) || (player.getClassIndex() == _subIndex))
		{
			canSwitchSub = false;
		}
		else if (!player.isInventoryUnder90(true))
		{
			player.sendPacket(SystemMessageId.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
			canSwitchSub = false;
		}
		else if (player.getWeightPenalty() >= 2)
		{
			player.sendPacket(SystemMessageId.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
			canSwitchSub = false;
		}
		else if (player.isAllSkillsDisabled())
		{
			canSwitchSub = false;
		}
		else if (player.isAffected(EffectFlag.MUTED))
		{
			canSwitchSub = false;
			player.sendPacket(SystemMessageId.YOU_CANNOT_CHANGE_THE_CLASS_BECAUSE_OF_IDENTITY_CRISIS);
		}
		else if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || (player.getPvpFlag() > 0) || (player.getInstanceId() > 0) || player.isTransformed() || player.isMounted())
		{
			canSwitchSub = false;
		}
		
		return canSwitchSub;
	}
}
