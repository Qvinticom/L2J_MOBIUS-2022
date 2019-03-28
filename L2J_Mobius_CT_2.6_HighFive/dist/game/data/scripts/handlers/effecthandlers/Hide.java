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

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.World;
import com.l2jmobius.gameserver.model.actor.Creature;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.skills.BuffInfo;

/**
 * Hide effect implementation.
 * @author ZaKaX, nBd
 */
public final class Hide extends AbstractEffect
{
	public Hide(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public void onExit(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			final PlayerInstance player = info.getEffected().getActingPlayer();
			if (!player.inObserverMode())
			{
				player.setInvisible(false);
			}
		}
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		if (info.getEffected().isPlayer())
		{
			final PlayerInstance player = info.getEffected().getActingPlayer();
			player.setInvisible(true);
			
			if ((player.getAI().getNextIntention() != null) && (player.getAI().getNextIntention().getCtrlIntention() == CtrlIntention.AI_INTENTION_ATTACK))
			{
				player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
			}
			
			World.getInstance().forEachVisibleObject(player, Creature.class, target ->
			{
				if ((target != null) && (target.getTarget() == player))
				{
					target.setTarget(null);
					target.abortAttack();
					target.abortCast();
					target.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
			});
		}
	}
}