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

import com.l2jmobius.commons.database.DatabaseFactory;
import com.l2jmobius.gameserver.model.L2LvlupData;
import com.l2jmobius.gameserver.model.base.ClassId;

/**
 * This class ...
 * @author NightMarez
 * @version $Revision: 1.3.2.4.2.3 $ $Date: 2005/03/27 15:29:18 $
 */
public class LevelUpData
{
	private static final String SELECT_ALL = "SELECT classid, defaulthpbase, defaulthpadd, defaulthpmod, defaultcpbase, defaultcpadd, defaultcpmod, defaultmpbase, defaultmpadd, defaultmpmod, class_lvl FROM lvlupgain";
	private static final String CLASS_LVL = "class_lvl";
	private static final String MP_MOD = "defaultmpmod";
	private static final String MP_ADD = "defaultmpadd";
	private static final String MP_BASE = "defaultmpbase";
	private static final String HP_MOD = "defaulthpmod";
	private static final String HP_ADD = "defaulthpadd";
	private static final String HP_BASE = "defaulthpbase";
	private static final String CP_MOD = "defaultcpmod";
	private static final String CP_ADD = "defaultcpadd";
	private static final String CP_BASE = "defaultcpbase";
	private static final String CLASS_ID = "classid";
	
	private static final Logger LOGGER = Logger.getLogger(LevelUpData.class.getName());
	
	private static LevelUpData _instance;
	
	private final Map<Integer, L2LvlupData> lvlTable;
	
	public static LevelUpData getInstance()
	{
		if (_instance == null)
		{
			_instance = new LevelUpData();
		}
		
		return _instance;
	}
	
	private LevelUpData()
	{
		lvlTable = new HashMap<>();
		try (Connection con = DatabaseFactory.getInstance().getConnection())
		{
			final PreparedStatement statement = con.prepareStatement(SELECT_ALL);
			final ResultSet rset = statement.executeQuery();
			L2LvlupData lvlDat;
			
			while (rset.next())
			{
				lvlDat = new L2LvlupData();
				lvlDat.setClassid(rset.getInt(CLASS_ID));
				lvlDat.setClassLvl(rset.getInt(CLASS_LVL));
				lvlDat.setClassHpBase(rset.getFloat(HP_BASE));
				lvlDat.setClassHpAdd(rset.getFloat(HP_ADD));
				lvlDat.setClassHpModifier(rset.getFloat(HP_MOD));
				lvlDat.setClassCpBase(rset.getFloat(CP_BASE));
				lvlDat.setClassCpAdd(rset.getFloat(CP_ADD));
				lvlDat.setClassCpModifier(rset.getFloat(CP_MOD));
				lvlDat.setClassMpBase(rset.getFloat(MP_BASE));
				lvlDat.setClassMpAdd(rset.getFloat(MP_ADD));
				lvlDat.setClassMpModifier(rset.getFloat(MP_MOD));
				
				lvlTable.put(new Integer(lvlDat.getClassid()), lvlDat);
			}
			
			statement.close();
			rset.close();
			
			LOGGER.info("LevelUpData: Loaded " + lvlTable.size() + " Character Level Up Templates.");
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating Lvl up data table " + e);
		}
	}
	
	/**
	 * @param classId
	 * @return
	 */
	public L2LvlupData getTemplate(int classId)
	{
		return lvlTable.get(classId);
	}
	
	public L2LvlupData getTemplate(ClassId classId)
	{
		return lvlTable.get(classId.getId());
	}
}
