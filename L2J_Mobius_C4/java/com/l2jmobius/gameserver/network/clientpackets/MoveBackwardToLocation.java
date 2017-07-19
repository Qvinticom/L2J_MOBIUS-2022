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
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.PartyMemberPosition;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.util.Util;

/**
 * This class ...
 * @version $Revision: 1.11.2.4.2.4 $ $Date: 2005/03/27 15:29:30 $
 */
public class MoveBackwardToLocation extends L2GameClientPacket
{
	// private static Logger _log = Logger.getLogger(MoveBackwardToLocation.class.getName());
	
	// cdddddd
	private int _targetX;
	private int _targetY;
	private int _targetZ;
	@SuppressWarnings("unused")
	private int _originX;
	@SuppressWarnings("unused")
	private int _originY;
	@SuppressWarnings("unused")
	private int _originZ;
	private int _moveMovement;
	
	// For geodata
	private int _CurX;
	private int _CurY;
	@SuppressWarnings("unused")
	private int _CurZ;
	
	private static final String _C__01_MOVEBACKWARDTOLOC = "[C] 01 MoveBackwardToLoc";
	
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
			_moveMovement = readD(); // is 0 if cursor keys are used 1 if mouse is used
		}
		catch (final BufferUnderflowException e)
		{
			if (!Config.ALLOW_L2WALKER)
			{
				getClient().getActiveChar().sendPacket(new SystemMessage(769));
				Util.handleIllegalPlayerAction(getClient().getActiveChar(), "Player " + getClient().getActiveChar().getName() + " is trying to use L2Walker and got kicked.", Config.DEFAULT_PUNISH);
			}
		}
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		_targetZ += activeChar.getTemplate().collisionHeight;
		_CurX = activeChar.getX();
		_CurY = activeChar.getY();
		_CurZ = activeChar.getZ();
		
		if (activeChar.getTeleMode() > 0)
		{
			if (activeChar.getTeleMode() == 1)
			{
				activeChar.setTeleMode(0);
			}
			activeChar.sendPacket(new ActionFailed());
			activeChar.teleToLocation(_targetX, _targetY, _targetZ, false);
			return;
		}
		
		// Disable keyboard movement when geodata is not enabled and player is not flying.
		if ((_moveMovement == 0) && (Config.PATHFINDING < 1) && !activeChar.isFlying())
		{
			activeChar.sendPacket(new ActionFailed());
		}
		else
		{
			final double dx = _targetX - _CurX;
			final double dy = _targetY - _CurY;
			// Can't move if character is confused, or trying to move a huge distance
			if (activeChar.isOutOfControl() || (((dx * dx) + (dy * dy)) > 98010000)) // 9900*9900
			{
				activeChar.sendPacket(new ActionFailed());
				return;
			}
			
			activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(_targetX, _targetY, _targetZ, 0));
			
			if (activeChar.getParty() != null)
			{
				activeChar.getParty().broadcastToPartyMembers(activeChar, new PartyMemberPosition(activeChar));
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.network.clientpackets.L2GameClientPacket#getType()
	 */
	@Override
	public String getType()
	{
		return _C__01_MOVEBACKWARDTOLOC;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return true;
	}
}