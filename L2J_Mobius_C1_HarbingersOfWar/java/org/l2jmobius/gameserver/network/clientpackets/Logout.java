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
import org.l2jmobius.gameserver.network.serverpackets.LeaveWorld;

public class Logout extends ClientBasePacket
{
	private static final String _C__09_LOGOUT = "[C] 09 Logout";
	
	public Logout(byte[] decrypt, ClientThread client) throws IOException
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
			if (player.isInWater() && (player.getZ() > -16000))
			{
				player.sendMessage("You cannot exit the game under water.");
				player.sendPacket(new ActionFailed());
				return;
			}
			
			final LeaveWorld ql = new LeaveWorld();
			client.getConnection().sendPacket(ql);
			player.deleteMe();
			client.saveCharToDisk(player);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__09_LOGOUT;
	}
}
