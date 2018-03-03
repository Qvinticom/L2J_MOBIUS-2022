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

import com.l2jmobius.gameserver.model.L2Object;
import com.l2jmobius.gameserver.model.actor.L2Character;

public class NullKnownList extends ObjectKnownList
{
	/**
	 * @param activeObject
	 */
	public NullKnownList(L2Object activeObject)
	{
		super(activeObject);
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#addKnownObject(com.l2jmobius.gameserver.model.L2Object, com.l2jmobius.gameserver.model.L2Character)
	 */
	@Override
	public boolean addKnownObject(L2Object object, L2Character dropper)
	{
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#addKnownObject(com.l2jmobius.gameserver.model.L2Object)
	 */
	@Override
	public boolean addKnownObject(L2Object object)
	{
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#getActiveObject()
	 */
	@Override
	public L2Object getActiveObject()
	{
		return super.getActiveObject();
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#getDistanceToForgetObject(com.l2jmobius.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToForgetObject(L2Object object)
	{
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#getDistanceToWatchObject(com.l2jmobius.gameserver.model.L2Object)
	 */
	@Override
	public int getDistanceToWatchObject(L2Object object)
	{
		return 0;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#removeAllKnownObjects() no-op
	 */
	@Override
	public void removeAllKnownObjects()
	{
		// null
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.l2jmobius.gameserver.model.actor.knownlist.ObjectKnownList#removeKnownObject(com.l2jmobius.gameserver.model.L2Object)
	 */
	@Override
	public boolean removeKnownObject(L2Object object)
	{
		return false;
	}
}
