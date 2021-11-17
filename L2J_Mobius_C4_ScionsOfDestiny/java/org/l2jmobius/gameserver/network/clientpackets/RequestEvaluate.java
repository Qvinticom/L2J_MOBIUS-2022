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
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestEvaluate implements IClientIncomingPacket
{
	@SuppressWarnings("unused")
	private int _targetId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetId = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		SystemMessage sm;
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if (!(player.getTarget() instanceof Player))
		{
			sm = new SystemMessage(SystemMessageId.THAT_IS_THE_INCORRECT_TARGET);
			player.sendPacket(sm);
			return;
		}
		
		if (player.getLevel() < 10)
		{
			sm = new SystemMessage(SystemMessageId.ONLY_CHARACTERS_OF_LEVEL_10_OR_ABOVE_ARE_AUTHORIZED_TO_MAKE_RECOMMENDATIONS);
			player.sendPacket(sm);
			return;
		}
		
		if (player.getTarget() == player)
		{
			sm = new SystemMessage(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF);
			player.sendPacket(sm);
			return;
		}
		
		if (player.getRecomLeft() <= 0)
		{
			sm = new SystemMessage(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_MAKE_FURTHER_RECOMMENDATIONS_AT_THIS_TIME_YOU_WILL_RECEIVE_MORE_RECOMMENDATION_CREDITS_EACH_DAY_AT_1_P_M);
			player.sendPacket(sm);
			return;
		}
		
		final Player target = (Player) player.getTarget();
		if (target.getRecomHave() >= Config.ALT_RECOMMENDATIONS_NUMBER)
		{
			sm = new SystemMessage(SystemMessageId.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION);
			player.sendPacket(sm);
			return;
		}
		
		if (!player.canRecom(target))
		{
			sm = new SystemMessage(SystemMessageId.THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED);
			player.sendPacket(sm);
			return;
		}
		
		player.giveRecom(target);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_S1_YOU_ARE_AUTHORIZED_TO_MAKE_S2_MORE_RECOMMENDATIONS);
		sm.addString(target.getName());
		sm.addNumber(player.getRecomLeft());
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_S1);
		sm.addString(player.getName());
		target.sendPacket(sm);
		
		player.sendPacket(new UserInfo(player));
		target.broadcastUserInfo();
	}
}
