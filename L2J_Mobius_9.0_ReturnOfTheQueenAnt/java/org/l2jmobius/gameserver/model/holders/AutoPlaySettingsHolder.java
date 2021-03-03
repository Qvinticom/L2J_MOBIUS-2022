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
package org.l2jmobius.gameserver.model.holders;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Mobius
 */
public class AutoPlaySettingsHolder
{
	private final AtomicBoolean _pickup = new AtomicBoolean();
	private final AtomicBoolean _longRange = new AtomicBoolean();
	private final AtomicBoolean _respectfulHunting = new AtomicBoolean();
	private final AtomicInteger _autoPotionPercent = new AtomicInteger();
	
	public AutoPlaySettingsHolder()
	{
	}
	
	public boolean doPickup()
	{
		return _pickup.get();
	}
	
	public void setPickup(boolean value)
	{
		_pickup.set(value);
	}
	
	public boolean isLongRange()
	{
		return _longRange.get();
	}
	
	public void setLongRange(boolean value)
	{
		_longRange.set(value);
	}
	
	public boolean isRespectfulHunting()
	{
		return _respectfulHunting.get();
	}
	
	public void setRespectfulHunting(boolean value)
	{
		_respectfulHunting.set(value);
	}
	
	public int getAutoPotionPercent()
	{
		return _autoPotionPercent.get();
	}
	
	public void setAutoPotionPercent(int value)
	{
		_autoPotionPercent.set(value);
	}
}
