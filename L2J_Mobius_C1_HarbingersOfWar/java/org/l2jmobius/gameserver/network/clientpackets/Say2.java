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
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Say2 extends ClientBasePacket
{
	public static final int ALL = 0;
	public static final int SHOUT = 1;
	public static final int TELL = 2;
	public static final int PARTY = 3;
	public static final int CLAN = 4;
	public static final int PRIVATE_CHAT_PLAYER = 6;
	public static final int PRIVATE_CHAT_GM = 7;
	public static final int TRADE = 8;
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;
	
	public Say2(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final String text = readS();
		final int type = readD();
		String target = null;
		if (type == 2)
		{
			target = readS();
		}
		final PlayerInstance activeChar = client.getActiveChar();
		// Connection con = client.getConnection();
		final CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		if (type == 2)
		{
			final PlayerInstance receiver = World.getInstance().getPlayer(target);
			if (receiver != null)
			{
				receiver.sendPacket(cs);
				activeChar.sendPacket(cs);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_NOT_ONLINE);
				sm.addString(target);
				activeChar.sendPacket(sm);
			}
		}
		else if ((type == 1) || (type == 8))
		{
			for (PlayerInstance player : World.getInstance().getAllPlayers())
			{
				player.sendPacket(cs);
			}
		}
		else if (type == 0)
		{
			for (PlayerInstance player : activeChar.getKnownPlayers())
			{
				player.sendPacket(cs);
			}
			activeChar.sendPacket(cs);
		}
		else if ((type == 4) && (activeChar.getClan() != null))
		{
			activeChar.getClan().broadcastToOnlineMembers(cs);
		}
		else if ((type == 3) && activeChar.isInParty())
		{
			activeChar.getParty().broadcastToPartyMembers(cs);
		}
	}
}
