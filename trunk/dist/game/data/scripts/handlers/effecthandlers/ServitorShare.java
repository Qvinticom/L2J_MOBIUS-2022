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

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.EffectFlag;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.stats.Stats;

/**
 * Servitor Share effect implementation. Have effect only on servitor's but not on pets Important: Only one effect can be used on char per time.
 * @author Zealar
 */
public final class ServitorShare extends AbstractEffect
{
	private final Map<Stats, Double> stats = new HashMap<>(9);
	
	public ServitorShare(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
		for (String key : params.getSet().keySet())
		{
			stats.put(Stats.valueOfXml(key), params.getDouble(key, 1.));
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		super.onStart(info);
		info.getEffected().getActingPlayer().setServitorShare(stats);
		if (info.getEffected().getActingPlayer().getServitors() != null)
		{
			for (L2Summon summon : info.getEffected().getActingPlayer().getServitors().values())
			{
				summon.broadcastInfo();
				summon.getStatus().startHpMpRegeneration();
			}
		}
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.SERVITOR_SHARE.getMask();
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.BUFF;
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().getActingPlayer().setServitorShare(null);
		if (info.getEffected().getServitors() != null)
		{
			for (L2Summon summon : info.getEffected().getActingPlayer().getServitors().values())
			{
				if (summon.getCurrentHp() > summon.getMaxHp())
				{
					summon.setCurrentHp(summon.getMaxHp());
				}
				if (summon.getCurrentMp() > summon.getMaxMp())
				{
					summon.setCurrentMp(summon.getMaxMp());
				}
				summon.broadcastInfo();
			}
		}
	}
}
