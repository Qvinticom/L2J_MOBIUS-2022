/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.effects.EffectFlag;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.network.serverpackets.FlyToLocation;
import com.l2jserver.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jserver.gameserver.network.serverpackets.ValidateLocation;

/**
 * Jump to Friend effect implementation.
 * @author Mobius
 */
public final class JumpToFriend extends AbstractEffect
{
	public JumpToFriend(Condition attachCond, Condition applyCond, StatsSet set, StatsSet params)
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
		
		final Location destination = GeoData.getInstance().moveCheck(info.getEffected().getX(), info.getEffected().getY(), info.getEffected().getZ(), info.getEffector().getX(), info.getEffector().getY(), info.getEffector().getZ(), info.getEffected().getInstanceId());
		
		info.getEffected().broadcastPacket(new FlyToLocation(info.getEffected(), destination, FlyType.JUMP_EFFECTED));
		info.getEffected().setXYZ(destination);
		info.getEffector().broadcastPacket(new ValidateLocation(info.getEffected()));
	}
}
