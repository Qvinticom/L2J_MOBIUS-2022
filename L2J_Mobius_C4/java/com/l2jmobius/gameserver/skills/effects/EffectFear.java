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
package com.l2jmobius.gameserver.skills.effects;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.geodata.GeoData;
import com.l2jmobius.gameserver.model.L2CharPosition;
import com.l2jmobius.gameserver.model.L2Effect;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2SiegeSummonInstance;
import com.l2jmobius.gameserver.skills.Env;

/**
 * @author littlecrow Implementation of the Fear Effect
 */
final class EffectFear extends L2Effect
{
	public static final int FEAR_RANGE = 500;
	
	private int _dX = -1;
	private int _dY = -1;
	
	public EffectFear(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.FEAR;
	}
	
	/** Notify started */
	@Override
	public void onStart()
	{
		if (getEffected() instanceof L2FolkInstance)
		{
			return;
		}
		
		if ((getEffected() instanceof L2NpcInstance) && (((L2NpcInstance) getEffected()).getNpcId() == 12024))
		{
			return;
		}
		
		if (getEffected() instanceof L2SiegeSummonInstance)
		{
			return;
		}
		
		// players are only affected by grandboss skills
		if ((getEffected() instanceof L2PcInstance) && (getEffector() instanceof L2PcInstance))
		{
			return;
		}
		
		if (!getEffected().isAfraid())
		{
			if (getEffected().getX() > getEffector().getX())
			{
				_dX = 1;
			}
			if (getEffected().getY() > getEffector().getY())
			{
				_dY = 1;
			}
			
			getEffected().startFear();
			onActionTime();
			
		}
	}
	
	/** Notify exited */
	@Override
	public void onExit()
	
	{
		getEffected().stopFear(this);
	}
	
	@Override
	public boolean onActionTime()
	{
		int posX = getEffected().getX();
		int posY = getEffected().getY();
		final int posZ = getEffected().getZ();
		
		posX += _dX * FEAR_RANGE;
		posY += _dY * FEAR_RANGE;
		
		if (Config.PATHFINDING > 0)
		{
			final Location destiny = GeoData.getInstance().moveCheck(getEffected().getX(), getEffected().getY(), getEffected().getZ(), posX, posY, posZ);
			posX = destiny.getX();
			posY = destiny.getY();
		}
		
		if (!(getEffected() instanceof L2PetInstance))
		{
			getEffected().setRunning();
		}
		
		getEffected().getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new L2CharPosition(posX, posY, posZ, 0));
		return true;
		
	}
}