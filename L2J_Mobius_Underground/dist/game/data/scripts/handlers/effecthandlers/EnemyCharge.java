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
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.effects.AbstractEffect;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.model.skills.Skill;
import com.l2jmobius.gameserver.network.serverpackets.FlyToLocation;
import com.l2jmobius.gameserver.network.serverpackets.ValidateLocation;

/**
 * Enemy Charge effect implementation.
 */
public final class EnemyCharge extends AbstractEffect
{
	private final int _speed;
	private final int _delay;
	private final int _animationSpeed;
	
	public EnemyCharge(StatsSet params)
	{
		_speed = params.getInt("speed", 0);
		_delay = params.getInt("delay", 0);
		_animationSpeed = params.getInt("animationSpeed", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		if (effected.isMovementDisabled())
		{
			return;
		}
		
		// Get current position of the L2Character
		final int curX = effector.getX();
		final int curY = effector.getY();
		final int curZ = effector.getZ();
		
		// Calculate distance (dx,dy) between current position and destination
		final double dx = effected.getX() - curX;
		final double dy = effected.getY() - curY;
		final double dz = effected.getZ() - curZ;
		final double distance = Math.sqrt((dx * dx) + (dy * dy));
		if (distance > 2000)
		{
			_log.info("EffectEnemyCharge was going to use invalid coordinates for characters, getEffector: " + curX + "," + curY + " and getEffected: " + effected.getX() + "," + effected.getY());
			return;
		}
		
		int offset = Math.max((int) distance - skill.getFlyRadius(), 30);
		
		// approximation for moving closer when z coordinates are different
		// TODO: handle Z axis movement better
		offset -= Math.abs(dz);
		if (offset < 5)
		{
			offset = 5;
		}
		
		// If no distance
		if ((distance < 1) || ((distance - offset) <= 0))
		{
			return;
		}
		
		// Calculate movement angles needed
		final double sin = dy / distance;
		final double cos = dx / distance;
		
		// Calculate the new destination with offset included
		final int x = curX + (int) ((distance - offset) * cos);
		final int y = curY + (int) ((distance - offset) * sin);
		final int z = effected.getZ();
		
		final Location destination = GeoData.getInstance().moveCheck(effector.getX(), effector.getY(), effector.getZ(), x, y, z, effector.getInstanceWorld());
		
		effector.broadcastPacket(new FlyToLocation(effector, destination, skill.getFlyType(), _speed, _delay, _animationSpeed));
		
		// maybe is need force set X,Y,Z
		effected.setXYZ(destination);
		effected.broadcastPacket(new ValidateLocation(effector));
		effected.revalidateZone(true);
	}
}
