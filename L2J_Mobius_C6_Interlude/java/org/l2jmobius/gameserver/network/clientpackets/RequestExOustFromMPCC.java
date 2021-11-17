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
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author -Wooden- D0 0F 00 5A 00 77 00 65 00 72 00 67 00 00 00
 */
public class RequestExOustFromMPCC implements IClientIncomingPacket
{
	private String _name;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_name = packet.readS();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player target = World.getInstance().getPlayer(_name);
		final Player player = client.getPlayer();
		if ((target != null) && target.isInParty() && player.isInParty() && player.getParty().isInCommandChannel() && target.getParty().isInCommandChannel() && player.getParty().getCommandChannel().getChannelLeader().equals(player))
		{
			target.getParty().getCommandChannel().removeParty(target.getParty());
			
			SystemMessage sm = SystemMessage.sendString("Your party was dismissed from the CommandChannel.");
			target.getParty().broadcastToPartyMembers(sm);
			
			sm = SystemMessage.sendString(target.getParty().getPartyMembers().get(0).getName() + "'s party was dismissed from the CommandChannel.");
		}
		else
		{
			player.sendMessage("Incorrect Target");
		}
	}
}
