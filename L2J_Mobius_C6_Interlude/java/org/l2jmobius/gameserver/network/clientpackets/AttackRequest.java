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
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Servitor;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class AttackRequest implements IClientIncomingPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX;
	@SuppressWarnings("unused")
	private int _originY;
	@SuppressWarnings("unused")
	private int _originZ;
	@SuppressWarnings("unused")
	private int _attackId;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_objectId = packet.readD();
		_originX = packet.readD();
		_originY = packet.readD();
		_originZ = packet.readD();
		_attackId = packet.readC(); // 0 for simple click 1 for shift-click
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		if (!client.getFloodProtectors().canPerformPlayerAction())
		{
			return;
		}
		
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// avoid using expensive operations if not needed
		final WorldObject target;
		if (player.getTargetId() == _objectId)
		{
			target = player.getTarget();
		}
		else
		{
			target = World.getInstance().findObject(_objectId);
		}
		
		if (target == null)
		{
			return;
		}
		
		// Like L2OFF
		if (player.isAttackingNow() && player.isMoving())
		{
			// If target is not attackable, send a Server->Client packet ActionFailed
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Players can't attack objects in the other instances except from multiverse
		if ((target.getInstanceId() != player.getInstanceId()) && (player.getInstanceId() != -1))
		{
			return;
		}
		
		// Only GMs can directly attack invisible characters
		if ((target instanceof Player) && ((Player) target).getAppearance().isInvisible() && !player.isGM())
		{
			return;
		}
		
		// No attacks to same team in event
		if (player.isOnEvent() && !player.isOnSoloEvent())
		{
			if (target instanceof Player)
			{
				if (player.getTeam() == ((Player) target).getTeam())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
			else if (target instanceof Servitor)
			{
				if (player.getTeam() == ((Servitor) target).getOwner().getTeam())
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
			}
		}
		
		if (player.getTarget() != target)
		{
			target.onAction(player);
		}
		else if ((target.getObjectId() != player.getObjectId()) && (player.getPrivateStoreType() == 0) /* && activeChar.getActiveRequester() ==null */)
		{
			target.onForcedAttack(player);
		}
		else
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}