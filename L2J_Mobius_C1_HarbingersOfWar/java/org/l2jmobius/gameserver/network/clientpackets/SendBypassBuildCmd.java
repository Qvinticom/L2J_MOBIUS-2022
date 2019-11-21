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
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class SendBypassBuildCmd extends ClientBasePacket
{
	private static final String _C__5B_SENDBYPASSBUILDCMD = "[C] 5b SendBypassBuildCmd";
	public static final int GM_MESSAGE = 9;
	public static final int ANNOUNCEMENT = 10;
	
	public SendBypassBuildCmd(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		PlayerInstance activeChar = client.getActiveChar();
		String command = readS();
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
					String text = command.substring(9);
					CreatureSay cs = new CreatureSay(0, 10, activeChar.getName(), text);
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
					String text = command.substring(7);
					CreatureSay cs = new CreatureSay(0, 9, activeChar.getName(), text);
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
					activeChar.setIsInvul(false);
					String text = "Your status is set back to mortal.";
					SystemMessage sm = new SystemMessage(614);
					sm.addString(text);
					activeChar.sendPacket(sm);
				}
				else
				{
					activeChar.setIsInvul(true);
					String text = "You are now Invulnerable";
					SystemMessage sm = new SystemMessage(614);
					sm.addString(text);
					activeChar.sendPacket(sm);
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__5B_SENDBYPASSBUILDCMD;
	}
}
