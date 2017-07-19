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
package com.l2jmobius.gameserver.model;

import java.util.List;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;

import javolution.util.FastList;

public class L2SiegeClan
{
	// ==========================================================================================
	// Instance
	// ===============================================================
	// Data Field
	private int _ClanId = 0;
	private List<L2NpcInstance> _Flag = new FastList<>();
	private int _numFlagsAdded = 0;
	private SiegeClanType _type;
	
	public enum SiegeClanType
	{
		OWNER,
		DEFENDER,
		ATTACKER,
		DEFENDER_PENDING
	}
	
	// =========================================================
	// Constructor
	
	public L2SiegeClan(int clanId, SiegeClanType type)
	{
		_ClanId = clanId;
		_type = type;
	}
	
	// =========================================================
	// Method - Public
	public int getNumFlags()
	{
		return _numFlagsAdded;
	}
	
	public void addFlag(L2NpcInstance flag)
	{
		_numFlagsAdded++;
		getFlag().add(flag);
	}
	
	public boolean removeFlag(L2NpcInstance flag)
	{
		if (flag == null)
		{
			return false;
		}
		final boolean ret = getFlag().remove(flag);
		// flag.deleteMe();
		// check if null objects or dups remain in the list.
		// for some reason, this might be happenning sometimes...
		// delete false dupplicates: if this flag got deleted, delete its copies too.
		if (ret)
		{
			while (getFlag().remove(flag))
			{
			}
		}
		
		flag.deleteMe();
		_numFlagsAdded--;
		return ret;
	}
	
	public void removeFlags()
	{
		for (final L2NpcInstance flag : getFlag())
		{
			removeFlag(flag);
		}
	}
	
	// =========================================================
	// Proeprty
	public final int getClanId()
	{
		return _ClanId;
	}
	
	public final List<L2NpcInstance> getFlag()
	{
		if (_Flag == null)
		{
			_Flag = new FastList<>();
		}
		return _Flag;
	}
	
	public SiegeClanType getType()
	{
		return _type;
	}
	
	public void setType(SiegeClanType setType)
	{
		_type = setType;
	}
}