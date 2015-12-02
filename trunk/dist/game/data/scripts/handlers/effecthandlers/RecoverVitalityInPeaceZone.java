/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.stat.PcStat;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.zone.ZoneId;

/**
 * Recover Vitality in Peace Zone effect implementation.
 * @author Mobius
 */
public final class RecoverVitalityInPeaceZone extends AbstractEffect
{
	private final double _power;
	
	public RecoverVitalityInPeaceZone(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		
		_power = params.getDouble("power", 0);
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		if (!info.getEffected().isPlayer() || info.getEffected().isDead() || !info.getEffected().isInsideZone(ZoneId.PEACE))
		{
			return false;
		}
		
		long vitality = info.getEffected().getActingPlayer().getVitalityPoints();
		vitality += _power;
		if (vitality >= PcStat.MAX_VITALITY_POINTS)
		{
			vitality = PcStat.MAX_VITALITY_POINTS;
		}
		info.getEffected().getActingPlayer().setVitalityPoints((int) vitality);
		
		return info.getSkill().isToggle();
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (info.getEffected().isPlayer() && !info.isRemoved())
		{
			long vitality = info.getEffected().getActingPlayer().getVitalityPoints();
			vitality += 10000;
			if (vitality >= PcStat.MAX_VITALITY_POINTS)
			{
				vitality = PcStat.MAX_VITALITY_POINTS;
			}
			info.getEffected().getActingPlayer().setVitalityPoints((int) vitality);
		}
	}
}
