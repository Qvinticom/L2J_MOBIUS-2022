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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.entity.Wedding;

/**
 * @author evill33t
 */
public class CoupleManager
{
	protected static final Logger LOGGER = Logger.getLogger(CoupleManager.class.getName());
	
	// =========================================================
	// Data Field
	private final List<Wedding> _couples = new ArrayList<>();
	
	public static final CoupleManager getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public CoupleManager()
	{
		LOGGER.info("Initializing CoupleManager");
		_couples.clear();
		load();
	}
	
	// =========================================================
	// Method - Public
	public void reload()
	{
		_couples.clear();
		load();
	}
	
	// =========================================================
	// Method - Private
	private final void load()
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			PreparedStatement statement;
			ResultSet rs;
			
			statement = con.prepareStatement("Select id from mods_wedding order by id");
			rs = statement.executeQuery();
			
			while (rs.next())
			{
				_couples.add(new Wedding(rs.getInt("id")));
			}
			
			statement.close();
			rs.close();
			
			LOGGER.info("Loaded: " + _couples.size() + " couples(s)");
		}
		catch (Exception e)
		{
			LOGGER.warning("Exception: CoupleManager.load(): " + e.getMessage() + " " + e);
		}
	}
	
	// =========================================================
	// Property - Public
	public final Wedding getCouple(int coupleId)
	{
		final int index = getCoupleIndex(coupleId);
		if (index >= 0)
		{
			return _couples.get(index);
		}
		return null;
	}
	
	public void createCouple(L2PcInstance player1, L2PcInstance player2)
	{
		if ((player1 != null) && (player2 != null))
		{
			if ((player1.getPartnerId() == 0) && (player2.getPartnerId() == 0))
			{
				final int _player1id = player1.getObjectId();
				final int _player2id = player2.getObjectId();
				
				Wedding _new = new Wedding(player1, player2);
				_couples.add(_new);
				player1.setPartnerId(_player2id);
				player2.setPartnerId(_player1id);
				player1.setCoupleId(_new.getId());
				player2.setCoupleId(_new.getId());
			}
		}
	}
	
	public void deleteCouple(int coupleId)
	{
		final int index = getCoupleIndex(coupleId);
		Wedding wedding = _couples.get(index);
		
		if (wedding != null)
		{
			L2PcInstance player1 = (L2PcInstance) L2World.getInstance().findObject(wedding.getPlayer1Id());
			L2PcInstance player2 = (L2PcInstance) L2World.getInstance().findObject(wedding.getPlayer2Id());
			if (player1 != null)
			{
				player1.setPartnerId(0);
				player1.setMarried(false);
				player1.setCoupleId(0);
			}
			if (player2 != null)
			{
				player2.setPartnerId(0);
				player2.setMarried(false);
				player2.setCoupleId(0);
			}
			wedding.divorce();
			_couples.remove(index);
		}
	}
	
	public final int getCoupleIndex(int coupleId)
	{
		int i = 0;
		for (Wedding temp : _couples)
		{
			if ((temp != null) && (temp.getId() == coupleId))
			{
				return i;
			}
			i++;
		}
		return -1;
	}
	
	public final List<Wedding> getCouples()
	{
		return _couples;
	}
	
	private static class SingletonHolder
	{
		protected static final CoupleManager _instance = new CoupleManager();
	}
}
