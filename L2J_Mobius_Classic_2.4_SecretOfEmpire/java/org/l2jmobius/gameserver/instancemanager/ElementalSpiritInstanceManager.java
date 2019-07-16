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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.model.holders.ElementalSpiritDataHolder;

/**
 * @author Mobius
 */
public class ElementalSpiritInstanceManager
{
	private static final String LOAD_QUERY = "SELECT * FROM character_spirits WHERE charId=?";
	private static final String STORE_QUERY = "REPLACE INTO character_spirits (charId, type, level, stage, experience, attack_points, defense_points, crit_rate_points, crit_damage_points, in_use) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	public List<ElementalSpiritDataHolder> findByPlayerId(int playerId)
	{
		final List<ElementalSpiritDataHolder> result = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(LOAD_QUERY))
		{
			stmt.setInt(1, playerId);
			try (ResultSet rset = stmt.executeQuery())
			{
				while (rset.next())
				{
					final ElementalSpiritDataHolder newHolder = new ElementalSpiritDataHolder();
					newHolder.setCharId(rset.getInt("charId"));
					newHolder.setType(rset.getByte("type"));
					newHolder.setLevel(rset.getByte("level"));
					newHolder.setStage(rset.getByte("stage"));
					newHolder.setExperience(rset.getLong("experience"));
					newHolder.setAttackPoints(rset.getByte("attack_points"));
					newHolder.setDefensePoints(rset.getByte("defense_points"));
					newHolder.setCritRatePoints(rset.getByte("crit_rate_points"));
					newHolder.setCritDamagePoints(rset.getByte("crit_damage_points"));
					newHolder.setInUse(rset.getByte("in_use") == 1);
					result.add(newHolder);
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return result;
	}
	
	public void save(ElementalSpiritDataHolder data)
	{
		if (data == null)
		{
			return;
		}
		
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement(STORE_QUERY))
		{
			statement.setInt(1, data.getCharId());
			statement.setInt(2, data.getType());
			statement.setInt(3, data.getLevel());
			statement.setInt(4, data.getStage());
			statement.setLong(5, data.getExperience());
			statement.setInt(6, data.getAttackPoints());
			statement.setInt(7, data.getDefensePoints());
			statement.setInt(8, data.getCritRatePoints());
			statement.setInt(9, data.getCritDamagePoints());
			statement.setInt(10, data.isInUse() ? 1 : 0);
			statement.execute();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static ElementalSpiritInstanceManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final ElementalSpiritInstanceManager INSTANCE = new ElementalSpiritInstanceManager();
	}
}
