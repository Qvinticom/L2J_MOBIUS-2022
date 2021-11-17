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

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.model.actor.Player;
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
		_activate = packet.readC() == 1; // enabled?
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player activeChar = client.getPlayer();
		if (activeChar == null)
		{
			return;
		}
		
		final int size = activeChar.getHomunculusList().size();
		if (size == 0)
		{
			return;
		}
		
		final Homunculus homunculus = activeChar.getHomunculusList().get(_slot);
		if (homunculus == null)
		{
			return;
		}
		
		for (int i = 0; i < Config.MAX_HOMUNCULUS_COUNT; i++)
		{
			if (size <= i)
			{
				break;
			}
			
			final Homunculus homu = activeChar.getHomunculusList().get(i);
			if (homu == null)
			{
				continue;
			}
			
			if (homu.isActive())
			{
				homu.setActive(false);
				activeChar.getHomunculusList().update(homu);
				activeChar.getHomunculusList().refreshStats(true);
				activeChar.sendPacket(new ExShowHomunculusList(activeChar));
				activeChar.sendPacket(new ExActivateHomunculusResult(false));
			}
		}
		
		if (_activate)
		{
			if (!homunculus.isActive())
			{
				
				homunculus.setActive(true);
				activeChar.getHomunculusList().update(homunculus);
				activeChar.getHomunculusList().refreshStats(true);
				activeChar.sendPacket(new ExShowHomunculusList(activeChar));
				activeChar.sendPacket(new ExActivateHomunculusResult(true));
			}
		}
		else
		{
			if (homunculus.isActive())
			{
				homunculus.setActive(false);
				activeChar.getHomunculusList().update(homunculus);
				activeChar.getHomunculusList().refreshStats(true);
				activeChar.sendPacket(new ExShowHomunculusList(activeChar));
				activeChar.sendPacket(new ExActivateHomunculusResult(false));
			}
		}
	}
}
