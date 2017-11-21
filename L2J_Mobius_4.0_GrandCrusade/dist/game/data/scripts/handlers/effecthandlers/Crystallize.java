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
package handlers.effecthandlers;

import com.l2jmobius.gameserver.enums.ItemGrade;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * An effect that allows the player to crystallize items up to a certain grade.
 * @author Nik
 */
public final class Crystallize extends AbstractEffect
{
	private final ItemGrade _grade;
	
	public Crystallize(StatsSet params)
	{
		_grade = params.getEnum("grade", ItemGrade.class);
	}
	
	@Override
	public boolean canStart(BuffInfo info)
	{
		return info.getEffected().isPlayer();
	}
	
	@Override
	public void onStart(L2Character effector, L2Character effected, Skill skill)
	{
		final L2PcInstance player = effected.getActingPlayer();
		if (player != null)
		{
			player.setCrystallizeGrade(_grade);
		}
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		final L2PcInstance player = info.getEffected().getActingPlayer();
		if (player != null)
		{
			player.setCrystallizeGrade(null);
		}
	}
}