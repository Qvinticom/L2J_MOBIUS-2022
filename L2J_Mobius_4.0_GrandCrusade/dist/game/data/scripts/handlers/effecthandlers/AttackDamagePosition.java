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

import com.l2jmobius.commons.util.MathUtil;
import com.l2jmobius.gameserver.enums.Position;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.model.stats.Stats;

/**
 * @author Nik
 */
public class AttackDamagePosition extends AbstractEffect
{
	protected final double _amount;
	protected final Position _position;
	
	public AttackDamagePosition(StatsSet params)
	{
		_amount = params.getDouble("amount");
		_position = params.getEnum("position", Position.class);
	}
	
	@Override
	public void pump(L2Character effected, Skill skill)
	{
		effected.getStat().mergePositionTypeValue(Stats.ATTACK_DAMAGE, _position, (_amount / 100) + 1, MathUtil::mul);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		info.getEffected().getStat().mergePositionTypeValue(Stats.ATTACK_DAMAGE, _position, (_amount / 100) + 1, MathUtil::div);
	}
}
