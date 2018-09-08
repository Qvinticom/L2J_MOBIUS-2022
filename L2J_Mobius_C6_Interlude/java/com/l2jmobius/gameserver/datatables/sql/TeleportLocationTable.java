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
package com.l2jmobius.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.model.L2TeleportLocation;

/**
 * This class ...
 * @version $Revision: 1.3.2.2.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class TeleportLocationTable
{
	private static final Logger LOGGER = Logger.getLogger(TeleportLocationTable.class.getName());
	
	private static TeleportLocationTable _instance;
	
	private Map<Integer, L2TeleportLocation> teleports;
	
	public static TeleportLocationTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new TeleportLocationTable();
		}
		
		return _instance;
	}
	
	private TeleportLocationTable()
	{
		reloadAll();
	}
	
	public void reloadAll()
	{
		teleports = new HashMap<>();
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
			final ResultSet rset = statement.executeQuery();
			L2TeleportLocation teleport;
			
			while (rset.next())
			{
				teleport = new L2TeleportLocation();
				
				teleport.setTeleId(rset.getInt("id"));
				teleport.setX(rset.getInt("loc_x"));
				teleport.setY(rset.getInt("loc_y"));
				teleport.setZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setIsForNoble(rset.getInt("fornoble") == 1);
				
				teleports.put(teleport.getTeleId(), teleport);
			}
			
			statement.close();
			rset.close();
			
			LOGGER.info("TeleportLocationTable: Loaded " + teleports.size() + " Teleport Location Templates");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating teleport table " + e);
		}
		
		if (Config.CUSTOM_TELEPORT_TABLE)
		{
			try (Connection con = DatabaseFactory.getConnection())
			{
				final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM custom_teleport");
				final ResultSet rset = statement.executeQuery();
				L2TeleportLocation teleport;
				
				int _cTeleCount = teleports.size();
				
				while (rset.next())
				{
					teleport = new L2TeleportLocation();
					teleport.setTeleId(rset.getInt("id"));
					teleport.setX(rset.getInt("loc_x"));
					teleport.setY(rset.getInt("loc_y"));
					teleport.setZ(rset.getInt("loc_z"));
					teleport.setPrice(rset.getInt("price"));
					teleport.setIsForNoble(rset.getInt("fornoble") == 1);
					teleports.put(teleport.getTeleId(), teleport);
				}
				
				statement.close();
				rset.close();
				
				_cTeleCount = teleports.size() - _cTeleCount;
				
				if (_cTeleCount > 0)
				{
					LOGGER.info("TeleportLocationTable: Loaded " + _cTeleCount + " Custom Teleport Location Templates.");
				}
			}
			catch (Exception e)
			{
				LOGGER.warning("Error while creating custom teleport table " + e);
			}
		}
	}
	
	/**
	 * @param id
	 * @return
	 */
	public L2TeleportLocation getTemplate(int id)
	{
		return teleports.get(id);
	}
}
