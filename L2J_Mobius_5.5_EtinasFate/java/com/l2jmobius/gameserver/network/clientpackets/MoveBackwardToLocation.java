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
package com.l2jmobius.gameserver.network.clientpackets;

import java.util.Arrays;

import com.l2jmobius.Config;
import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.data.xml.impl.DoorData;
import com.l2jmobius.gameserver.enums.AdminTeleportType;
import com.l2jmobius.gameserver.enums.SayuneType;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.SayuneEntry;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.events.EventDispatcher;
import com.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerMoveRequest;
import com.l2jmobius.gameserver.model.events.returns.TerminateReturn;
import com.l2jmobius.gameserver.network.GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.FlyToLocation;
import com.l2jmobius.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillLaunched;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.StopMove;
import com.l2jmobius.gameserver.network.serverpackets.sayune.ExFlyMove;
import com.l2jmobius.gameserver.network.serverpackets.sayune.ExFlyMoveBroadcast;
import com.l2jmobius.gameserver.util.Broadcast;

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
		_movementMode = packet.readD(); // is 0 if cursor keys are used 1 if mouse is used
		return true;
	}
	
	@Override
	public void run(GameClient client)
	{
		final PlayerInstance player = client.getPlayer();
		if (player == null)
		{
			return;
		}
		
		if ((Config.PLAYER_MOVEMENT_BLOCK_TIME > 0) && !player.isGM() && (player.getNotMoveUntil() > System.currentTimeMillis()))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC_ONE_MOMENT_PLEASE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			player.sendPacket(new StopMove(player));
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Mobius: Check for possible door logout and move over exploit. Also checked at ValidatePosition.
		if (DoorData.getInstance().checkIfDoorsBetween(player.getX(), player.getY(), player.getZ(), _targetX, _targetY, _targetZ, player.getInstanceWorld(), false))
		{
			player.stopMove(player.getLastServerPosition());
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Correcting targetZ from floor level to head level (?)
		// Client is giving floor level as targetZ but that floor level doesn't
		// match our current geodata and teleport coords as good as head level!
		// L2J uses floor, not head level as char coordinates. This is some
		// sort of incompatibility fix.
		// Validate position packets sends head level.
		_targetZ += player.getTemplate().getCollisionHeight();
		
		if (!player.isCursorKeyMovementActive() && (player.isInFrontOf(new Location(_targetX, _targetY, _targetZ)) || player.isOnSideOf(new Location(_originX, _originY, _originZ))))
		{
			player.setCursorKeyMovementActive(true);
		}
		
		if (_movementMode == 1)
		{
			player.setCursorKeyMovement(false);
			final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerMoveRequest(player, new Location(_targetX, _targetY, _targetZ)), player, TerminateReturn.class);
			if ((terminate != null) && terminate.terminate())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
		}
		else // 0
		{
			if (!Config.ENABLE_KEYBOARD_MOVEMENT)
			{
				return;
			}
			player.setCursorKeyMovement(true);
			if (!player.isCursorKeyMovementActive())
			{
				return;
			}
		}
		
		switch (player.getTeleMode())
		{
			case DEMONIC:
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				player.teleToLocation(new Location(_targetX, _targetY, _targetZ));
				player.setTeleMode(AdminTeleportType.NORMAL);
				break;
			}
			case SAYUNE:
			{
				player.sendPacket(new ExFlyMove(player, SayuneType.ONE_WAY_LOC, -1, Arrays.asList(new SayuneEntry(false, -1, _targetX, _targetY, _targetZ))));
				player.setXYZ(_targetX, _targetY, _targetZ);
				Broadcast.toKnownPlayers(player, new ExFlyMoveBroadcast(player, SayuneType.ONE_WAY_LOC, -1, new Location(_targetX, _targetY, _targetZ)));
				player.setTeleMode(AdminTeleportType.NORMAL);
				break;
			}
			case CHARGE:
			{
				player.setXYZ(_targetX, _targetY, _targetZ);
				Broadcast.toSelfAndKnownPlayers(player, new MagicSkillUse(player, 30012, 10, 500, 0));
				Broadcast.toSelfAndKnownPlayers(player, new FlyToLocation(player, _targetX, _targetY, _targetZ, FlyType.CHARGE));
				Broadcast.toSelfAndKnownPlayers(player, new MagicSkillLaunched(player, 30012, 10));
				player.sendPacket(ActionFailed.STATIC_PACKET);
				break;
			}
			default:
			{
				final double dx = _targetX - player.getX();
				final double dy = _targetY - player.getY();
				// Can't move if character is confused, or trying to move a huge distance
				if (player.isControlBlocked() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
				{
					player.sendPacket(ActionFailed.STATIC_PACKET);
					return;
				}
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_targetX, _targetY, _targetZ));
				break;
			}
		}
		
		// Mobius: Check spawn protections.
		if (player.isSpawnProtected() || player.isTeleportProtected())
		{
			player.onActionRequest();
		}
	}
}
