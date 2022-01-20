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

import java.nio.BufferUnderflowException;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketReader;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.xml.DoorData;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.EnchantResult;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.util.IllegalPlayerAction;
import org.l2jmobius.gameserver.util.Util;

public class MoveBackwardToLocation implements IClientIncomingPacket
{
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _movementMode;
	
	@Override
	public boolean read(GameClient client, PacketReader packet)
	{
		_targetX = packet.readD();
		_targetY = packet.readD();
		_targetZ = packet.readD();
		_originX = packet.readD();
		_originY = packet.readD();
		_originZ = packet.readD();
		
		try
		{
			_movementMode = packet.readD(); // is 0 if cursor keys are used 1 if mouse is used
		}
		catch (BufferUnderflowException e)
		{
			// Ignore for now
			if (Config.L2WALKER_PROTECTION)
			{
				final Player player = client.getPlayer();
				player.sendPacket(SystemMessageId.A_HACKING_TOOL_HAS_BEEN_DISCOVERED_PLEASE_TRY_PLAYING_AGAIN_AFTER_CLOSING_UNNECESSARY_PROGRAMS);
				Util.handleIllegalPlayerAction(player, player + " trying to use L2Walker!", IllegalPlayerAction.PUNISH_KICK);
			}
		}
		
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final Player player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		// Move flood protection
		if (!client.getFloodProtectors().canPerformMoveAction())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF movements prohibited when char is sitting
		if (player.isSitting())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF movements prohibited when char is teleporting
		if (player.isTeleporting())
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF the enchant window will close
		if (player.getActiveEnchantItem() != null)
		{
			player.sendPacket(new EnchantResult(0));
			player.setActiveEnchantItem(null);
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			player.sendPacket(new StopMove(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Mobius: Check for possible door logout and move over exploit. Also checked at ValidatePosition.
		if (DoorData.getInstance().checkIfDoorsBetween(player.getX(), player.getY(), player.getZ(), _targetX, _targetY, _targetZ, player.getInstanceId()))
		{
			player.stopMove(player.getLastServerPosition());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_movementMode == 1)
		{
			player.setCursorKeyMovement(false);
		}
		else // 0
		{
			if (!Config.ENABLE_KEYBOARD_MOVEMENT)
			{
				return;
			}
			player.setCursorKeyMovement(true);
		}
		
		if (player.getTeleMode() > 0)
		{
			if (player.getTeleMode() == 1)
			{
				player.setTeleMode(0);
			}
			player.sendPacket(ActionFailed.STATIC_PACKET);
			player.teleToLocation(new Location(_targetX, _targetY, _targetZ), false);
			return;
		}
		
		final double dx = _targetX - player.getX();
		final double dy = _targetY - player.getY();
		// Can't move if character is confused, or trying to move a huge distance
		if (player.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// This is to avoid exploit with Hit + Fast movement
		if ((player.isMoving() && player.isAttackingNow()))
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_targetX, _targetY, _targetZ));
	}
}