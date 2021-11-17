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

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ManagePledgePower;

public class RequestPledgePower implements IClientIncomingPacket
{
	private int _clanMemberId;
	private int _action;
	private int _privs;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_clanMemberId = packet.readD();
		_action = packet.readD();
		
		if (_action == 3)
		{
			_privs = packet.readD();
		}
		else
		{
			_privs = 0;
		}
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
		
		if (player.getClan() != null)
		{
			Player member = null;
			if (player.getClan().getClanMember(_clanMemberId) != null)
			{
				member = player.getClan().getClanMember(_clanMemberId).getPlayer();
			}
			
			switch (_action)
			{
				case 1:
				{
					player.sendPacket(new ManagePledgePower(player.getClanPrivileges()));
					break;
				}
				
				case 2:
				{
					
					if (member != null)
					{
						player.sendPacket(new ManagePledgePower(member.getClanPrivileges()));
					}
					break;
				}
				case 3:
				{
					if (player.isClanLeader())
					{
						if (member != null)
						{
							member.setClanPrivileges(_privs);
						}
					}
					break;
				}
			}
		}
	}
}
