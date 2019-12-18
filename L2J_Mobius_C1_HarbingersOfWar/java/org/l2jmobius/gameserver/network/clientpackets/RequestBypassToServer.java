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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.AdminCommands;
import org.l2jmobius.gameserver.managers.CommunityBoardManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.NpcInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;

public class RequestBypassToServer extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestBypassToServer.class.getName());
	
	public RequestBypassToServer(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final String command = readS();
		try
		{
			if (command.startsWith("admin_") && (client.getAccessLevel() >= 100))
			{
				AdminCommands.getInstance().handleCommands(client, command);
			}
			else if (command.equals("come_here"))
			{
				final WorldObject obj = client.getActiveChar().getTarget();
				if (obj instanceof NpcInstance)
				{
					final NpcInstance temp = (NpcInstance) obj;
					final PlayerInstance player = client.getActiveChar();
					temp.setTarget(player);
					temp.moveTo(player.getX(), player.getY(), player.getZ(), 0);
				}
			}
			else if (command.startsWith("npc_"))
			{
				final int endOfId = command.indexOf(95, 5);
				final String id = command.substring(4, endOfId);
				final WorldObject object = World.getInstance().findObject(Integer.parseInt(id));
				if (object instanceof NpcInstance)
				{
					((NpcInstance) object).onBypassFeedback(client.getActiveChar(), command.substring(endOfId + 1));
				}
			}
			else if (command.startsWith("bbs_"))
			{
				CommunityBoardManager.getInstance().handleCommands(client, command);
			}
		}
		catch (Exception e)
		{
			_log.warning("Bad RequestBypassToServer: " + e.toString());
		}
	}
}
