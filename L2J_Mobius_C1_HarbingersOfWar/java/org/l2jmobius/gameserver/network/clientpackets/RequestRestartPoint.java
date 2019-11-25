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

import org.l2jmobius.gameserver.data.MapRegionTable;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.Revive;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;

public class RequestRestartPoint extends ClientBasePacket
{
	public RequestRestartPoint(byte[] decrypt, ClientThread client)
	{
		super(decrypt);
		
		final PlayerInstance activeChar = client.getActiveChar();
		final int[] townCords = MapRegionTable.getInstance().getClosestTownCords(activeChar);
		final StopMove stopMove = new StopMove(activeChar);
		activeChar.sendPacket(stopMove);
		activeChar.sendPacket(new ActionFailed());
		activeChar.broadcastPacket(stopMove);
		activeChar.teleToLocation(townCords[0], townCords[1], townCords[2]);
		activeChar.setCurrentHp(0.6 * activeChar.getMaxHp());
		activeChar.setCurrentMp(0.6 * activeChar.getMaxMp());
		final Revive revive = new Revive(activeChar);
		final SocialAction sa = new SocialAction(activeChar.getObjectId(), 15);
		activeChar.broadcastPacket(sa);
		activeChar.sendPacket(sa);
		activeChar.sendPacket(revive);
		activeChar.broadcastPacket(revive);
		activeChar.setTarget(activeChar);
	}
}
