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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.ItemInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.Connection;
import org.l2jmobius.gameserver.network.serverpackets.DropItem;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestDropItem extends ClientBasePacket
{
	private static final String _C__12_REQUESTDROPITEM = "[C] 12 RequestDropItem";
	
	public RequestDropItem(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		int objectId = readD();
		int count = readD();
		int x = readD();
		int y = readD();
		int z = readD();
		if (count == 0)
		{
			return;
		}
		if ((client.getActiveChar().getPrivateStoreType() == 0) && (client.getActiveChar().getTransactionRequester() == null))
		{
			Connection con = client.getConnection();
			PlayerInstance activeChar = client.getActiveChar();
			ItemInstance oldItem = activeChar.getInventory().getItem(objectId);
			if (oldItem == null)
			{
				_log.warning("tried to drop an item that is not in the inventory ?!?:" + objectId);
				return;
			}
			int oldCount = oldItem.getCount();
			_log.fine("requested drop item " + objectId + "(" + oldCount + ") at " + x + "/" + y + "/" + z);
			ItemInstance dropedItem = null;
			if (oldCount < count)
			{
				_log.finest(activeChar.getObjectId() + ":player tried to drop more items than he has");
				return;
			}
			if ((activeChar.getDistance(x, y) > 150.0) || (Math.abs(z - activeChar.getZ()) > 50))
			{
				_log.finest(activeChar.getObjectId() + ": trying to drop too far away");
				SystemMessage sm = new SystemMessage(151);
				activeChar.sendPacket(sm);
				return;
			}
			if (oldItem.isEquipped())
			{
				dropedItem = activeChar.getInventory().dropItem(objectId, count);
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(oldItem);
				con.sendPacket(iu);
				UserInfo ui = new UserInfo(activeChar);
				con.sendPacket(ui);
			}
			else
			{
				dropedItem = activeChar.getInventory().dropItem(objectId, count);
			}
			dropedItem.setX(x);
			dropedItem.setY(y);
			dropedItem.setZ(z);
			dropedItem.setOnTheGround(true);
			_log.fine("dropping " + objectId + " item(" + count + ") at: " + x + " " + y + " " + z);
			DropItem di = new DropItem(dropedItem, activeChar.getObjectId());
			activeChar.sendPacket(di);
			activeChar.addKnownObjectWithoutCreate(dropedItem);
			for (Creature player : activeChar.broadcastPacket(di))
			{
				((PlayerInstance) player).addKnownObjectWithoutCreate(dropedItem);
			}
			InventoryUpdate iu = new InventoryUpdate();
			if (oldCount == dropedItem.getCount())
			{
				_log.finest("remove item from inv");
				iu.addRemovedItem(oldItem);
			}
			else
			{
				_log.finest("reducing item in inv");
				iu.addModifiedItem(oldItem);
			}
			con.sendPacket(iu);
			SystemMessage sm = new SystemMessage(298);
			sm.addItemName(dropedItem.getItemId());
			con.sendPacket(sm);
			con.sendPacket(new UserInfo(activeChar));
			World.getInstance().addVisibleObject(dropedItem);
		}
		else
		{
			SystemMessage msg = new SystemMessage(61);
			client.getActiveChar().sendPacket(msg);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__12_REQUESTDROPITEM;
	}
}
