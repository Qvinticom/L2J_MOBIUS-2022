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

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

public class RequestSocialAction extends ClientBasePacket
{
	private static final String _C__1B_REQUESTSOCIALACTION = "[C] 1B RequestSocialAction";
	
	public RequestSocialAction(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int actionId = readD();
		PlayerInstance activeChar = client.getActiveChar();
		if ((activeChar.getPrivateStoreType() == 0) && (activeChar.getTransactionRequester() == null) && (activeChar.getCurrentState() == 0))
		{
			_log.fine("Social Action:" + actionId);
			SocialAction atk = new SocialAction(client.getActiveChar().getObjectId(), actionId);
			activeChar.sendPacket(atk);
			activeChar.broadcastPacket(atk);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__1B_REQUESTSOCIALACTION;
	}
}
