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
package com.l2jmobius.gameserver.datatables;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * This class stores references to all online game masters. (access level > 100)
 * @version $Revision: 1.2.2.1.2.7 $ $Date: 2005/04/05 19:41:24 $
 */
public class GmListTable
{
	protected static final Logger LOGGER = Logger.getLogger(GmListTable.class.getName());
	private static GmListTable _instance;
	
	/** Set(L2PcInstance>) containing all the GM in game */
	private final Map<L2PcInstance, Boolean> _gmList;
	
	public static GmListTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new GmListTable();
		}
		
		return _instance;
	}
	
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
	
	public List<L2PcInstance> getAllGms(boolean includeHidden)
	{
		final List<L2PcInstance> tmpGmList = new ArrayList<>();
		
		for (Entry<L2PcInstance, Boolean> n : _gmList.entrySet())
		{
			if (includeHidden || !n.getValue())
			{
				tmpGmList.add(n.getKey());
			}
		}
		return tmpGmList;
	}
	
	public List<String> getAllGmNames(boolean includeHidden)
	{
		final List<String> tmpGmList = new ArrayList<>();
		
		for (Entry<L2PcInstance, Boolean> n : _gmList.entrySet())
		{
			if (!n.getValue())
			{
				tmpGmList.add(n.getKey().getName());
			}
			else if (includeHidden)
			{
				tmpGmList.add(n.getKey().getName() + " (invis)");
			}
		}
		return tmpGmList;
	}
	
	private GmListTable()
	{
		LOGGER.info("GmListTable: initalized.");
		_gmList = new ConcurrentHashMap<>();
	}
	
	/**
	 * Add a L2PcInstance player to the Set _gmList
	 * @param player
	 * @param hidden
	 */
	public void addGm(L2PcInstance player, boolean hidden)
	{
		_gmList.put(player, hidden);
	}
	
	public void deleteGm(L2PcInstance player)
	{
		_gmList.remove(player);
	}
	
	/**
	 * GM will be displayed on clients GM list.
	 * @param player the player
	 */
	public void showGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, false);
		}
	}
	
	/**
	 * GM will no longer be displayed on clients GM list.
	 * @param player the player
	 */
	public void hideGm(L2PcInstance player)
	{
		if (_gmList.containsKey(player))
		{
			_gmList.put(player, true);
		}
	}
	
	public boolean isGmOnline(boolean includeHidden)
	{
		for (boolean b : _gmList.values())
		{
			if (includeHidden || !b)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public void sendListToPlayer(L2PcInstance player)
	{
		if (isGmOnline(player.isGM()))
		{
			SystemMessage sm = new SystemMessage(SystemMessageId.GM_LIST);
			player.sendPacket(sm);
			
			for (String name : getAllGmNames(player.isGM()))
			{
				final SystemMessage sm1 = new SystemMessage(SystemMessageId.GM_S1);
				sm1.addString(name);
				player.sendPacket(sm1);
			}
		}
		else
		{
			SystemMessage sm2 = new SystemMessage(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
			player.sendPacket(sm2);
		}
	}
	
	public static void broadcastToGMs(L2GameServerPacket packet)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			gm.sendPacket(packet);
		}
	}
	
	public static void broadcastMessageToGMs(String message)
	{
		for (L2PcInstance gm : getInstance().getAllGms(true))
		{
			// prevents a NPE.
			if (gm != null)
			{
				gm.sendPacket(SystemMessage.sendString(message));
			}
		}
	}
}
