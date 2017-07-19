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
package com.l2jmobius.gameserver.model.actor.knownlist;

import com.l2jmobius.gameserver.model.L2Attackable;
import com.l2jmobius.gameserver.model.L2Character;
import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PlayableInstance;

public class AttackableKnownList extends NpcKnownList
{
	// =========================================================
	// Data Field
	
	// =========================================================
	// Constructor
	public AttackableKnownList(L2Attackable activeChar)
	{
		super(activeChar);
	}
	
	// =========================================================
	// Method - Public
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		if (!super.removeKnownObject(object))
		{
			return false;
		}
		
		// Remove the L2Object from the _aggrolist of the L2Attackable
		if ((object != null) && (object instanceof L2Character))
		{
			getActiveChar().getAggroList().remove(object);
		}
		return true;
	}
	
	// =========================================================
	// Method - Private
	
	// =========================================================
	// Property - Public
	@Override
	public L2Attackable getActiveChar()
	{
		return (L2Attackable) super.getActiveChar();
	}
	
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 2 * getDistanceToWatchObject(object);
	}
	
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		if ((object instanceof L2FolkInstance) || !(object instanceof L2Character))
		{
			return 0;
		}
		
		if (object instanceof L2PlayableInstance)
		{
			return object.getKnownList().getDistanceToWatchObject(getActiveObject());
		}
		
		if ((getActiveChar().getFactionRange() + getActiveChar().getAggroRange()) > 300)
		{
			return (getActiveChar().getFactionRange() + getActiveChar().getAggroRange());
		}
		
		return 300;
	}
}