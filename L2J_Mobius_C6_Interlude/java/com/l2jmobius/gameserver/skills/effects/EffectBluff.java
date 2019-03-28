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

import com.l2jmobius.gameserver.model.Effect;
import com.l2jmobius.gameserver.model.actor.instance.ArtefactInstance;
import com.l2jmobius.gameserver.model.actor.instance.ControlTowerInstance;
import com.l2jmobius.gameserver.model.actor.instance.EffectPointInstance;
import com.l2jmobius.gameserver.model.actor.instance.FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.SiegeFlagInstance;
import com.l2jmobius.gameserver.model.actor.instance.SiegeSummonInstance;
import com.l2jmobius.gameserver.network.serverpackets.BeginRotation;
import com.l2jmobius.gameserver.network.serverpackets.StopRotation;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;
import com.l2jmobius.gameserver.skills.Env;

/**
 * @author programmos, sword developers Implementation of the Bluff Effect
 */
public class EffectBluff extends Effect
{
	
	public EffectBluff(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.BLUFF;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onStart()
	{
		if (getEffected().isDead() || getEffected().isAfraid())
		{
			return;
		}
		
		if ((getEffected() instanceof FolkInstance) || (getEffected() instanceof ControlTowerInstance) || (getEffected() instanceof ArtefactInstance) || (getEffected() instanceof EffectPointInstance) || (getEffected() instanceof SiegeFlagInstance) || (getEffected() instanceof SiegeSummonInstance))
		{
			return;
		}
		
		super.onStart();
		
		// break target
		getEffected().setTarget(null);
		// stop cast
		getEffected().breakCast();
		// stop attacking
		getEffected().breakAttack();
		// stop follow
		getEffected().getAI().stopFollow();
		// stop auto attack
		getEffected().getAI().clientStopAutoAttack();
		
		getEffected().broadcastPacket(new BeginRotation(getEffected(), getEffected().getHeading(), 1, 65535));
		getEffected().broadcastPacket(new StopRotation(getEffected(), getEffector().getHeading(), 65535));
		getEffected().setHeading(getEffector().getHeading());
		// sometimes rotation didn't showed correctly ??
		getEffected().sendPacket(new ValidateLocation(getEffector()));
		getEffector().sendPacket(new ValidateLocation(getEffected()));
		onActionTime();
	}
}
