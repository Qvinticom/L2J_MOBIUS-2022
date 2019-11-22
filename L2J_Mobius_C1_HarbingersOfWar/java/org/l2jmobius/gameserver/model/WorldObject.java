/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.model;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.ClientThread;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;

public class WorldObject implements Serializable
{
	private int _objectId;
	private int _x;
	private int _y;
	private int _z;
	protected Set<WorldObject> _knownObjects = ConcurrentHashMap.newKeySet();
	private final Set<PlayerInstance> _knownPlayer = ConcurrentHashMap.newKeySet();
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}
	
	public int getX()
	{
		return _x;
	}
	
	public void setX(int x)
	{
		_x = x;
	}
	
	public int getY()
	{
		return _y;
	}
	
	public void setY(int y)
	{
		_y = y;
	}
	
	public int getZ()
	{
		return _z;
	}
	
	public void setZ(int z)
	{
		_z = z;
	}
	
	public void onAction(PlayerInstance player)
	{
		player.sendPacket(new ActionFailed());
	}
	
	public void onActionShift(ClientThread client)
	{
		client.getActiveChar().sendPacket(new ActionFailed());
	}
	
	public void onForcedAttack(PlayerInstance player)
	{
		player.sendPacket(new ActionFailed());
	}
	
	public Set<WorldObject> getKnownObjects()
	{
		return _knownObjects;
	}
	
	public void addKnownObject(WorldObject object)
	{
		_knownObjects.add(object);
		if (object instanceof PlayerInstance)
		{
			_knownPlayer.add((PlayerInstance) object);
		}
	}
	
	public void removeKnownObject(WorldObject object)
	{
		_knownObjects.remove(object);
		if (object instanceof PlayerInstance)
		{
			_knownPlayer.remove(object);
		}
	}
	
	public void removeAllKnownObjects()
	{
		_knownObjects.clear();
		for (WorldObject object : _knownObjects)
		{
			object.removeKnownObject(this);
		}
	}
	
	public Set<PlayerInstance> getKnownPlayers()
	{
		return _knownPlayer;
	}
}
