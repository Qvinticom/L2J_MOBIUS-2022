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
package org.l2jmobius.gameserver.network.clientpackets.ability;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.data.xml.AbilityPointsData;
import org.l2jmobius.gameserver.enums.UserInfoType;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;
import org.l2jmobius.gameserver.network.serverpackets.ability.ExAcquireAPSkillList;

/**
 * @author UnAfraid
 */
public class RequestChangeAbilityPoint implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (player.isSubClassActive() && !player.isDualClassActive())
		{
			return;
		}
		
		if ((player.getLevel() < 99) || !player.isNoble())
		{
			player.sendPacket(SystemMessageId.ABILITIES_CAN_BE_USED_BY_NOBLESSE_EXALTED_LV_99_OR_ABOVE);
			return;
		}
		
		if (player.getAbilityPoints() >= Config.ABILITY_MAX_POINTS)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_ACQUIRE_ANY_MORE_ABILITY_POINTS);
			return;
		}
		
		if (player.isInOlympiadMode())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_OR_RESET_ABILITY_POINTS_WHILE_PARTICIPATING_IN_THE_OLYMPIAD_OR_CEREMONY_OF_CHAOS);
			return;
		}
		
		if (player.isOnEvent())
		{
			player.sendMessage("You cannot use or reset Ability Points while participating in an event.");
			return;
		}
		
		final long spRequired = AbilityPointsData.getInstance().getPrice(player.getAbilityPoints());
		if (spRequired > player.getSp())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_NEED_S1_SP_TO_CONVERT_TO1_ABILITY_POINT);
			sm.addLong(spRequired);
			player.sendPacket(sm);
			return;
		}
		
		if (player.getStat().removeSp(spRequired))
		{
			player.setAbilityPoints(player.getAbilityPoints() + 1);
			final UserInfo info = new UserInfo(player, false);
			info.addComponentType(UserInfoType.SLOTS, UserInfoType.CURRENT_HPMPCP_EXP_SP);
			player.sendPacket(info);
			player.sendPacket(new ExAcquireAPSkillList(player));
		}
	}
}
