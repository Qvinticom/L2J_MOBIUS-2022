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

import org.l2jmobius.gameserver.ClientThread;
import org.l2jmobius.gameserver.Connection;
import org.l2jmobius.gameserver.data.MapRegionTable;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.Revive;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;
import org.l2jmobius.gameserver.network.serverpackets.StopMove;
import org.l2jmobius.gameserver.network.serverpackets.TeleportToLocation;

public class RequestRestartPoint extends ClientBasePacket
{
	private static final String _C__6d_REQUESTRESTARTPOINT = "[C] 6d RequestRestartPoint";
	
	public RequestRestartPoint(byte[] decrypt, ClientThread client) throws IOException
	{
		super(decrypt);
		Connection con = client.getConnection();
		PlayerInstance activeChar = client.getActiveChar();
		int[] townCords = MapRegionTable.getInstance().getClosestTownCords(activeChar);
		StopMove stopMove = new StopMove(activeChar);
		con.sendPacket(stopMove);
		ActionFailed actionFailed = new ActionFailed();
		con.sendPacket(actionFailed);
		activeChar.broadcastPacket(stopMove);
		TeleportToLocation teleport = new TeleportToLocation(activeChar, townCords[0], townCords[1], townCords[2]);
		activeChar.sendPacket(teleport);
		World.getInstance().removeVisibleObject(activeChar);
		activeChar.removeAllKnownObjects();
		activeChar.setX(townCords[0]);
		activeChar.setY(townCords[1]);
		activeChar.setZ(townCords[2]);
		activeChar.setCurrentHp(0.6 * activeChar.getMaxHp());
		activeChar.setCurrentMp(0.6 * activeChar.getMaxMp());
		Revive revive = new Revive(activeChar);
		try
		{
			Thread.sleep(2000L);
		}
		catch (InterruptedException e)
		{
			// empty catch block
		}
		World.getInstance().addVisibleObject(activeChar);
		SocialAction sa = new SocialAction(activeChar.getObjectId(), 15);
		activeChar.broadcastPacket(sa);
		activeChar.sendPacket(sa);
		activeChar.sendPacket(revive);
		activeChar.broadcastPacket(revive);
		activeChar.setTarget(activeChar);
	}
	
	@Override
	public String getType()
	{
		return _C__6d_REQUESTRESTARTPOINT;
	}
}
