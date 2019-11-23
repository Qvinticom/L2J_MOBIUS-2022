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

import java.io.IOException;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectInfo;
import org.l2jmobius.gameserver.network.serverpackets.RestartResponse;

public class RequestRestart extends ClientBasePacket
{
	private static final String _C__46_REQUESTRESTART = "[C] 46 RequestRestart";
	
	public RequestRestart(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		
		final PlayerInstance player = client.getActiveChar();
		if (player != null)
		{
			if ((player.getPvpFlag() > 0) || player.isInCombat())
			{
				player.sendMessage("You cannot exit the game while in combat.");
				player.sendPacket(new ActionFailed());
				return;
			}
			player.deleteMe();
			final RestartResponse response = new RestartResponse();
			client.getConnection().sendPacket(response);
			client.saveCharToDisk(client.getActiveChar());
			client.setActiveChar(null);
			final CharSelectInfo cl = new CharSelectInfo(client.getLoginName(), client.getSessionId());
			client.getConnection().sendPacket(cl);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__46_REQUESTRESTART;
	}
}
