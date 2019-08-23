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
package org.l2jmobius.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.datatables.csv.HennaTable;
import org.l2jmobius.gameserver.model.actor.instance.HennaInstance;
import org.l2jmobius.gameserver.model.base.ClassId;
import org.l2jmobius.gameserver.model.items.Henna;

public class HennaTreeTable
{
	private static Logger LOGGER = Logger.getLogger(HennaTreeTable.class.getName());
	
	private final Map<ClassId, List<HennaInstance>> _hennaTrees;
	private final boolean _initialized = true;
	
	private HennaTreeTable()
	{
		_hennaTrees = new HashMap<>();
		int classId = 0;
		int count = 0;
		
		try (Connection con = DatabaseFactory.getConnection())
		{
			final PreparedStatement statement = con.prepareStatement("SELECT class_name, id, parent_id FROM class_list ORDER BY id");
			final ResultSet classlist = statement.executeQuery();
			List<HennaInstance> list;
			
			classlist: while (classlist.next())
			{
				list = new ArrayList<>();
				classId = classlist.getInt("id");
				final PreparedStatement statement2 = con.prepareStatement("SELECT class_id, symbol_id FROM henna_trees where class_id=? ORDER BY symbol_id");
				statement2.setInt(1, classId);
				final ResultSet hennatree = statement2.executeQuery();
				
				while (hennatree.next())
				{
					final int id = hennatree.getInt("symbol_id");
					// String name = hennatree.getString("name");
					final Henna template = HennaTable.getInstance().getTemplate(id);
					
					if (template == null)
					{
						hennatree.close();
						statement2.close();
						classlist.close();
						statement.close();
						continue classlist;
					}
					
					final HennaInstance temp = new HennaInstance(template);
					temp.setSymbolId(id);
					temp.setItemIdDye(template.getDyeId());
					temp.setAmountDyeRequire(template.getAmountDyeRequire());
					temp.setPrice(template.getPrice());
					temp.setStatINT(template.getStatINT());
					temp.setStatSTR(template.getStatSTR());
					temp.setStatCON(template.getStatCON());
					temp.setStatMEM(template.getStatMEM());
					temp.setStatDEX(template.getStatDEX());
					temp.setStatWIT(template.getStatWIT());
					
					list.add(temp);
				}
				_hennaTrees.put(ClassId.values()[classId], list);
				
				hennatree.close();
				statement2.close();
				
				count += list.size();
			}
			
			classlist.close();
			statement.close();
		}
		catch (Exception e)
		{
			LOGGER.warning("Error while creating henna tree for classId " + classId + " " + e);
		}
		
		LOGGER.info("HennaTreeTable: Loaded " + count + " Henna Tree Templates.");
	}
	
	public HennaInstance[] getAvailableHenna(ClassId classId)
	{
		final List<HennaInstance> henna = _hennaTrees.get(classId);
		if (henna == null)
		{
			// the hennatree for this class is undefined, so we give an empty list
			LOGGER.warning("Hennatree for class " + classId + " is not defined!");
			return new HennaInstance[0];
		}
		
		return henna.toArray(new HennaInstance[henna.size()]);
	}
	
	public boolean isInitialized()
	{
		return _initialized;
	}
	
	public static HennaTreeTable getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final HennaTreeTable INSTANCE = new HennaTreeTable();
	}
}
