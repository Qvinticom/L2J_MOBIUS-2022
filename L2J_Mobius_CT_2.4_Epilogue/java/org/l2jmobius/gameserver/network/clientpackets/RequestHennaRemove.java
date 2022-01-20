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
import org.l2jmobius.gameserver.model.item.Henna;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

/**
 * @author Zoey76
 */
public class RequestHennaRemove implements IClientIncomingPacket
{
	private int _symbolId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_symbolId = packet.readD();
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
		
		if (!client.getFloodProtectors().canPerformTransaction())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		Henna henna;
		boolean found = false;
		for (int i = 1; i <= 3; i++)
		{
			henna = player.getHenna(i);
			if ((henna != null) && (henna.getDyeId() == _symbolId))
			{
				if (player.getAdena() >= henna.getCancelFee())
				{
					player.removeHenna(i);
				}
				else
				{
					player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				found = true;
				break;
			}
		}
		// TODO: Test.
		if (!found)
		{
			PacketLogger.warning(getClass().getSimpleName() + ": " + player + " requested Henna Draw remove without any henna.");
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}
