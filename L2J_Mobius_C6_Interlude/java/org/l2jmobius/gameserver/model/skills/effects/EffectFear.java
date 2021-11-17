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
package org.l2jmobius.gameserver.model.skills.effects;

import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.model.Effect;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Commander;
import org.l2jmobius.gameserver.model.actor.instance.Folk;
import org.l2jmobius.gameserver.model.actor.instance.FortSiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.SiegeFlag;
import org.l2jmobius.gameserver.model.actor.instance.SiegeGuard;
import org.l2jmobius.gameserver.model.actor.instance.SiegeSummon;
import org.l2jmobius.gameserver.model.skills.Env;

/**
 * @author littlecrow Implementation of the Fear Effect
 */
final class EffectFear extends Effect
{
	public static final int FEAR_RANGE = 500;
	
	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected().isSleeping())
		{
			getEffected().stopSleeping(null);
		}
		
		if (!getEffected().isAfraid())
		{
			getEffected().startFear();
			onActionTime();
		}
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopFear(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		// Fear skills cannot be used l2pcinstance to l2pcinstance. Heroic Dread, Curse: Fear, Fear and Horror are the exceptions.
		if ((getEffected() instanceof Player) && (getEffector() instanceof Player) && (getSkill().getId() != 1376) && (getSkill().getId() != 1169) && (getSkill().getId() != 65) && (getSkill().getId() != 1092))
		{
			return false;
		}
		
		if (getEffected() instanceof Folk)
		{
			return false;
		}
		
		if (getEffected() instanceof SiegeGuard)
		{
			return false;
		}
		
		// Fear skills cannot be used on Headquarters Flag.
		if (getEffected() instanceof SiegeFlag)
		{
			return false;
		}
		
		if (getEffected() instanceof SiegeSummon)
		{
			return false;
		}
		
		if ((getEffected() instanceof FortSiegeGuard) || (getEffected() instanceof Commander))
		{
			return false;
		}
		
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		final int posZ = getEffected().getZ();
		int signx = -1;
		int signy = -1;
		if (getEffected().getX() > getEffector().getX())
		{
			signx = 1;
		}
		if (getEffected().getY() > getEffector().getY())
		{
			signy = 1;
		}
		posX += signx * FEAR_RANGE;
		posY += signy * FEAR_RANGE;
		
		final Location destiny = GeoEngine.getInstance().getValidLocation(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ, getEffected().getInstanceId());
		getEffected().setRunning();
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(destiny.getX(), destiny.getY(), destiny.getZ(), 0));
		return true;
	}
}
