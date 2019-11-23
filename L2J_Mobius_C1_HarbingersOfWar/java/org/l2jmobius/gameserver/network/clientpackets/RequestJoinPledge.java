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
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.AskJoinPledge;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class RequestJoinPledge extends ClientBasePacket
{
	private static final String _C__24_REQUESTJOINPLEDGE = "[C] 24 RequestJoinPledge";
	
	public RequestJoinPledge(byte[] rawPacket, ClientThread client)
	{
		super(rawPacket);
		WorldObject object;
		int target = readD();
		// Connection con = client.getConnection();
		PlayerInstance activeChar = client.getActiveChar();
		if (activeChar.isTransactionInProgress())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.WAITING_FOR_REPLY));
			return;
		}
		if (target == activeChar.getObjectId())
		{
			SystemMessage sm = new SystemMessage(SystemMessage.CANNOT_INVITE_YOURSELF);
			activeChar.sendPacket(sm);
			return;
		}
		if (activeChar.isClanLeader() && ((object = World.getInstance().findObject(target)) instanceof PlayerInstance))
		{
			PlayerInstance member = (PlayerInstance) object;
			if (member.getClanId() != 0)
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S1_WORKING_WITH_ANOTHER_CLAN);
				sm.addString(member.getName());
				activeChar.sendPacket(sm);
				return;
			}
			if (member.isTransactionInProgress())
			{
				SystemMessage sm = new SystemMessage(SystemMessage.S1_IS_BUSY_TRY_LATER);
				sm.addString(member.getName());
				activeChar.sendPacket(sm);
				return;
			}
			member.setTransactionRequester(activeChar);
			activeChar.setTransactionRequester(member);
			AskJoinPledge ap = new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName());
			member.sendPacket(ap);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__24_REQUESTJOINPLEDGE;
	}
}
