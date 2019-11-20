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

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.TargetUnselected;

public class RequestTargetCanceld extends ClientBasePacket
{
	private static final String _C__37_REQUESTTARGETCANCELD = "[C] 37 RequestTargetCanceld";
	
	public RequestTargetCanceld(byte[] rawPacket, ClientThread client) throws IOException
	{
		super(rawPacket);
		PlayerInstance activeChar = client.getActiveChar();
		if (activeChar.getTarget() != null)
		{
			TargetUnselected atk = new TargetUnselected(activeChar);
			client.getConnection().sendPacket(atk);
			((Creature) activeChar).setTarget(null);
		}
		// else
		// {
		// _log.warning("we have no target to cancel ??");
		// }
	}
	
	@Override
	public String getType()
	{
		return _C__37_REQUESTTARGETCANCELD;
	}
}
