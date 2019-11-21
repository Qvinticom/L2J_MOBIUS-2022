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
import org.l2jmobius.gameserver.network.serverpackets.GMViewCharacterInfo;
import org.l2jmobius.gameserver.network.serverpackets.GMViewItemList;
import org.l2jmobius.gameserver.network.serverpackets.GMViewPledgeInfo;

public class RequestGMCommand extends ClientBasePacket
{
	private static final String _C__6E_REQUESTGMCOMMAND = "[C] 6e RequestGMCommand";
	
	public RequestGMCommand(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		String targetName = readS();
		int command = readD();
		@SuppressWarnings("unused")
		int unknown = readD();
		PlayerInstance player = World.getInstance().getPlayer(targetName);
		if (player == null)
		{
			return;
		}
		switch (command)
		{
			case 1:
			{
				client.getActiveChar().sendPacket(new GMViewCharacterInfo(player));
				break;
			}
			case 2:
			{
				if (player.getClan() == null)
				{
					break;
				}
				client.getActiveChar().sendPacket(new GMViewPledgeInfo(player.getClan(), player));
				break;
			}
			case 3:
			{
				break;
			}
			case 4:
			{
				break;
			}
			case 5:
			{
				client.getActiveChar().sendPacket(new GMViewItemList(player));
				break;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return _C__6E_REQUESTGMCOMMAND;
	}
}
