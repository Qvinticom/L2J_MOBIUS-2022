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

import com.l2jmobius.L2DatabaseFactory;
import com.l2jmobius.gameserver.datatables.ClanTable;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.entity.ClanHall;

import javolution.util.FastList;

public class ClanHallManager
{
	
	// =========================================================
	private static ClanHallManager _Instance;
	
	public static final ClanHallManager getInstance()
	{
		if (_Instance == null)
		{
			System.out.println("Initializing ClanHallManager");
			_Instance = new ClanHallManager();
			
		}
		return _Instance;
	}
	// =========================================================
	
	// =========================================================
	// Data Field
	private List<ClanHall> _ClanHalls;
	
	// =========================================================
	// Constructor
	public ClanHallManager()
	{
		load();
	}
	
	// =========================================================
	// Method - Private
	private final void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT * FROM clanhall ORDER BY id");
			ResultSet rs = statement.executeQuery())
		{
			while (rs.next())
			{
				if (rs.getInt("ownerId") != 0)
				{
					// just in case clan is deleted manually from db
					if (ClanTable.getInstance().getClan(rs.getInt("ownerId")) == null)
					{
						AuctionManager.initNPC(rs.getInt("id"));
					}
				}
				getClanHalls().add(new ClanHall(rs.getInt("id"), rs.getString("name"), rs.getInt("ownerId"), rs.getInt("lease"), rs.getString("desc"), rs.getString("location"), rs.getLong("paidUntil"), rs.getInt("Grade"), rs.getBoolean("paid")));
			}
			
			System.out.println("Loaded: " + getClanHalls().size() + " clan halls");
		}
		catch (final Exception e)
		{
			System.out.println("Exception: ClanHallManager.load(): " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	// =========================================================
	// Property - Public
	public final ClanHall getClanHallById(int clanHallId)
	{
		for (final ClanHall clanHall : getClanHalls())
		{
			if (clanHall.getId() == clanHallId)
			{
				return clanHall;
			}
		}
		return null;
	}
	
	public final ClanHall getNearbyClanHall(int x, int y, int maxDist)
	{
		for (final ClanHall ch : getClanHalls())
		{
			if ((ch.getZone() != null) && (ch.getZone().getDistanceToZone(x, y) < maxDist))
			{
				return ch;
			}
		}
		return null;
	}
	
	public final ClanHall getClanHallByOwner(L2Clan clan)
	{
		for (final ClanHall clanHall : getClanHalls())
		{
			if (clan.getClanId() == clanHall.getOwnerId())
			{
				return clanHall;
			}
		}
		return null;
	}
	
	public final List<ClanHall> getClanHalls()
	{
		if (_ClanHalls == null)
		{
			_ClanHalls = new FastList<>();
		}
		return _ClanHalls;
	}
}