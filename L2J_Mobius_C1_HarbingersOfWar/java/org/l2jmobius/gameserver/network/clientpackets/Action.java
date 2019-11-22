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

import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class Action extends ClientBasePacket
{
	private static final String ACTION__C__04 = "[C] 04 Action";
	private static Logger _log = Logger.getLogger(Action.class.getName());
	
	public Action(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		int objectId = readD();
		@SuppressWarnings("unused")
		int originX = readD();
		@SuppressWarnings("unused")
		int originY = readD();
		@SuppressWarnings("unused")
		int originZ = readD();
		int actionId = readC();
		_log.fine("Action:" + actionId);
		_log.fine("oid:" + objectId);
		PlayerInstance activeChar = client.getActiveChar();
		WorldObject obj = World.getInstance().findObject(objectId);
		if ((obj != null) && !activeChar.isDead() && (activeChar.getPrivateStoreType() == 0) && (activeChar.getTransactionRequester() == null))
		{
			switch (actionId)
			{
				case 0:
				{
					obj.onAction(activeChar);
					break;
				}
				case 1:
				{
					obj.onActionShift(client);
				}
			}
		}
		else
		{
			// _log.warning("object not found, oid " + objectId + " or player is dead");
			activeChar.sendPacket(new ActionFailed());
		}
	}
	
	@Override
	public String getType()
	{
		return ACTION__C__04;
	}
}
