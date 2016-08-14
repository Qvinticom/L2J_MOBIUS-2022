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
import com.l2jmobius.gameserver.network.serverpackets.FlyToLocation.FlyType;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author Nos
 */
public class FlyMove extends AbstractEffect
{
	private final FlyType _flyType;
	private final int _angle;
	private final boolean _absoluteAngle; // Use map angle instead of character angle.
	private final int _range;
	private final boolean _selfPos; // Use the position and heading of yourself to move in the given range
	private final int _speed;
	private final int _delay;
	private final int _animationSpeed;
	
	public FlyMove(StatsSet params)
	{
		_flyType = params.getEnum("flyType", FlyType.class, FlyType.DUMMY);
		_angle = params.getInt("angle", 0);
		_absoluteAngle = params.getBoolean("absoluteAngle", false);
		_range = params.getInt("range", 20);
		_selfPos = params.getBoolean("selfPos", false);
		_speed = params.getInt("speed", 0);
		_delay = params.getInt("delay", 0);
		_animationSpeed = params.getInt("animationSpeed", 0);
	}
	
	@Override
	public void instant(L2Character effector, L2Character effected, Skill skill, L2ItemInstance item)
	{
		final L2Character target = _selfPos ? effector : effected;
		
		// Avoid calculating heading towards yourself because it always yields 0. Same results can be achieved with absoluteAngle of 0.
		final int heading = (_selfPos || (effector == effected)) ? effector.getHeading() : Util.calculateHeadingFrom(effector, effected);
		double angle = _absoluteAngle ? _angle : Util.convertHeadingToDegree(heading);
		angle = (angle + _angle) % 360;
		if (angle < 0)
		{
			angle += 360;
		}
		
		final double radiansAngle = Math.toRadians(angle);
		final int posX = (int) (target.getX() + (_range * Math.cos(radiansAngle)));
		final int posY = (int) (target.getY() + (_range * Math.sin(radiansAngle)));
		final int posZ = target.getZ();
		final Location destination = GeoData.getInstance().moveCheck(effector.getX(), effector.getY(), effector.getZ(), posX, posY, posZ, effector.getInstanceWorld());
		
		effector.broadcastPacket(new FlyToLocation(effector, destination, _flyType, _speed, _delay, _animationSpeed));
		effector.setXYZ(destination);
		effected.revalidateZone(true);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
}
