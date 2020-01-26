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
package org.l2jmobius.gameserver.instancemanager;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.concurrent.ThreadPool;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.serverpackets.SetupGauge;
import org.l2jmobius.gameserver.network.serverpackets.SocialAction;

/**
 * @author Michiru
 */
public class AwayManager
{
	protected static final Logger LOGGER = Logger.getLogger(AwayManager.class.getName());
	
	protected Map<PlayerInstance, RestoreData> _awayPlayers;
	
	private AwayManager()
	{
		_awayPlayers = Collections.synchronizedMap(new WeakHashMap<PlayerInstance, RestoreData>());
	}
	
	private final class RestoreData
	{
		private final String _originalTitle;
		private final int _originalTitleColor;
		private final boolean _sitForced;
		
		public RestoreData(PlayerInstance player)
		{
			_originalTitle = player.getTitle();
			_originalTitleColor = player.getAppearance().getTitleColor();
			_sitForced = !player.isSitting();
		}
		
		public boolean isSitForced()
		{
			return _sitForced;
		}
		
		public void restore(PlayerInstance player)
		{
			player.getAppearance().setTitleColor(_originalTitleColor);
			player.setTitle(_originalTitle);
		}
	}
	
	public void setAway(PlayerInstance player, String text)
	{
		player.setAwaying(true);
		player.broadcastPacket(new SocialAction(player.getObjectId(), 9));
		player.sendMessage("Your status is Away in " + Config.AWAY_TIMER + " Sec.");
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		final SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.AWAY_TIMER * 1000);
		player.sendPacket(sg);
		player.setImmobilized(true);
		ThreadPool.schedule(new setPlayerAwayTask(player, text), Config.AWAY_TIMER * 1000);
	}
	
	public void setBack(PlayerInstance player)
	{
		player.sendMessage("You are back from Away Status in " + Config.BACK_TIMER + " Sec.");
		final SetupGauge sg = new SetupGauge(SetupGauge.BLUE, Config.BACK_TIMER * 1000);
		player.sendPacket(sg);
		ThreadPool.schedule(new setPlayerBackTask(player), Config.BACK_TIMER * 1000);
	}
	
	public void extraBack(PlayerInstance player)
	{
		if (player == null)
		{
			return;
		}
		final RestoreData rd = _awayPlayers.get(player);
		if (rd == null)
		{
			return;
		}
		
		rd.restore(player);
		_awayPlayers.remove(player);
	}
	
	class setPlayerAwayTask implements Runnable
	{
		private final PlayerInstance _player;
		private final String _awayText;
		
		setPlayerAwayTask(PlayerInstance player, String awayText)
		{
			_player = player;
			_awayText = awayText;
		}
		
		@Override
		public void run()
		{
			if (_player == null)
			{
				return;
			}
			if (_player.isAttackingNow() || _player.isCastingNow())
			{
				return;
			}
			
			_awayPlayers.put(_player, new RestoreData(_player));
			
			_player.disableAllSkills();
			_player.abortAttack();
			_player.abortCast();
			_player.setTarget(null);
			_player.setImmobilized(false);
			if (!_player.isSitting())
			{
				_player.sitDown();
			}
			if (_awayText.length() <= 1)
			{
				_player.sendMessage("You are now *Away*");
			}
			else
			{
				_player.sendMessage("You are now Away *" + _awayText + "*");
			}
			
			_player.getAppearance().setTitleColor(Config.AWAY_TITLE_COLOR);
			
			if (_awayText.length() <= 1)
			{
				_player.setTitle("*Away*");
			}
			else
			{
				_player.setTitle("Away*" + _awayText + "*");
			}
			
			_player.broadcastUserInfo();
			_player.setParalyzed(true);
			_player.setAway(true);
			_player.setAwaying(false);
		}
	}
	
	class setPlayerBackTask implements Runnable
	{
		private final PlayerInstance _player;
		
		setPlayerBackTask(PlayerInstance player)
		{
			_player = player;
		}
		
		@Override
		public void run()
		{
			if (_player == null)
			{
				return;
			}
			final RestoreData rd = _awayPlayers.get(_player);
			
			if (rd == null)
			{
				return;
			}
			
			_player.setParalyzed(false);
			_player.enableAllSkills();
			_player.setAway(false);
			
			if (rd.isSitForced())
			{
				_player.standUp();
			}
			
			rd.restore(_player);
			_awayPlayers.remove(_player);
			_player.broadcastUserInfo();
			_player.sendMessage("You are Back now!");
		}
	}
	
	public static AwayManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final AwayManager INSTANCE = new AwayManager();
	}
}
