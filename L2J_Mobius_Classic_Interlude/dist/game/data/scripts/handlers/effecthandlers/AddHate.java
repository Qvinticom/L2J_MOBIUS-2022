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

import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;

/**
 * Add Hate effect implementation.
 * @author Adry_85
 */
public class AddHate extends AbstractEffect
{
	private final double _power;
	private final boolean _affectSummoner;
	
	public AddHate(StatSet params)
	{
		_power = params.getDouble("power", 0);
		_affectSummoner = params.getBoolean("affectSummoner", false);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature creature, Creature effected, Skill skill, Item item)
	{
		Creature effector = creature;
		if (_affectSummoner && (effector.getSummoner() != null))
		{
			effector = effector.getSummoner();
		}
		
		if (!effected.isAttackable())
		{
			return;
		}
		
		final double val = _power;
		if (val > 0)
		{
			((Attackable) effected).addDamageHate(effector, 0, (int) val);
			effected.setRunning();
		}
		else if (val < 0)
		{
			((Attackable) effected).reduceHate(effector, (int) -val);
		}
	}
}
