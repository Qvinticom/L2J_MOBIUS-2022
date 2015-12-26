/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * @author NviX
 */
public final class SummonDebuff extends AbstractEffect
{
	private static final int PRICE_OF_SUMMONING_LION = 10061;
	private static final int PRICE_OF_SUMMONING_LUMI = 11818;
	
	/**
	 * @param attachCond
	 * @param applyCond
	 * @param set
	 * @param params
	 */
	public SummonDebuff(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public boolean onActionTime(BuffInfo info)
	{
		final L2PcInstance player = info.getEffected().getActingPlayer();
		if (player.hasSummon())
		{
			if (player.getEffectList().isAffectedBySkill(PRICE_OF_SUMMONING_LION))
			{
				final Skill skill = SkillData.getInstance().getSkill(PRICE_OF_SUMMONING_LION, 1);
				skill.applyEffects(player, player);
				return true;
			}
			else if (player.getEffectList().isAffectedBySkill(PRICE_OF_SUMMONING_LUMI))
			{
				final Skill skill = SkillData.getInstance().getSkill(PRICE_OF_SUMMONING_LUMI, 1);
				skill.applyEffects(player, player);
				return true;
			}
		}
		return false;
	}
}
