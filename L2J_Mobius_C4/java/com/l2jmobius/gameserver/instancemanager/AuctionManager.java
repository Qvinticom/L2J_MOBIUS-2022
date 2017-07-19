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
package com.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.model.entity.Auction;

import javolution.util.FastList;

public class AuctionManager
{
	protected static Logger _log = Logger.getLogger(AuctionManager.class.getName());
	
	// =========================================================
	private static AuctionManager _Instance;
	
	public static final AuctionManager getInstance()
	{
		if (_Instance == null)
		{
			System.out.println("Initializing AuctionManager");
			_Instance = new AuctionManager();
			
		}
		return _Instance;
	}
	// =========================================================
	
	// =========================================================
	// Data Field
	private List<Auction> _Auctions;
	
	private static final String[] ITEM_INIT_DATA =
	{
		"(22, 0, 'NPC', 'NPC Clan', 'ClanHall', 22, 0, 'Moonstone Hall', 1, 20000000, 0, 1164841200000)",
		"(23, 0, 'NPC', 'NPC Clan', 'ClanHall', 23, 0, 'Onyx Hall', 1, 20000000, 0, 1164841200000)",
		"(24, 0, 'NPC', 'NPC Clan', 'ClanHall', 24, 0, 'Topaz Hall', 1, 20000000, 0, 1164841200000)",
		"(25, 0, 'NPC', 'NPC Clan', 'ClanHall', 25, 0, 'Ruby Hall', 1, 20000000, 0, 1164841200000)",
		"(26, 0, 'NPC', 'NPC Clan', 'ClanHall', 26, 0, 'Crystal Hall', 1, 20000000, 0, 1164841200000)",
		"(27, 0, 'NPC', 'NPC Clan', 'ClanHall', 27, 0, 'Onyx Hall', 1, 20000000, 0, 1164841200000)",
		"(28, 0, 'NPC', 'NPC Clan', 'ClanHall', 28, 0, 'Sapphire Hall', 1, 20000000, 0, 1164841200000)",
		"(29, 0, 'NPC', 'NPC Clan', 'ClanHall', 29, 0, 'Moonstone Hall', 1, 20000000, 0, 1164841200000)",
		"(30, 0, 'NPC', 'NPC Clan', 'ClanHall', 30, 0, 'Emerald Hall', 1, 20000000, 0, 1164841200000)",
		"(31, 0, 'NPC', 'NPC Clan', 'ClanHall', 31, 0, 'The Atramental Barracks', 1, 8000000, 0, 1164841200000)",
		"(32, 0, 'NPC', 'NPC Clan', 'ClanHall', 32, 0, 'The Scarlet Barracks', 1, 8000000, 0, 1164841200000)",
		"(33, 0, 'NPC', 'NPC Clan', 'ClanHall', 33, 0, 'The Viridian Barracks', 1, 8000000, 0, 1164841200000)",
		"(36, 0, 'NPC', 'NPC Clan', 'ClanHall', 36, 0, 'The Golden Chamber', 1, 50000000, 0, 1164841200000)",
		"(37, 0, 'NPC', 'NPC Clan', 'ClanHall', 37, 0, 'The Silver Chamber', 1, 50000000, 0, 1164841200000)",
		"(38, 0, 'NPC', 'NPC Clan', 'ClanHall', 38, 0, 'The Mithril Chamber', 1, 50000000, 0, 1164841200000)",
		"(39, 0, 'NPC', 'NPC Clan', 'ClanHall', 39, 0, 'Silver Manor', 1, 50000000, 0, 1164841200000)",
		"(40, 0, 'NPC', 'NPC Clan', 'ClanHall', 40, 0, 'Gold Manor', 1, 50000000, 0, 1164841200000)",
		"(41, 0, 'NPC', 'NPC Clan', 'ClanHall', 41, 0, 'The Bronze Chamber', 1, 50000000, 0, 1164841200000)",
		"(42, 0, 'NPC', 'NPC Clan', 'ClanHall', 42, 0, 'The Golden Chamber', 1, 50000000, 0, 1164841200000)",
		"(43, 0, 'NPC', 'NPC Clan', 'ClanHall', 43, 0, 'The Silver Chamber', 1, 50000000, 0, 1164841200000)",
		"(44, 0, 'NPC', 'NPC Clan', 'ClanHall', 44, 0, 'The Mithril Chamber', 1, 50000000, 0, 1164841200000)",
		"(45, 0, 'NPC', 'NPC Clan', 'ClanHall', 45, 0, 'The Bronze Chamber', 1, 50000000, 0, 1164841200000)",
		"(46, 0, 'NPC', 'NPC Clan', 'ClanHall', 46, 0, 'Silver Manor', 1, 50000000, 0, 1164841200000)",
		"(47, 0, 'NPC', 'NPC Clan', 'ClanHall', 47, 0, 'Moonstone Hall', 1, 50000000, 0, 1164841200000)",
		"(48, 0, 'NPC', 'NPC Clan', 'ClanHall', 48, 0, 'Onyx Hall', 1, 50000000, 0, 1164841200000)",
		"(49, 0, 'NPC', 'NPC Clan', 'ClanHall', 49, 0, 'Emerald Hall', 1, 50000000, 0, 1164841200000)",
		"(50, 0, 'NPC', 'NPC Clan', 'ClanHall', 50, 0, 'Sapphire Hall', 1, 50000000, 0, 1164841200000)"
	};
	
	private static final Integer[] ItemInitDataId =
	{
		22,
		23,
		24,
		25,
		26,
		27,
		28,
		29,
		30,
		31,
		32,
		33,
		36,
		37,
		38,
		39,
		40,
		41,
		42,
		43,
		44,
		45,
		46,
		47,
		48,
		49,
		50
	};
	
	// =========================================================
	// Constructor
	public AuctionManager()
	{
		load();
	}
	
	// =========================================================
	// Method - Public
	public void reload()
	{
		getAuctions().clear();
		load();
	}
	
	// =========================================================
	// Method - Private
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("Select id from auction order by id");
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				getAuctions().add(new Auction(rs.getInt("id")));
			}
			
			System.out.println("Loaded: " + getAuctions().size() + " auction(s)");
		}
		catch (final Exception e)
		{
			System.out.println("Exception: AuctionManager.load(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// =========================================================
	// Property - Public
	public final Auction getAuction(int auctionId)
	{
		final int index = getAuctionIndex(auctionId);
		if (index >= 0)
		{
			return getAuctions().get(index);
		}
		return null;
	}
	
	public final int getAuctionIndex(int auctionId)
	{
		Auction auction;
		for (int i = 0; i < getAuctions().size(); i++)
		{
			auction = getAuctions().get(i);
			if ((auction != null) && (auction.getId() == auctionId))
			{
				return i;
			}
		}
		return -1;
	}
	
	public final List<Auction> getAuctions()
	{
		if (_Auctions == null)
		{
			_Auctions = new FastList<>();
		}
		return _Auctions;
	}
	
	public static boolean initNPC(int id)
	{
		int i = 0;
		for (i = 0; i < ItemInitDataId.length; i++)
		{
			if (ItemInitDataId[i] == id)
			{
				break;
			}
		}
		
		if (i >= ItemInitDataId.length)
		{
			_log.warning("Clan Hall auction not found for Id :" + id);
			return false;
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("INSERT INTO `auction` VALUES " + ITEM_INIT_DATA[i]))
		{
			statement.execute();
		}
		catch (final Exception e)
		{
			_log.log(Level.SEVERE, "Exception: Auction.initNPC(): " + e.getMessage(), e);
			return false;
		}
		return true;
	}
}