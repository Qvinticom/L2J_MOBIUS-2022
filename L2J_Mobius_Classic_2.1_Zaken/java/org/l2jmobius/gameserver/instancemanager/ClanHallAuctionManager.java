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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.xml.ClanHallData;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.residences.ClanHallAuction;

/**
 * @author Sdw
 */
public class ClanHallAuctionManager
{
	private static final Logger LOGGER = Logger.getLogger(ClanHallAuctionManager.class.getName());
	
	private static final Map<Integer, ClanHallAuction> AUCTIONS = new HashMap<>();
	private static ScheduledFuture<?> _endTask;
	
	protected ClanHallAuctionManager()
	{
		// Schedule of the start, next Wednesday at 19:00.
		final Calendar start = Calendar.getInstance();
		if ((start.get(Calendar.DAY_OF_WEEK) >= Calendar.WEDNESDAY))
		{
			start.add(Calendar.DAY_OF_YEAR, 7);
		}
		start.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		start.set(Calendar.HOUR_OF_DAY, 19);
		start.set(Calendar.MINUTE, 0);
		start.set(Calendar.SECOND, 0);
		final long startDelay = Math.max(0, start.getTimeInMillis() - Chronos.currentTimeMillis());
		ThreadPool.scheduleAtFixedRate(() -> onStart(), startDelay, 604800000); // 604800000 = 1 week
		if (startDelay > 0)
		{
			onStart();
		}
		
		// Schedule of the end, next Wednesday at 11:00.
		final Calendar end = Calendar.getInstance();
		if ((end.get(Calendar.DAY_OF_WEEK) >= Calendar.WEDNESDAY))
		{
			end.add(Calendar.DAY_OF_YEAR, 7);
		}
		end.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
		end.set(Calendar.HOUR_OF_DAY, 11);
		end.set(Calendar.MINUTE, 0);
		end.set(Calendar.SECOND, 0);
		final long endDelay = Math.max(0, end.getTimeInMillis() - Chronos.currentTimeMillis());
		_endTask = ThreadPool.scheduleAtFixedRate(() -> onEnd(), endDelay, 604800000); // 604800000 = 1 week
	}
	
	private void onStart()
	{
		LOGGER.info(getClass().getSimpleName() + ": Clan Hall Auction has started!");
		AUCTIONS.clear();
		ClanHallData.getInstance().getFreeAuctionableHall().forEach(c -> AUCTIONS.put(c.getResidenceId(), new ClanHallAuction(c.getResidenceId())));
	}
	
	private void onEnd()
	{
		AUCTIONS.values().forEach(ClanHallAuction::finalizeAuctions);
		AUCTIONS.clear();
		LOGGER.info(getClass().getSimpleName() + ": Clan Hall Auction has ended!");
	}
	
	public ClanHallAuction getClanHallAuctionById(int clanHallId)
	{
		return AUCTIONS.get(clanHallId);
	}
	
	public ClanHallAuction getClanHallAuctionByClan(Clan clan)
	{
		for (ClanHallAuction auction : AUCTIONS.values())
		{
			if (auction.getBids().containsKey(clan.getId()))
			{
				return auction;
			}
		}
		return null;
	}
	
	public boolean checkForClanBid(int clanHallId, Clan clan)
	{
		for (Entry<Integer, ClanHallAuction> auction : AUCTIONS.entrySet())
		{
			if ((auction.getKey() != clanHallId) && auction.getValue().getBids().containsKey(clan.getId()))
			{
				return true;
			}
		}
		return false;
	}
	
	public long getRemainingTime()
	{
		return _endTask.getDelay(TimeUnit.MILLISECONDS);
	}
	
	public static ClanHallAuctionManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ClanHallAuctionManager INSTANCE = new ClanHallAuctionManager();
	}
}
