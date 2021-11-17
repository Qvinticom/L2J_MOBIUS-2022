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

import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.ServerBasePacket;
import org.l2jmobius.gameserver.network.serverpackets.SunRise;
import org.l2jmobius.gameserver.network.serverpackets.SunSet;
import org.l2jmobius.util.Chronos;

/**
 * Game Time task manager class.
 * @author Forsaiken
 */
public class GameTimeTaskManager extends Thread
{
	private long _gameStartTime = Chronos.currentTimeMillis() - 3600000L;
	
	protected GameTimeTaskManager()
	{
		super("GameTimeTaskManager");
	}
	
	public int getGameTime()
	{
		final long time = (Chronos.currentTimeMillis() - _gameStartTime) / 10000L;
		return (int) time;
	}
	
	@Override
	public void run()
	{
		try
		{
			do
			{
				broadcastToPlayers(new SunRise());
				Thread.sleep(21600000L);
				_gameStartTime = Chronos.currentTimeMillis();
				broadcastToPlayers(new SunSet());
				Thread.sleep(3600000L);
			}
			while (true);
		}
		catch (InterruptedException e1)
		{
		}
	}
	
	private void broadcastToPlayers(ServerBasePacket packet)
	{
		for (Player player : World.getInstance().getAllPlayers())
		{
			player.sendPacket(packet);
		}
	}
	
	public static final GameTimeTaskManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final GameTimeTaskManager INSTANCE = new GameTimeTaskManager();
	}
}
