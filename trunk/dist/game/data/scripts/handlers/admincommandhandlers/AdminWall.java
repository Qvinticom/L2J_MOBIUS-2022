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
package handlers.admincommandhandlers;

import java.awt.Color;

import com.l2jmobius.gameserver.data.xml.impl.WallData;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.holders.WallHolder;
import com.l2jmobius.gameserver.network.serverpackets.ExServerPrimitive;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author Mobius
 */
public class AdminWall implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_showwalls"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return false;
		}
		
		if (command.equals("admin_showwalls"))
		{
			final ExServerPrimitive packet = new ExServerPrimitive("wall_info", activeChar.getX(), activeChar.getY(), -16000);
			for (WallHolder wall : WallData.getInstance().getRegionWalls(activeChar.getX(), activeChar.getY()))
			{
				if ((Util.calculateDistance(activeChar.getX(), activeChar.getY(), activeChar.getZ(), wall.getPoint2X(), wall.getPoint2Y(), wall.getZMin(), false, false) < 3000) //
					|| (Util.calculateDistance(activeChar.getX(), activeChar.getY(), activeChar.getZ(), wall.getPoint1X(), wall.getPoint1Y(), wall.getZMin(), false, false) < 3000))
				{
					// top-bottom
					packet.addLine(Color.GREEN, wall.getPoint1X(), wall.getPoint1Y(), wall.getZMax(), wall.getPoint2X(), wall.getPoint2Y(), wall.getZMax());
					packet.addLine(Color.GREEN, wall.getPoint1X(), wall.getPoint1Y(), wall.getZMin(), wall.getPoint2X(), wall.getPoint2Y(), wall.getZMin());
					// left-right
					packet.addLine(Color.GREEN, wall.getPoint1X(), wall.getPoint1Y(), wall.getZMin(), wall.getPoint1X(), wall.getPoint1Y(), wall.getZMax());
					packet.addLine(Color.GREEN, wall.getPoint2X(), wall.getPoint2Y(), wall.getZMin(), wall.getPoint2X(), wall.getPoint2Y(), wall.getZMax());
					// diagonals
					packet.addLine(Color.GREEN, wall.getPoint1X(), wall.getPoint1Y(), wall.getZMin(), wall.getPoint2X(), wall.getPoint2Y(), wall.getZMax());
					packet.addLine(Color.GREEN, wall.getPoint1X(), wall.getPoint1Y(), wall.getZMax(), wall.getPoint2X(), wall.getPoint2Y(), wall.getZMin());
				}
			}
			activeChar.sendPacket(packet);
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
