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

import com.l2jmobius.gameserver.ai.CtrlEvent;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.geoengine.GeoEngine;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.Creature;
import com.l2jmobius.gameserver.model.actor.instance.DefenderInstance;
import com.l2jmobius.gameserver.model.actor.instance.FortCommanderInstance;
import com.l2jmobius.gameserver.model.actor.instance.SiegeFlagInstance;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.EffectFlag;
import com.l2jmobius.gameserver.model.items.instance.ItemInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.util.Util;

/**
 * Fear effect implementation.
 * @author littlecrow
 */
public final class Fear extends AbstractEffect
{
	private static final int FEAR_RANGE = 500;
	
	public Fear(StatsSet params)
	{
		
	}
	
	@Override
	public long getEffectFlags()
	{
		return EffectFlag.FEAR.getMask();
	}
	
	@Override
	public boolean canStart(Creature effector, Creature effected, Skill skill)
	{
		if ((effected == null) || effected.isRaid())
		{
			return false;
		}
		
		return effected.isPlayer() || effected.isSummon() || (effected.isAttackable() //
			&& !((effected instanceof DefenderInstance) || (effected instanceof FortCommanderInstance) //
				|| (effected instanceof SiegeFlagInstance) || (effected.getTemplate().getRace() == Race.SIEGE_WEAPON)));
	}
	
	@Override
	public int getTicks()
	{
		return 5;
	}
	
	@Override
	public boolean onActionTime(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		fearAction(null, effected);
		return false;
	}
	
	@Override
	public void onStart(Creature effector, Creature effected, Skill skill, ItemInstance item)
	{
		effected.getAI().notifyEvent(CtrlEvent.EVT_AFRAID);
		fearAction(effector, effected);
	}
	
	@Override
	public void onExit(Creature effector, Creature effected, Skill skill)
	{
		if (!effected.isPlayer())
		{
			effected.getAI().notifyEvent(CtrlEvent.EVT_THINK);
		}
	}
	
	private void fearAction(Creature effector, Creature effected)
	{
		final double radians = Math.toRadians((effector != null) ? Util.calculateAngleFrom(effector, effected) : Util.convertHeadingToDegree(effected.getHeading()));
		
		final int posX = (int) (effected.getX() + (FEAR_RANGE * Math.cos(radians)));
		final int posY = (int) (effected.getY() + (FEAR_RANGE * Math.sin(radians)));
		final int posZ = effected.getZ();
		
		final Location destination = GeoEngine.getInstance().canMoveToTargetLoc(effected.getX(), effected.getY(), effected.getZ(), posX, posY, posZ, effected.getInstanceWorld());
		effected.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, destination);
	}
}
