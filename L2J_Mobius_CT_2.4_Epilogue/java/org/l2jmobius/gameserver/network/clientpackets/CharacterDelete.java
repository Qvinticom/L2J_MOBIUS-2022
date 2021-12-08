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
import org.l2jmobius.gameserver.model.CharSelectInfoPackage;
import org.l2jmobius.gameserver.model.events.Containers;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerDelete;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.PacketLogger;
import org.l2jmobius.gameserver.network.serverpackets.CharDeleteFail;
import org.l2jmobius.gameserver.network.serverpackets.CharDeleteSuccess;
import org.l2jmobius.gameserver.network.serverpackets.CharSelectionInfo;

/**
 * @version $Revision: 1.8.2.1.2.3 $ $Date: 2005/03/27 15:29:30 $
 */
public class CharacterDelete implements IClientIncomingPacket
{
	// cd
	private int _charSlot;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_charSlot = packet.readD();
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		// if (!client.getFloodProtectors().canSelectCharacter())
		// {
		// sendPacket(new CharDeleteFail(CharDeleteFail.REASON_DELETION_FAILED));
		// return;
		// }
		
		try
		{
			switch (client.markToDeleteChar(_charSlot))
			{
				default:
				case -1: // Error
				{
					break;
				}
				case 0: // Success!
				{
					client.sendPacket(new CharDeleteSuccess());
					final CharSelectInfoPackage charInfo = client.getCharSelection(_charSlot);
					EventDispatcher.getInstance().notifyEvent(new OnPlayerDelete(charInfo.getObjectId(), charInfo.getName(), client), Containers.Players());
					break;
				}
				case 1:
				{
					client.sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
					break;
				}
				case 2:
				{
					client.sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
					break;
				}
			}
		}
		catch (Exception e)
		{
			PacketLogger.warning(getClass().getSimpleName() + ": " + e.getMessage());
		}
		
		final CharSelectionInfo cl = new CharSelectionInfo(client.getAccountName(), client.getSessionId().playOkID1, 0);
		client.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
