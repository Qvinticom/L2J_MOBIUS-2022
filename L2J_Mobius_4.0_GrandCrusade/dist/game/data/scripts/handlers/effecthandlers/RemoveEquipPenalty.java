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

import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.items.type.CrystalType;
import com.l2jmobius.gameserver.model.skills.Skill;

/**
 * An effect that removes equipment grade penalty. Its the base effect for the grade penalty mechanics.
 * @author Nik
 */
public final class RemoveEquipPenalty extends AbstractEffect
{
	private final CrystalType _grade;
	
	public RemoveEquipPenalty(StatsSet params)
	{
		_grade = params.getEnum("grade", CrystalType.class);
	}
	
	@Override
	public boolean canStart(L2Character effector, L2Character effected, Skill skill)
	{
		return effected.isPlayer();
	}
	
	@Override
	public void onStart(L2Character effector, L2Character effected, Skill skill)
	{
		final L2PcInstance player = effected.getActingPlayer();
		if (player != null)
		{
			player.setExpertiseLevel(_grade);
		}
	}
	
	@Override
	public void onExit(L2Character effector, L2Character effected, Skill skill)
	{
		final L2PcInstance player = effected.getActingPlayer();
		if (player != null)
		{
			player.setExpertiseLevel(null);
		}
	}
}