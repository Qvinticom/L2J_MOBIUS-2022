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

import java.util.Iterator;
import java.util.Set;

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class Say2 extends ClientBasePacket
{
	private static final String _C__38_SAY2 = "[C] 38 Say2";
	
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
		String text = readS();
		int type = readD();
		String target = null;
		if (type == 2)
		{
			target = readS();
		}
		_log.fine("Say type:" + type);
		PlayerInstance activeChar = client.getActiveChar();
		// Connection con = client.getConnection();
		CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
		if (type == 2)
		{
			PlayerInstance receiver = World.getInstance().getPlayer(target);
			if (receiver != null)
			{
				receiver.sendPacket(cs);
				activeChar.sendPacket(cs);
			}
			else
			{
				SystemMessage sm = new SystemMessage(3);
				sm.addString(target);
				activeChar.sendPacket(sm);
			}
		}
		else if ((type == 1) || (type == 8))
		{
			PlayerInstance[] players = World.getInstance().getAllPlayers();
			for (PlayerInstance player : players)
			{
				player.sendPacket(cs);
			}
		}
		else if (type == 0)
		{
			Set<PlayerInstance> players = activeChar.getKnownPlayers();
			Iterator<PlayerInstance> iter = players.iterator();
			while (iter.hasNext())
			{
				PlayerInstance player = iter.next();
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
	
	@Override
	public String getType()
	{
		return _C__38_SAY2;
	}
}
