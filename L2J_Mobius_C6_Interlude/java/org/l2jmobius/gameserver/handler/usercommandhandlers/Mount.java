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
package org.l2jmobius.gameserver.handler.usercommandhandlers;

import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.handler.IUserCommandHandler;
import org.l2jmobius.gameserver.model.Inventory;
import org.l2jmobius.gameserver.model.actor.Summon;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.Ride;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Broadcast;

/**
 * Support for /mount command.
 * @author Tempy
 */
public class Mount implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		61
	};
	
	@Override
	public synchronized boolean useUserCommand(int id, PlayerInstance player)
	{
		if (id != COMMAND_IDS[0])
		{
			return false;
		}
		
		final Summon pet = player.getPet();
		
		if ((pet != null) && pet.isMountable() && !player.isMounted())
		{
			if (player.isDead())
			{
				// A strider cannot be ridden when player is dead.
				final SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				player.sendPacket(msg);
			}
			else if (pet.isDead())
			{
				// A dead strider cannot be ridden.
				final SystemMessage msg = new SystemMessage(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				player.sendPacket(msg);
			}
			else if (pet.isInCombat())
			{
				// A strider in battle cannot be ridden.
				final SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				player.sendPacket(msg);
			}
			else if (player.isInCombat())
			{
				// A pet cannot be ridden while player is in battle.
				final SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				player.sendPacket(msg);
			}
			else if (!player.isInsideRadius(pet, 60, true, false))
			{
				player.sendMessage("Too far away from strider to mount.");
				return false;
			}
			else if (!GeoEngine.getInstance().canSeeTarget(player, pet))
			{
				final SystemMessage msg = new SystemMessage(SystemMessageId.CANT_SEE_TARGET);
				player.sendPacket(msg);
				return false;
			}
			else if (player.isSitting() || player.isMoving())
			{
				// A strider can be ridden only when player is standing.
				final SystemMessage msg = new SystemMessage(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				player.sendPacket(msg);
			}
			else if (!pet.isDead() && !player.isMounted())
			{
				if (!player.disarmWeapons())
				{
					return false;
				}
				
				final Ride mount = new Ride(player.getObjectId(), Ride.ACTION_MOUNT, pet.getTemplate().getNpcId());
				Broadcast.toSelfAndKnownPlayersInRadius(player, mount, 810000/* 900 */);
				player.setMountType(mount.getMountType());
				player.setMountObjectID(pet.getControlItemId());
				pet.unSummon(player);
				
				if ((player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND) != null) || (player.getInventory().getPaperdollItem(Inventory.PAPERDOLL_LRHAND) != null))
				{
					player.dismount();
				}
			}
		}
		else if (player.isRentedPet())
		{
			player.stopRentPet();
		}
		else if (player.isMounted())
		{
			player.dismount();
		}
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
