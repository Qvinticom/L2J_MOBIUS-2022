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

import com.l2jmobius.gameserver.GeoData;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.conditions.Condition;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.effects.EffectFlag;
import com.l2jmobius.gameserver.model.skills.BuffInfo;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.FlyToLocation;
import com.l2jmobius.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * Throw Horizontal effect implementation.
 */
public final class ThrowHorizontal extends AbstractEffect
{
	public ThrowHorizontal(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
	{
		super(attachCond, applyCond, set, params);
	}
	
	@Override
	public int getEffectFlags()
	{
		return EffectFlag.STUNNED.getMask();
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void onStart(BuffInfo info)
	{
		// Get current position of the L2Character
		final int curX = info.getEffected().getX();
		final int curY = info.getEffected().getY();
		
		// Calculate distance between effector and effected current position
		final double dx = info.getEffector().getX() - curX;
		final double dy = info.getEffector().getY() - curY;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			_log.info("EffectThrow was going to use invalid coordinates for characters, getEffected: " + curX + "," + curY + " and getEffector: " + info.getEffector().getX() + "," + info.getEffector().getY());
			return;
		}
		
		// If no distance
		if (distance < 1)
		{
			return;
		}
		
		final int x = info.getEffector().getX();
		final int y = info.getEffector().getY();
		final int z = info.getEffected().getZ();
		
		// Prevent using skill with this effect on NPC that not monster
		if (!(info.getEffected().isMonster() || info.getEffected().isRaidMinion() || info.getEffected().isMinion() || info.getEffected().isSummon() || info.getEffected().isPlayer()))
		{
			final L2PcInstance effector = (L2PcInstance) info.getEffector();
			effector.sendPacket(SystemMessageId.INVALID_TARGET);
			return;
		}
		final Location destination = GeoData.getInstance().moveCheck(info.getEffected().getX(), info.getEffected().getY(), info.getEffected().getZ(), x, y, z, info.getEffected().getInstanceId());
		
		info.getEffected().abortAttack();
		info.getEffected().abortCast();
		info.getEffected().broadcastPacket(new FlyToLocation(info.getEffected(), destination, FlyType.THROW_HORIZONTAL, 1000, 1, 1000));
		// TODO: Review.
		info.getEffected().setXYZ(destination);
		info.getEffected().broadcastPacket(new ValidateLocation(info.getEffected()));
	}
}