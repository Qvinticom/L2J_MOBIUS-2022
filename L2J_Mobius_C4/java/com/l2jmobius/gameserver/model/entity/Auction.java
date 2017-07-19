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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.idfactory.IdFactory;
import com.l2jmobius.gameserver.instancemanager.AuctionManager;
import com.l2jmobius.gameserver.instancemanager.ClanHallManager;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

import javolution.util.FastMap;

public class Auction
{
	protected static Logger _log = Logger.getLogger(Auction.class.getName());
	
	/*
	 * TODO: Announce to losing bidder that they have been out bidded Take adena when bidding Return adena when out bid Give item when auction end UpdateBidInDb Schedule Auction end Remove auction from auction and auction_bid table when auction end
	 */
	
	// =========================================================
	public static enum ItemTypeEnum
	{
		ClanHall
	}
	
	public static String[] ItemTypeName =
	{
		"ClanHall"
	};
	
	public static String getItemTypeName(ItemTypeEnum value)
	{
		return ItemTypeName[value.ordinal()];
	}
	
	// =========================================================
	// Schedule Task
	private class AutoEndTask implements Runnable
	{
		public AutoEndTask()
		{
			// do nothing???
		}
		
		@Override
		public void run()
		{
			try
			{
				final long timeRemaining = getEndDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
				if (timeRemaining > 0)
				{
					ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), timeRemaining);
				}
				else
				{
					endAuction();
				}
			}
			catch (final Throwable t)
			{
			}
		}
	}
	
	private void StartAutoTask(boolean forced)
	{
		correctAuctionTime(forced);
		ThreadPoolManager.getInstance().scheduleGeneral(new AutoEndTask(), 1000);
	}
	
	private void correctAuctionTime(boolean forced)
	{
		boolean corrected = false;
		
		if ((_EndDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) || forced)
		{
			// Since auction has past reschedule it to the next one (7 days)
			// This is usually caused by server being down
			corrected = true;
			if (forced)
			{
				setNextAuctionDate();
			}
			else
			{
				endAuction(); // end auction normally in case it had bidders and server was down when it ended
			}
		}
		
		_EndDate.set(Calendar.MINUTE, 0);
		
		if (corrected)
		{
			saveAuctionDate();
		}
	}
	
	private void setNextAuctionDate()
	{
		while (_EndDate.getTimeInMillis() < Calendar.getInstance().getTimeInMillis())
		{
			// Set next auction date if auction has passed
			_EndDate.add(Calendar.DAY_OF_MONTH, 7); // Schedule to happen in 7 days
		}
	}
	
	private void saveAuctionDate()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Update auction set endDate = ? where id = ?"))
		{
			statement.setLong(1, _EndDate.getTimeInMillis());
			statement.setInt(2, _Id);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: saveAuctionDate(): " + e.getMessage(), e);
		}
	}
	
	public class Bidder
	{
		private final String _Name;
		private final String _ClanName;
		private int _Bid;
		private final Calendar _timeBid;
		
		public Bidder(String name, String clanName, int bid, long timeBid)
		{
			_Name = name;
			_ClanName = clanName;
			_Bid = bid;
			_timeBid = Calendar.getInstance();
			_timeBid.setTimeInMillis(timeBid);
		}
		
		public String getName()
		{
			return _Name;
		}
		
		public String getClanName()
		{
			return _ClanName;
		}
		
		public int getBid()
		{
			return _Bid;
		}
		
		public Calendar getTimeBid()
		{
			return _timeBid;
		}
		
		public void setTimeBid(long timeBid)
		{
			_timeBid.setTimeInMillis(timeBid);
		}
		
		public void setBid(int bid)
		{
			_Bid = bid;
		}
	}
	
	// =========================================================
	// Data Field
	private int _Id = 0;
	
	private final int _AdenaId = 57;
	
	private Calendar _EndDate;
	
	private int _HighestBidderId = 0;
	private String _HighestBidderName = "";
	private int _HighestBidderMaxBid = 0;
	
	private int _ItemId = 0;
	private String _ItemName = "";
	private int _ItemObjectId = 0;
	private final int _ItemQuantity = 0;
	private String _ItemType = "";
	
	private int _SellerId = 0;
	private String _SellerClanName = "";
	private String _SellerName = "";
	
	private int _CurrentBid = 0;
	private int _StartingBid = 0;
	
	private final Map<Integer, Bidder> _bidders = new FastMap<>();
	
	// =========================================================
	// Constructor
	public Auction(int auctionId)
	{
		_Id = auctionId;
		load();
		
		// end auction automatically
		StartAutoTask(false);
	}
	
	public Auction(int itemId, L2Clan Clan, long delay, int bid, String name)
	{
		_Id = itemId;
		_EndDate = Calendar.getInstance();
		_EndDate.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + delay);
		_EndDate.set(Calendar.MINUTE, 0);
		_ItemId = itemId;
		_ItemName = name;
		_ItemType = "ClanHall";
		_SellerId = Clan.getLeaderId();
		_SellerName = Clan.getLeaderName();
		_SellerClanName = Clan.getName();
		_StartingBid = bid;
	}
	
	// =========================================================
	// Method - Public
	public synchronized void setBid(L2PcInstance bidder, int bid)
	{
		// Update bid if new bid is higher
		int requiredAdena = bid;
		if (getHighestBidderName().equals(bidder.getClan().getLeaderName()))
		{
			requiredAdena = bid - getHighestBidderMaxBid();
		}
		if (((getHighestBidderId() > 0) && (bid > getHighestBidderMaxBid())) || ((getHighestBidderId() == 0) && (bid >= getStartingBid())))
		{
			if (takeItem(bidder, 57, requiredAdena))
			{
				updateInDB(bidder, bid);
				bidder.getClan().setAuctionBiddedAt(_Id, true);
				return;
			}
		}
		
		// Your bid price must be higher than the minimum price that can be bid.
		bidder.sendPacket(new SystemMessage(677));
	}
	
	// =========================================================
	// Method - Private
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select * from auction where id = ?"))
		{
			statement.setInt(1, getId());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					_CurrentBid = rs.getInt("currentBid");
					_EndDate = Calendar.getInstance();
					_EndDate.setTimeInMillis(rs.getLong("endDate"));
					
					_ItemId = rs.getInt("itemId");
					_ItemName = rs.getString("itemName");
					_ItemObjectId = rs.getInt("itemObjectId");
					_ItemType = rs.getString("itemType");
					_SellerId = rs.getInt("sellerId");
					_SellerClanName = rs.getString("sellerClanName");
					_SellerName = rs.getString("sellerName");
					_StartingBid = rs.getInt("startingBid");
				}
			}
			loadBid();
		}
		catch (final Exception e)
		{
			System.out.println("Exception: Auction.load(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void loadBid()
	{
		_HighestBidderId = 0;
		_HighestBidderName = "";
		_HighestBidderMaxBid = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC"))
		{
			statement.setInt(1, getId());
			try (ResultSet rs = statement.executeQuery())
			{
				while (rs.next())
				{
					if (rs.isFirst())
					{
						_HighestBidderId = rs.getInt("bidderId");
						_HighestBidderName = rs.getString("bidderName");
						_HighestBidderMaxBid = rs.getInt("maxBid");
					}
					_bidders.put(rs.getInt("bidderId"), new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid")));
				}
			}
		}
		catch (final Exception e)
		{
			System.out.println("Exception: Auction.loadBid(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private void returnItem(String Clan, int itemId, int quantity, boolean penalty)
	{
		if (penalty)
		{
			quantity *= 0.9; // take 10% tax fee if needed
		}
		ClanTable.getInstance().getClanByName(Clan).getWarehouse().addItem("Outbidded", _AdenaId, quantity, null, null);
	}
	
	private boolean takeItem(L2PcInstance bidder, int itemId, int quantity)
	{
		
		// Take item from clan warehouse
		if ((bidder.getClan() != null) && (bidder.getClan().getWarehouse().getAdena() >= quantity))
		{
			bidder.getClan().getWarehouse().destroyItemByItemId("Buy", _AdenaId, quantity, bidder, bidder);
			return true;
		}
		
		bidder.sendMessage("You do not have enough adena.");
		return false;
	}
	
	private void updateInDB(L2PcInstance bidder, int bid)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			if (getBidders().get(bidder.getClanId()) != null)
			{
				try (PreparedStatement statement = con.prepareStatement("UPDATE auction_bid SET bidderId=?, bidderName=?, maxBid=?, time_bid=? WHERE auctionId=? AND bidderId=?"))
				{
					statement.setInt(1, bidder.getClanId());
					statement.setString(2, bidder.getClan().getLeaderName());
					statement.setInt(3, bid);
					statement.setLong(4, Calendar.getInstance().getTimeInMillis());
					statement.setInt(5, getId());
					statement.setInt(6, bidder.getClanId());
					statement.execute();
				}
			}
			else
			{
				try (PreparedStatement statement = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?, ?, ?, ?, ?, ?, ?)"))
				{
					statement.setInt(1, IdFactory.getInstance().getNextId());
					statement.setInt(2, getId());
					statement.setInt(3, bidder.getClanId());
					statement.setString(4, bidder.getName());
					statement.setInt(5, bid);
					statement.setString(6, bidder.getClan().getName());
					statement.setLong(7, Calendar.getInstance().getTimeInMillis());
					statement.execute();
				}
				if (L2World.getInstance().getPlayer(_HighestBidderName) != null)
				{
					L2World.getInstance().getPlayer(_HighestBidderName).sendMessage("You have been outbidded.");
				}
			}
			
			// Update internal var
			_HighestBidderId = bidder.getClanId();
			_HighestBidderMaxBid = bid;
			_HighestBidderName = bidder.getClan().getLeaderName();
			if (_bidders.get(_HighestBidderId) == null)
			{
				_bidders.put(_HighestBidderId, new Bidder(_HighestBidderName, bidder.getClan().getName(), bid, Calendar.getInstance().getTimeInMillis()));
			}
			else
			{
				_bidders.get(_HighestBidderId).setBid(bid);
				_bidders.get(_HighestBidderId).setTimeBid(Calendar.getInstance().getTimeInMillis());
			}
			// You have bid in a clan hall auction
			bidder.sendPacket(new SystemMessage(1006));
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.updateInDB(L2PcInstance bidder, int bid): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void removeBids()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?"))
		{
			statement.setInt(1, getId());
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
		}
		
		for (final Bidder b : _bidders.values())
		{
			if (ClanTable.getInstance().getClanByName(b.getClanName()).getHasHideout() == 0)
			{
				returnItem(b.getClanName(), 57, b.getBid(), true); // 10 % tax
			}
			else
			{
				if (L2World.getInstance().getPlayer(b.getName()) != null)
				{
					L2World.getInstance().getPlayer(b.getName()).sendMessage("Congratulations!You have conquered the Clan Hall!");
				}
			}
			ClanTable.getInstance().getClanByName(b.getClanName()).setAuctionBiddedAt(0, true);
		}
		_bidders.clear();
	}
	
	public void deleteAuctionFromDB()
	{
		AuctionManager.getInstance().getAuctions().remove(this);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction WHERE itemId=?"))
		{
			statement.setInt(1, _ItemId);
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.deleteFromDB(): " + e.getMessage(), e);
		}
	}
	
	void endAuction()
	{
		if ((_HighestBidderId == 0) && (_SellerId == 0))
		{
			StartAutoTask(true);
			return;
		}
		if ((_HighestBidderId == 0) && (_SellerId > 0))
		{
			deleteAuctionFromDB();
			return;
		}
		if (_SellerId > 0)
		{
			returnItem(_SellerClanName, 57, _HighestBidderMaxBid, true);
			returnItem(_SellerClanName, 57, ClanHallManager.getInstance().getClanHallById(_ItemId).getLease(), false);
		}
		ClanHallManager.getInstance().getClanHallById(_ItemId).setOwner(ClanTable.getInstance().getClanByName(_bidders.get(_HighestBidderId).getClanName()));
		cancelAuction();
	}
	
	public synchronized void cancelBid(int bidder)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?"))
		{
			statement.setInt(1, getId());
			statement.setInt(2, bidder);
			statement.execute();
		}
		
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.cancelBid(String bidder): " + e.getMessage(), e);
		}
		
		returnItem(_bidders.get(bidder).getClanName(), 57, _bidders.get(bidder).getBid(), true);
		ClanTable.getInstance().getClanByName(_bidders.get(bidder).getClanName()).setAuctionBiddedAt(0, true);
		_bidders.remove(bidder);
	}
	
	public void cancelAuction()
	{
		removeBids();
		deleteAuctionFromDB();
	}
	
	public void confirmAuction()
	{
		AuctionManager.getInstance().getAuctions().add(this);
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO auction (id, sellerId, sellerName, sellerClanName, itemType, itemId, itemObjectId, itemName, itemQuantity, startingBid, currentBid, endDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"))
		{
			statement.setInt(1, getId());
			statement.setInt(2, _SellerId);
			statement.setString(3, _SellerName);
			statement.setString(4, _SellerClanName);
			statement.setString(5, _ItemType);
			statement.setInt(6, _ItemId);
			statement.setInt(7, _ItemObjectId);
			statement.setString(8, _ItemName);
			statement.setInt(9, _ItemQuantity);
			statement.setInt(10, _StartingBid);
			statement.setInt(11, _CurrentBid);
			statement.setLong(12, _EndDate.getTimeInMillis());
			statement.execute();
			
			loadBid();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.load(): " + e.getMessage(), e);
		}
	}
	
	// =========================================================
	// Proeprty
	public final int getId()
	{
		return _Id;
	}
	
	public final int getCurrentBid()
	{
		return _CurrentBid;
	}
	
	public final Calendar getEndDate()
	{
		return _EndDate;
	}
	
	public final int getHighestBidderId()
	{
		return _HighestBidderId;
	}
	
	public final String getHighestBidderName()
	{
		return _HighestBidderName;
	}
	
	public final int getHighestBidderMaxBid()
	{
		return _HighestBidderMaxBid;
	}
	
	public final int getItemId()
	{
		return _ItemId;
	}
	
	public final String getItemName()
	{
		return _ItemName;
	}
	
	public final int getItemObjectId()
	{
		return _ItemObjectId;
	}
	
	public final int getItemQuantity()
	{
		return _ItemQuantity;
	}
	
	public final String getItemType()
	{
		return _ItemType;
	}
	
	public final int getSellerId()
	{
		return _SellerId;
	}
	
	public final String getSellerName()
	{
		return _SellerName;
	}
	
	public final String getSellerClanName()
	{
		return _SellerClanName;
	}
	
	public final int getStartingBid()
	{
		return _StartingBid;
	}
	
	public final Map<Integer, Bidder> getBidders()
	{
		return _bidders;
	}
}