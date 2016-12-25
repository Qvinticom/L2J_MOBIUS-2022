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
package com.l2jmobius.gameserver.model.actor.tasks.player;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.enums.Movie;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.ExStartScenePlayer;

/**
 * @author Mobius
 */
public class MovieTask implements Runnable
{
	private final L2PcInstance _player;
	private final Movie _movie;
	
	public MovieTask(L2PcInstance player, Movie movie)
	{
		_player = player;
		_movie = movie;
	}
	
	@Override
	public void run()
	{
		if (_player == null)
		{
			return;
		}
		if (_player.isTeleporting())
		{
			ThreadPoolManager.getInstance().scheduleGeneral(new MovieTask(_player, _movie), 300);
		}
		else
		{
			_player.sendPacket(new ExStartScenePlayer(_movie));
		}
	}
}
