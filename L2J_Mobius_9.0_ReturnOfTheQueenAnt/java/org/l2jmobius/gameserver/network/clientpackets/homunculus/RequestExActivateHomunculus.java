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
package org.l2jmobius.gameserver.network.clientpackets.homunculus;

import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.homunculus.Homunculus;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExActivateHomunculusResult;
import org.l2jmobius.gameserver.network.serverpackets.homunculus.ExShowHomunculusList;

/**
 * @author Mobius
 */
public class RequestExActivateHomunculus implements IClientIncomingPacket
{
	private int _slot;
	private boolean _activate;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_slot = packet.readD();
		_activate = packet.readC() == 1 ? true : false; // enabled?
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		PlayerInstance activeChar = client.getPlayer();
		if (activeChar == null)
		{
			return;
		}
		
		final Homunculus homunculus = activeChar.getHomunculusList().get(_slot);
		boolean anotherActive = false;
		int size = activeChar.getHomunculusList().size();
		if (size > 1)
		{
			if (_slot == 0)
			{
				if (activeChar.getHomunculusList().get(1).isActive())
				{
					anotherActive = true;
				}
			}
			else
			{
				if (activeChar.getHomunculusList().get(0).isActive())
				{
					anotherActive = true;
				}
			}
		}
		
		if (anotherActive)
		{
			return;
		}
		if (!homunculus.isActive() && _activate)
		{
			homunculus.setActive(true);
			activeChar.getHomunculusList().update(homunculus);
			activeChar.getHomunculusList().refreshStats(true);
			activeChar.sendPacket(new ExShowHomunculusList(activeChar));
			activeChar.sendPacket(new ExActivateHomunculusResult(true));
		}
		else if (homunculus.isActive() && !_activate)
		{
			homunculus.setActive(false);
			activeChar.getHomunculusList().update(homunculus);
			activeChar.getHomunculusList().refreshStats(true);
			activeChar.sendPacket(new ExShowHomunculusList(activeChar));
			activeChar.sendPacket(new ExActivateHomunculusResult(false));
		}
	}
}
