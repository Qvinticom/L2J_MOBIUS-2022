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
package org.l2jmobius.gameserver;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class GmListTable
{
	private static Logger _log = Logger.getLogger(GmListTable.class.getName());
	private static GmListTable _instance;
	private final List<PlayerInstance> _gmList = new ArrayList<>();
	
	public static GmListTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new GmListTable();
		}
		return _instance;
	}
	
	private GmListTable()
	{
	}
	
	public void addGm(PlayerInstance player)
	{
		_log.fine("added gm: " + player.getName());
		_gmList.add(player);
	}
	
	public void deleteGm(PlayerInstance player)
	{
		_log.fine("deleted gm: " + player.getName());
		_gmList.remove(player);
	}
	
	public void sendListToPlayer(PlayerInstance player)
	{
		if (_gmList.isEmpty())
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("No GM online");
		}
		else
		{
			SystemMessage sm = new SystemMessage(614);
			sm.addString("" + _gmList.size() + " GM's online:");
			player.sendPacket(sm);
			for (int i = 0; i < _gmList.size(); ++i)
			{
				sm = new SystemMessage(614);
				sm.addString(_gmList.get(i).getName());
				player.sendPacket(sm);
			}
		}
	}
	
	public void broadcastToGMs(ServerBasePacket packet)
	{
		for (int i = 0; i < _gmList.size(); ++i)
		{
			_gmList.get(i).sendPacket(packet);
		}
	}
}
