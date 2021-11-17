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
import org.l2jmobius.gameserver.network.serverpackets.ExBrExtraUserInfo;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

/**
 * @author Mobius
 */
public class RequestEvaluate implements IClientIncomingPacket
{
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		packet.readD(); // target Id
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
		
		final Player target = (Player) player.getTarget();
		if (target == null)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.SELECT_TARGET));
			return;
		}
		
		if (!(player.getTarget() instanceof Player))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.INVALID_TARGET));
			return;
		}
		
		if (player.getLevel() < 10)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.ONLY_CHARACTERS_OF_LEVEL_10_OR_ABOVE_ARE_AUTHORIZED_TO_MAKE_RECOMMENDATIONS));
			return;
		}
		
		if (player.getTarget() == player)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_RECOMMEND_YOURSELF));
			return;
		}
		
		if (player.getRecomLeft() <= 0)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOU_ARE_OUT_OF_RECOMMENDATIONS_TRY_AGAIN_LATER));
			return;
		}
		
		if (target.getRecomHave() >= 255)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.YOUR_SELECTED_TARGET_CAN_NO_LONGER_RECEIVE_A_RECOMMENDATION));
			return;
		}
		
		if (!Config.ALT_RECOMMEND && !player.canRecom(target))
		{
			player.sendPacket(new SystemMessage(SystemMessageId.THAT_CHARACTER_HAS_ALREADY_BEEN_RECOMMENDED));
			return;
		}
		
		player.giveRecom(target);
		
		SystemMessage sm = null;
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_RECOMMENDED_C1_YOU_HAVE_S2_RECOMMENDATIONS_LEFT);
		sm.addPcName(target);
		sm.addInt(player.getRecomLeft());
		player.sendPacket(sm);
		
		sm = new SystemMessage(SystemMessageId.YOU_HAVE_BEEN_RECOMMENDED_BY_C1);
		sm.addPcName(player);
		target.sendPacket(sm);
		
		player.sendPacket(new UserInfo(player));
		player.sendPacket(new ExBrExtraUserInfo(player));
		target.broadcastUserInfo();
	}
}
