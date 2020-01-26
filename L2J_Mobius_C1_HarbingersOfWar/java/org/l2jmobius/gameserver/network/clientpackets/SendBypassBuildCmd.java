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

import org.l2jmobius.gameserver.AdminCommands;
import org.l2jmobius.gameserver.managers.GmListManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.CreatureSay;

public class SendBypassBuildCmd extends ClientBasePacket
{
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;
	
	public SendBypassBuildCmd(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final PlayerInstance activeChar = client.getActiveChar();
		final String command = readS();
		if (client.getAccessLevel() >= 100)
		{
			if (command.equals("admin"))
			{
				AdminCommands.getInstance().showMainPage(client);
			}
			else if (command.startsWith("announce"))
			{
				try
				{
					final String text = command.substring(9);
					final CreatureSay cs = new CreatureSay(0, 10, activeChar.getName(), text);
					for (PlayerInstance player : World.getInstance().getAllPlayers())
					{
						player.sendPacket(cs);
					}
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
			}
			else if (command.startsWith("gmchat"))
			{
				try
				{
					final String text = command.substring(7);
					final CreatureSay cs = new CreatureSay(0, 9, activeChar.getName(), text);
					GmListManager.getInstance().broadcastToGMs(cs);
				}
				catch (StringIndexOutOfBoundsException e)
				{
				}
			}
			else if (command.startsWith("invul"))
			{
				if (activeChar.isInvul())
				{
					activeChar.setInvul(false);
					activeChar.sendMessage("Your status is set back to mortal.");
				}
				else
				{
					activeChar.setInvul(true);
					activeChar.sendMessage("You are now Invulnerable.");
				}
			}
		}
	}
}
