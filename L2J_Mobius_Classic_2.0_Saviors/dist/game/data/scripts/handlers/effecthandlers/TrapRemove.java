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

import org.l2jmobius.gameserver.enums.TrapAction;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.Trap;
import org.l2jmobius.gameserver.model.effects.AbstractEffect;
import org.l2jmobius.gameserver.model.events.EventDispatcher;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnTrapAction;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.model.skill.Skill;
import org.l2jmobius.gameserver.network.SystemMessageId;

/**
 * Trap Remove effect implementation.
 * @author UnAfraid
 */
public class TrapRemove extends AbstractEffect
{
	private final int _power;
	
	public TrapRemove(StatSet params)
	{
		if (params.isEmpty())
		{
			throw new IllegalArgumentException(getClass().getSimpleName() + ": effect without power!");
		}
		
		_power = params.getInt("power");
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(Creature effector, Creature effected, Skill skill, Item item)
	{
		if (!effected.isTrap())
		{
			return;
		}
		
		if (effected.isAlikeDead())
		{
			return;
		}
		
		final Trap trap = (Trap) effected;
		if (!trap.canBeSeen(effector))
		{
			if (effector.isPlayer())
			{
				effector.sendPacket(SystemMessageId.INVALID_TARGET);
			}
			return;
		}
		
		if (trap.getLevel() > _power)
		{
			return;
		}
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnTrapAction(trap, effector, TrapAction.TRAP_DISARMED), trap);
		
		trap.unSummon();
		if (effector.isPlayer())
		{
			effector.sendPacket(SystemMessageId.THE_TRAP_DEVICE_HAS_BEEN_STOPPED);
		}
	}
}
