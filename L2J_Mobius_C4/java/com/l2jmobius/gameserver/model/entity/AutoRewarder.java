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
package com.l2jmobius.gameserver.model.entity;

import java.util.ArrayList;
import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author Jamaica
 */
public class AutoRewarder
{
	private static List<String> _ips;
	
	public static void load()
	{
		_ips = new ArrayList<>();
		
		System.out.println("Initializing Auto Rewarder");
		ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new Runnable()
		{
			@Override
			public void run()
			{
				autoReward();
			}
			
		}, Config.AUTO_REWARD_DELAY * 1000, Config.AUTO_REWARD_DELAY * 1000);
	}
	
	public static void autoReward()
	{
		for (final L2PcInstance p : L2World.getInstance().getAllPlayers())
		{
			if (p == null)
			{
				continue;
			}
			
			if (p.inOfflineMode() || p.isInJail())
			{
				continue;
			}
			
			if (p.getClient() == null)
			{
				continue;
			}
			
			if (p.getClient().isDetached())
			{
				continue;
			}
			
			final String ip = p.getClient().getConnection().getInetAddress().getHostAddress();
			if ((ip != null) && _ips.contains(ip))
			{
				continue;
			}
			
			_ips.add(ip);
			
			p.addItem("autoReward", Config.AUTO_REWARD_ID, Config.AUTO_REWARD_COUNT, p, true);
		}
		_ips.clear();
	}
}