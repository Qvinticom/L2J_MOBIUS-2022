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
package com.l2jmobius.gameserver.network.clientpackets.ability;

import com.l2jmobius.Config;
import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.data.xml.impl.SkillTreesData;
import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.model.L2SkillLearn;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.ceremonyofchaos.CeremonyOfChaosEvent;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.ability.ExAcquireAPSkillList;

/**
 * @author UnAfraid
 */
public class RequestResetAbilityPoint implements IClientIncomingPacket
{
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.isSubClassActive() && !activeChar.isDualClassActive())
		{
			return;
		}
		
		if ((activeChar.getPrivateStoreType() != PrivateStoreType.NONE) || (activeChar.getActiveRequester() != null))
		{
			return;
		}
		else if (activeChar.getLevel() < 85)
		{
			client.sendPacket(SystemMessageId.REACH_LEVEL_85_TO_USE_THE_ABILITY);
			return;
		}
		else if (activeChar.isInOlympiadMode() || activeChar.isOnEvent(CeremonyOfChaosEvent.class))
		{
			client.sendPacket(SystemMessageId.YOU_CANNOT_USE_OR_RESET_ABILITY_POINTS_WHILE_PARTICIPATING_IN_THE_OLYMPIAD_OR_CEREMONY_OF_CHAOS);
			return;
		}
		else if (activeChar.isOnEvent()) // custom event message
		{
			activeChar.sendMessage("You cannot use or reset Ability Points while participating in an event.");
			return;
		}
		else if (activeChar.getAbilityPoints() == 0)
		{
			activeChar.sendMessage("You don't have ability points to reset!");
			return;
		}
		else if (activeChar.getAbilityPointsUsed() == 0)
		{
			activeChar.sendMessage("You haven't used your ability points yet!");
			return;
		}
		else if (activeChar.getSp() < Config.ABILITY_POINTS_RESET_SP)
		{
			client.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_SP_FOR_THIS);
			return;
		}
		activeChar.setSp(activeChar.getSp() - Config.ABILITY_POINTS_RESET_SP);
		
		for (L2SkillLearn sk : SkillTreesData.getInstance().getAbilitySkillTree().values())
		{
			final Skill skill = activeChar.getKnownSkill(sk.getSkillId());
			if (skill != null)
			{
				activeChar.removeSkill(skill);
			}
		}
		activeChar.setAbilityPointsUsed(0);
		activeChar.sendPacket(new ExAcquireAPSkillList(activeChar));
		activeChar.broadcastUserInfo();
	}
}
