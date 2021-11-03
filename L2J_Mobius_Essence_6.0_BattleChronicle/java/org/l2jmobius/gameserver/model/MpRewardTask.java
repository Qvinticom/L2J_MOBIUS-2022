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
package org.l2jmobius.gameserver.model;

import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;

/**
 * @author UnAfraid
 */
public class MpRewardTask
{
	private int _count;
	private final double _value;
	private final ScheduledFuture<?> _task;
	private final Creature _creature;
	
	public MpRewardTask(Creature creature, Npc npc)
	{
		final NpcTemplate template = npc.getTemplate();
		_creature = creature;
		_count = template.getMpRewardTicks();
		_value = calculateBaseValue(npc, creature);
		_task = ThreadPool.scheduleAtFixedRate(this::run, Config.EFFECT_TICK_RATIO, Config.EFFECT_TICK_RATIO);
	}
	
	/**
	 * @param npc
	 * @param creature
	 * @return
	 */
	private double calculateBaseValue(Npc npc, Creature creature)
	{
		final NpcTemplate template = npc.getTemplate();
		switch (template.getMpRewardType())
		{
			case PER:
			{
				return (creature.getMaxMp() * (template.getMpRewardValue() / 100)) / template.getMpRewardTicks();
			}
		}
		return template.getMpRewardValue() / template.getMpRewardTicks();
	}
	
	private void run()
	{
		if ((--_count <= 0) || (_creature.isPlayer() && !_creature.getActingPlayer().isOnline()))
		{
			_task.cancel(false);
			return;
		}
		
		_creature.setCurrentMp(_creature.getCurrentMp() + _value);
	}
}
