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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jmobius.Config;
import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.datatables.AccessLevel;

/**
 * @author FBIagent<br>
 */
public class AccessLevels
{
	private static final Logger LOGGER = Logger.getLogger(AccessLevels.class.getName());
	
	private static AccessLevels _instance = null;
	public AccessLevel _masterAccessLevel;
	public AccessLevel _userAccessLevel;
	private final Map<Integer, AccessLevel> _accessLevels = new HashMap<>();
	
	private AccessLevels()
	{
		_masterAccessLevel = new AccessLevel(Config.MASTERACCESS_LEVEL, "Master Access", Config.MASTERACCESS_NAME_COLOR, Config.MASTERACCESS_TITLE_COLOR, true, true, true, true, true, true, true, true, true, true, true);
		_userAccessLevel = new AccessLevel(Config.USERACCESS_LEVEL, "User", Integer.decode("0xFFFFFF"), Integer.decode("0xFFFFFF"), false, false, false, true, false, true, true, true, true, true, false);
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement stmt = con.prepareStatement("SELECT * FROM `access_levels` ORDER BY `accessLevel` DESC");
			final ResultSet rset = stmt.executeQuery();
			int accessLevel = 0;
			String name = null;
			int nameColor = 0;
			int titleColor = 0;
			boolean isGm = false;
			boolean allowPeaceAttack = false;
			boolean allowFixedRes = false;
			boolean allowTransaction = false;
			boolean allowAltG = false;
			boolean giveDamage = false;
			boolean takeAggro = false;
			boolean gainExp = false;
			
			boolean useNameColor = true;
			boolean useTitleColor = false;
			boolean canDisableGmStatus = true;
			
			while (rset.next())
			{
				accessLevel = rset.getInt("accessLevel");
				name = rset.getString("name");
				
				if (accessLevel == Config.USERACCESS_LEVEL)
				{
					LOGGER.info("AccessLevels: Access level with name " + name + " is using reserved user access level " + Config.USERACCESS_LEVEL + ". Ignoring it...");
					continue;
				}
				else if (accessLevel == Config.MASTERACCESS_LEVEL)
				{
					LOGGER.info("AccessLevels: Access level with name " + name + " is using reserved master access level " + Config.MASTERACCESS_LEVEL + ". Ignoring it...");
					continue;
				}
				else if (accessLevel < 0)
				{
					LOGGER.info("AccessLevels: Access level with name " + name + " is using banned access level state(below 0). Ignoring it...");
					continue;
				}
				
				try
				{
					nameColor = Integer.decode("0x" + rset.getString("nameColor"));
				}
				catch (NumberFormatException nfe)
				{
					LOGGER.warning(nfe.getMessage());
					
					try
					{
						nameColor = Integer.decode("0xFFFFFF");
					}
					catch (NumberFormatException nfe2)
					{
						LOGGER.warning(nfe.getMessage());
					}
				}
				
				try
				{
					titleColor = Integer.decode("0x" + rset.getString("titleColor"));
				}
				catch (NumberFormatException nfe)
				{
					LOGGER.warning(nfe.getMessage());
					
					try
					{
						titleColor = Integer.decode("0x77FFFF");
					}
					catch (NumberFormatException nfe2)
					{
						LOGGER.warning(nfe.getMessage());
					}
				}
				
				isGm = rset.getBoolean("isGm");
				allowPeaceAttack = rset.getBoolean("allowPeaceAttack");
				allowFixedRes = rset.getBoolean("allowFixedRes");
				allowTransaction = rset.getBoolean("allowTransaction");
				allowAltG = rset.getBoolean("allowAltg");
				giveDamage = rset.getBoolean("giveDamage");
				takeAggro = rset.getBoolean("takeAggro");
				gainExp = rset.getBoolean("gainExp");
				
				useNameColor = rset.getBoolean("useNameColor");
				useTitleColor = rset.getBoolean("useTitleColor");
				canDisableGmStatus = rset.getBoolean("canDisableGmStatus");
				
				_accessLevels.put(accessLevel, new AccessLevel(accessLevel, name, nameColor, titleColor, isGm, allowPeaceAttack, allowFixedRes, allowTransaction, allowAltG, giveDamage, takeAggro, gainExp, useNameColor, useTitleColor, canDisableGmStatus));
			}
			
			rset.close();
			stmt.close();
		}
		catch (SQLException e)
		{
			LOGGER.warning("AccessLevels: Error loading from database " + e);
		}
		LOGGER.info("AccessLevels: Master Access Level is " + Config.MASTERACCESS_LEVEL);
		LOGGER.info("AccessLevels: User Access Level is " + Config.USERACCESS_LEVEL);
	}
	
	/**
	 * Returns the one and only instance of this class<br>
	 * <br>
	 * @return AccessLevels: the one and only instance of this class<br>
	 */
	public static AccessLevels getInstance()
	{
		return _instance == null ? (_instance = new AccessLevels()) : _instance;
	}
	
	/**
	 * Returns the access level by characterAccessLevel<br>
	 * <br>
	 * @param accessLevelNum as int<br>
	 *            <br>
	 * @return AccessLevel: AccessLevel instance by char access level<br>
	 */
	public AccessLevel getAccessLevel(int accessLevelNum)
	{
		AccessLevel accessLevel = null;
		
		synchronized (_accessLevels)
		{
			accessLevel = _accessLevels.get(accessLevelNum);
		}
		return accessLevel;
	}
	
	public void addBanAccessLevel(int accessLevel)
	{
		synchronized (_accessLevels)
		{
			if (accessLevel > -1)
			{
				return;
			}
			
			_accessLevels.put(accessLevel, new AccessLevel(accessLevel, "Banned", Integer.decode("0x000000"), Integer.decode("0x000000"), false, false, false, false, false, false, false, false, false, false, false));
		}
	}
	
	public static void reload()
	{
		_instance = null;
		getInstance();
	}
}
