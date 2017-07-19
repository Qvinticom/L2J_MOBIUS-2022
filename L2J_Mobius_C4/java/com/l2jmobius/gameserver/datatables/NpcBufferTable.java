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
package com.l2jmobius.gameserver.datatables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.l2jmobius.L2DatabaseFactory;

import javolution.util.FastMap;

public class NpcBufferTable
{
	private static NpcBufferTable _instance = null;
	
	private final Map<Integer, Integer> _skillId = new FastMap<>();
	private final Map<Integer, Integer> _skillLevels = new FastMap<>();
	private final Map<Integer, Integer> _skillFeeIds = new FastMap<>();
	private final Map<Integer, Integer> _skillFeeAmounts = new FastMap<>();
	
	public void addSkill(int skillId, int skillLevel, int skillFeeId, int skillFeeAmount, int buffGroup)
	{
		_skillId.put(buffGroup, skillId);
		_skillLevels.put(buffGroup, skillLevel);
		_skillFeeIds.put(buffGroup, skillFeeId);
		_skillFeeAmounts.put(buffGroup, skillFeeAmount);
	}
	
	public int[] getSkillGroupInfo(int buffGroup)
	{
		final Integer skillId = _skillId.get(buffGroup);
		final Integer skillLevel = _skillLevels.get(buffGroup);
		final Integer skillFeeId = _skillFeeIds.get(buffGroup);
		final Integer skillFeeAmount = _skillFeeAmounts.get(buffGroup);
		
		if ((skillId == null) || (skillLevel == null) || (skillFeeId == null) || (skillFeeAmount == null))
		{
			return null;
		}
		
		return new int[]
		{
			skillId,
			skillLevel,
			skillFeeId,
			skillFeeAmount
		};
	}
	
	private NpcBufferTable()
	{
		int skillCount = 0;
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement("SELECT skill_id, skill_level, skill_fee_id, skill_fee_amount, buff_group FROM npc_buffer order by id");
			ResultSet rset = statement.executeQuery())
		{
			while (rset.next())
			{
				
				final int skillId = rset.getInt("skill_id");
				final int skillLevel = rset.getInt("skill_level");
				final int skillFeeId = rset.getInt("skill_fee_id");
				final int skillFeeAmount = rset.getInt("skill_fee_amount");
				final int buffGroup = rset.getInt("buff_group");
				
				addSkill(skillId, skillLevel, skillFeeId, skillFeeAmount, buffGroup);
				skillCount++;
			}
		}
		catch (final Exception e)
		{
			System.out.println("NpcBufferTable: Error reading npc_buffer table: " + e);
		}
		
		System.out.println("NpcBufferTable: Loaded " + skillCount + " skills.");
	}
	
	public static NpcBufferTable getInstance()
	{
		if (_instance == null)
		{
			_instance = new NpcBufferTable();
		}
		
		return _instance;
	}
}