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

import org.l2jmobius.gameserver.enums.CreatureState;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.AttackCanceld;

public class MoveBackwardToLocation extends ClientBasePacket
{
	public MoveBackwardToLocation(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		final int targetX = readD();
		final int targetY = readD();
		final int targetZ = readD();
		final int originX = readD();
		final int originY = readD();
		final int originZ = readD();
		
		final Player activeChar = client.getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getCurrentState() == CreatureState.CASTING)
		{
			activeChar.sendPacket(new ActionFailed());
		}
		else
		{
			if (activeChar.getCurrentState() == CreatureState.ATTACKING)
			{
				final AttackCanceld ac = new AttackCanceld(activeChar.getObjectId());
				activeChar.sendPacket(ac);
				activeChar.broadcastPacket(ac);
			}
			
			activeChar.setInCombat(false);
			activeChar.setCurrentState(CreatureState.IDLE);
			activeChar.setX(originX);
			activeChar.setY(originY);
			activeChar.setZ(originZ);
			activeChar.moveTo(targetX, targetY, targetZ, 0);
			
			// Water check.
			activeChar.checkWaterState();
		}
	}
}
