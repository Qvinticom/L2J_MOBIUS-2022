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
package org.l2jmobius.gameserver.managers;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;

public class GmListManager
{
	private static GmListManager _instance;
	private final List<PlayerInstance> _gmList = new ArrayList<>();
	
	public static GmListManager getInstance()
	{
		if (_instance == null)
		{
			_instance = new GmListManager();
		}
		return _instance;
	}
	
	private GmListManager()
	{
	}
	
	public void addGm(PlayerInstance player)
	{
		_gmList.add(player);
	}
	
	public void deleteGm(PlayerInstance player)
	{
		_gmList.remove(player);
	}
	
	public void sendListToPlayer(PlayerInstance player)
	{
		if (_gmList.isEmpty())
		{
			player.sendMessage("No GM online.");
		}
		else
		{
			player.sendMessage(_gmList.size() + " GM's online:");
			for (int i = 0; i < _gmList.size(); ++i)
			{
				player.sendMessage(_gmList.get(i).getName());
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
