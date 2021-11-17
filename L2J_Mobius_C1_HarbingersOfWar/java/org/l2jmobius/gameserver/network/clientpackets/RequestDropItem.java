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

import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.DropItem;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.network.serverpackets.UserInfo;

public class RequestDropItem extends ClientBasePacket
{
	private static final Logger _log = Logger.getLogger(RequestDropItem.class.getName());
	
	public RequestDropItem(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int objectId = readD();
		final int count = readD();
		final int x = readD();
		final int y = readD();
		final int z = readD();
		
		if (count == 0)
		{
			return;
		}
		
		final Player activeChar = client.getActiveChar();
		if ((activeChar.getPrivateStoreType() == 0) && (activeChar.getTransactionRequester() == null))
		{
			final Item oldItem = activeChar.getInventory().getItem(objectId);
			if (oldItem == null)
			{
				_log.warning("tried to drop an item that is not in the inventory ?!?:" + objectId);
				return;
			}
			
			final int oldCount = oldItem.getCount();
			Item dropedItem = null;
			if (oldCount < count)
			{
				return;
			}
			
			if ((activeChar.getDistance(x, y) > 150.0) || (Math.abs(z - activeChar.getZ()) > 50))
			{
				activeChar.sendPacket(new SystemMessage(SystemMessage.CANNOT_DISCARD_DISTANCE_TOO_FAR));
				return;
			}
			
			// Do not drop items when casting known skills to avoid exploits.
			final Skill skill = activeChar.getSkill();
			if ((skill != null) && activeChar.isSkillDisabled(skill.getId()) && (activeChar.getSkillLevel(skill.getId()) > 0))
			{
				activeChar.sendMessage("You cannot drop an item while casting " + skill.getName() + ".");
				return;
			}
			
			if (oldItem.isEquipped())
			{
				dropedItem = activeChar.getInventory().dropItem(objectId, count);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(oldItem);
				activeChar.sendPacket(iu);
				activeChar.sendPacket(new UserInfo(activeChar));
			}
			else
			{
				dropedItem = activeChar.getInventory().dropItem(objectId, count);
			}
			dropedItem.setX(x);
			dropedItem.setY(y);
			dropedItem.setZ(z);
			dropedItem.setOnTheGround(true);
			
			final DropItem di = new DropItem(dropedItem, activeChar.getObjectId());
			activeChar.sendPacket(di);
			activeChar.addKnownObjectWithoutCreate(dropedItem);
			for (Player player : activeChar.broadcastPacket(di))
			{
				player.addKnownObjectWithoutCreate(dropedItem);
			}
			
			final InventoryUpdate iu = new InventoryUpdate();
			if (oldCount == dropedItem.getCount())
			{
				iu.addRemovedItem(oldItem);
			}
			else
			{
				iu.addModifiedItem(oldItem);
			}
			activeChar.sendPacket(iu);
			
			final SystemMessage sm = new SystemMessage(SystemMessage.YOU_DROPPED_S1);
			sm.addItemName(dropedItem.getItemId());
			activeChar.sendPacket(sm);
			activeChar.sendPacket(new UserInfo(activeChar));
			World.getInstance().addVisibleObject(dropedItem);
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessage.NOTHING_HAPPENED));
		}
	}
}
