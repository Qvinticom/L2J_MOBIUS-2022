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
package com.l2jmobius.gameserver.model.actor.stat;

import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.skills.Stats;

public class NpcStat extends CharStat
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public NpcStat(L2NpcInstance activeChar)
	{
		super(activeChar);
		
		setLevel(getActiveChar().getTemplate().level);
	}
	
	// =========================================================
	// Method - Public
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public L2NpcInstance getActiveChar()
	{
		return (L2NpcInstance) super.getActiveChar();
	}
	
	@Override
	public final int getMaxHp()
	{
		if (getActiveChar().getMaxSiegeHp() > 0)
		{
			return getActiveChar().getMaxSiegeHp();
		}
		
		return (int) calcStat(Stats.MAX_HP, getActiveChar().getTemplate().baseHpMax, null, null);
	}
	
	@Override
	public int getWalkSpeed()
	{
		return (int) calcStat(Stats.WALK_SPEED, getActiveChar().getTemplate().baseWalkSpd, null, null);
	}
	
	@Override
	public float getMovementSpeedMultiplier()
	{
		if (getActiveChar() == null)
		{
			return 1;
		}
		
		if (getActiveChar().isRunning())
		{
			return (getRunSpeed() * 1f) / getActiveChar().getTemplate().baseRunSpd;
		}
		return (getWalkSpeed() * 1f) / getActiveChar().getTemplate().baseWalkSpd;
	}
}