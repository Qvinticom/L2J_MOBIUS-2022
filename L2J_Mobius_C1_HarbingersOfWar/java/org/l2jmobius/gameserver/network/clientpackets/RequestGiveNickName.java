/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.clientpackets;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CharInfo;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestGiveNickName extends ClientBasePacket
{
	public RequestGiveNickName(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		final String target = readS();
		final String title = readS();
		// Connection con = client.getConnection();
		final Player activeChar = client.getActiveChar();
		if (activeChar.isClanLeader())
		{
			Player member;
			if (activeChar.getClan().getLevel() < 3)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.CLAN_LEVEL_3_NEEDED_TO_ENDOVE_TITLE));
				activeChar.sendMessage("But you can do it freely for now ;)");
			}
			if ((member = World.getInstance().getPlayer(target)).getClanId() == activeChar.getClanId())
			{
				member.setTitle(title);
				member.sendPacket(new UserInfo(member));
				member.broadcastPacket(new CharInfo(member));
			}
		}
	}
}
