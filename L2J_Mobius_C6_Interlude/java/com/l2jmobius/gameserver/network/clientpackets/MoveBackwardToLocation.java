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

import java.nio.BufferUnderflowException;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.csv.DoorTable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.position.Location;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.EnchantResult;
import com.l2jmobius.gameserver.network.serverpackets.StopMove;
import com.l2jmobius.gameserver.util.IllegalPlayerAction;
import com.l2jmobius.gameserver.util.Util;

public class MoveBackwardToLocation extends L2GameClientPacket
{
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	private int _originX;
	private int _originY;
	private int _originZ;
	private int _movementMode;
	
	@Override
	protected void readImpl()
	{
		_targetX = readD();
		_targetY = readD();
		_targetZ = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		
		try
		{
			_movementMode = readD(); // is 0 if cursor keys are used 1 if mouse is used
		}
		catch (BufferUnderflowException e)
		{
			// Ignore for now
			if (Config.L2WALKER_PROTEC)
			{
				final L2PcInstance activeChar = getClient().getActiveChar();
				activeChar.sendPacket(SystemMessageId.HACKING_TOOL);
				Util.handleIllegalPlayerAction(activeChar, "Player " + activeChar.getName() + " trying to use L2Walker!", IllegalPlayerAction.PUNISH_KICK);
			}
		}
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		// Move flood protection
		if (!getClient().getFloodProtectors().getMoveAction().tryPerformAction("MoveBackwardToLocation"))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF movements prohibited when char is sitting
		if (activeChar.isSitting())
		{
			getClient().sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF movements prohibited when char is teleporting
		if (activeChar.isTeleporting())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Like L2OFF the enchant window will close
		if (activeChar.getActiveEnchantItem() != null)
		{
			activeChar.sendPacket(new EnchantResult(0));
			activeChar.setActiveEnchantItem(null);
		}
		
		if ((_targetX == _originX) && (_targetY == _originY) && (_targetZ == _originZ))
		{
			activeChar.sendPacket(new StopMove(activeChar));
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Mobius: Check for possible door logout and move over exploit. Also checked at ValidatePosition.
		if (DoorTable.getInstance().checkIfDoorsBetween(activeChar.getX(), activeChar.getY(), activeChar.getZ(), _targetX, _targetY, _targetZ))
		{
			activeChar.stopMove(activeChar.getLastServerPosition());
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		if (_movementMode == 1)
		{
			activeChar.setCursorKeyMovement(false);
		}
		else // 0
		{
			activeChar.setCursorKeyMovement(true);
		}
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
			{
				activeChar.setTeleMode(0);
			}
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			activeChar.teleToLocation(new Location(_targetX, _targetY, _targetZ), false);
			return;
		}
		
		final double dx = _targetX - activeChar.getX();
		final double dy = _targetY - activeChar.getY();
		// Can't move if character is confused, or trying to move a huge distance
		if (activeChar.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// This is to avoid exploit with Hit + Fast movement
		if ((activeChar.isMoving() && activeChar.isAttackingNow()))
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(_targetX, _targetY, _targetZ));
	}
}