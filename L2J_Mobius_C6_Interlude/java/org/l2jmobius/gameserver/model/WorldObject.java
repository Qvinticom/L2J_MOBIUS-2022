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
package org.l2jmobius.gameserver.model;

import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.idfactory.IdFactory;
import org.l2jmobius.gameserver.instancemanager.ItemsOnGroundManager;
import org.l2jmobius.gameserver.instancemanager.MercTicketManager;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.actor.knownlist.WorldObjectKnownList;
import org.l2jmobius.gameserver.model.actor.poly.ObjectPoly;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.GameClient;
import org.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import org.l2jmobius.gameserver.network.serverpackets.GetItem;

/**
 * Mother class of all objects in the world which ones is it possible to interact (PC, NPC, Item...)<BR>
 * <BR>
 * WorldObject :<BR>
 * <BR>
 * <li>Creature</li>
 * <li>ItemInstance</li>
 * <li>Potion</li>
 */
public abstract class WorldObject
{
	private static final Logger LOGGER = Logger.getLogger(WorldObject.class.getName());
	
	private boolean _visible;
	private WorldObjectKnownList _knownList;
	private String _name;
	private int _objectId;
	private ObjectPoly _poly;
	private ObjectPosition _position;
	
	// Objects can only see objects in same instancezone, instance 0 is normal world -1 the all seeing world
	private int _instanceId = 0;
	
	public WorldObject(int objectId)
	{
		_objectId = objectId;
	}
	
	public void onAction(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onActionShift(GameClient client)
	{
		// Like L2OFF send to PlayerInstance
		onActionShift(client.getPlayer());
	}
	
	/**
	 * @param player
	 */
	public void onActionShift(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	public void onForcedAttack(PlayerInstance player)
	{
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	/**
	 * Do Nothing.<BR>
	 * <BR>
	 * <B><U> Overridden in </U> :</B><BR>
	 * <BR>
	 * <li>GuardInstance : Set the home location of its GuardInstance</li>
	 * <li>Attackable : Reset the Spoiled flag</li><BR>
	 * <BR>
	 */
	public void onSpawn()
	{
	}
	
	// Position - Should remove to fully move to WorldObjectPosition
	public void setXYZ(int x, int y, int z)
	{
		getPosition().setXYZ(x, y, z);
	}
	
	public void setXYZInvisible(int x, int y, int z)
	{
		getPosition().setXYZInvisible(x, y, z);
	}
	
	public int getX()
	{
		return getPosition().getX();
	}
	
	public int getY()
	{
		return getPosition().getY();
	}
	
	public int getZ()
	{
		return getPosition().getZ();
	}
	
	/**
	 * Remove a WorldObject from the world.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the WorldObject from the world</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of World </B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND Server->Client packets to players</B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion != null <I>(WorldObject is visible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Delete NPC/PC or Unsummon</li><BR>
	 * <BR>
	 */
	public void decayMe()
	{
		// Remove the WorldObject from the world
		_visible = false;
		World.getInstance().removeVisibleObject(this, getPosition().getWorldRegion());
		World.getInstance().removeObject(this);
		getPosition().setWorldRegion(null);
		
		if (Config.SAVE_DROPPED_ITEM)
		{
			ItemsOnGroundManager.getInstance().removeObject(this);
		}
	}
	
	/**
	 * Remove a ItemInstance from the world and send server->client GetItem packets.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Send a Server->Client Packet GetItem to player that pick up and its _knowPlayers member</li>
	 * <li>Remove the WorldObject from the world</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of World </B></FONT><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>this instanceof ItemInstance</li>
	 * <li>_worldRegion != null <I>(WorldObject is visible at the beginning)</I></li> <BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Do Pickup Item : PCInstance and Pet</li><BR>
	 * <BR>
	 * @param creature Player that pick up the item
	 */
	public void pickupMe(Creature creature) // NOTE: Should move this function into ItemInstance because it does not apply to Creature
	{
		WorldRegion oldregion = getPosition().getWorldRegion();
		
		// Create a server->client GetItem packet to pick up the ItemInstance
		GetItem gi = new GetItem((ItemInstance) this, creature.getObjectId());
		creature.broadcastPacket(gi);
		
		synchronized (this)
		{
			_visible = false;
			getPosition().setWorldRegion(null);
		}
		
		// if this item is a mercenary ticket, remove the spawns!
		if (this instanceof ItemInstance)
		{
			final int itemId = ((ItemInstance) this).getItemId();
			if (MercTicketManager.getInstance().getTicketCastleId(itemId) > 0)
			{
				MercTicketManager.getInstance().removeTicket((ItemInstance) this);
				ItemsOnGroundManager.getInstance().removeObject(this);
			}
		}
		
		// this can synchronize on others instancies, so it's out of synchronized, to avoid deadlocks
		// Remove the ItemInstance from the world
		World.getInstance().removeVisibleObject(this, oldregion);
	}
	
	public void refreshID()
	{
		World.getInstance().removeObject(this);
		IdFactory.getInstance().releaseId(getObjectId());
		_objectId = IdFactory.getInstance().getNextId();
	}
	
	/**
	 * Init the position of a WorldObject spawn and add it in the world as a visible object.<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Set the x,y,z position of the WorldObject spawn and update its _worldregion</li>
	 * <li>Add the WorldObject spawn in the _allobjects of World</li>
	 * <li>Add the WorldObject spawn to _visibleObjects of its WorldRegion</li>
	 * <li>Add the WorldObject spawn in the world as a <B>visible</B> object</li><BR>
	 * <BR>
	 * <B><U> Assert </U> :</B><BR>
	 * <BR>
	 * <li>_worldRegion == null <I>(WorldObject is invisible at the beginning)</I></li><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Create Door</li>
	 * <li>Spawn : Monster, Minion, CTs, Summon...</li><BR>
	 */
	public void spawnMe()
	{
		synchronized (this)
		{
			// Set the x,y,z position of the WorldObject spawn and update its _worldregion
			_visible = true;
			getPosition().setWorldRegion(World.getInstance().getRegion(getPosition().getWorldPosition()));
			
			// Add the WorldObject spawn in the _allobjects of World
			World.getInstance().storeObject(this);
			
			// Add the WorldObject spawn to _visibleObjects and if necessary to _allplayers of its WorldRegion
			getPosition().getWorldRegion().addVisibleObject(this);
		}
		
		// this can synchronize on others instances, so it's out of synchronized, to avoid deadlocks
		// Add the WorldObject spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, getPosition().getWorldRegion(), null);
		
		onSpawn();
	}
	
	public void spawnMe(int x, int y, int z)
	{
		synchronized (this)
		{
			// Set the x,y,z position of the WorldObject spawn and update its _worldregion
			_visible = true;
			
			if (x > World.MAP_MAX_X)
			{
				x = World.MAP_MAX_X - 5000;
			}
			if (x < World.MAP_MIN_X)
			{
				x = World.MAP_MIN_X + 5000;
			}
			if (y > World.MAP_MAX_Y)
			{
				y = World.MAP_MAX_Y - 5000;
			}
			if (y < World.MAP_MIN_Y)
			{
				y = World.MAP_MIN_Y + 5000;
			}
			
			getPosition().setWorldPosition(x, y, z);
			getPosition().setWorldRegion(World.getInstance().getRegion(getPosition().getWorldPosition()));
		}
		
		// these can synchronize on others instances, so they're out of synchronized, to avoid deadlocks
		// Add the WorldObject spawn in the _allobjects of World
		World.getInstance().storeObject(this);
		
		// Add the WorldObject spawn to _visibleObjects and if necessary to _allplayers of its WorldRegion
		final WorldRegion region = getPosition().getWorldRegion();
		if (region != null)
		{
			region.addVisibleObject(this);
		}
		else
		{
			LOGGER.info("ATTENTION: no region found for location " + x + "," + y + "," + z + ". It's not possible to spawn object " + _objectId + " here...");
			return;
		}
		// this can synchronize on others instances, so it's out of synchronized, to avoid deadlocks
		// Add the WorldObject spawn in the world as a visible object
		World.getInstance().addVisibleObject(this, region, null);
		
		onSpawn();
	}
	
	public void toggleVisible()
	{
		if (isVisible())
		{
			decayMe();
		}
		else
		{
			spawnMe();
		}
	}
	
	public abstract boolean isAutoAttackable(Creature attacker);
	
	/**
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * A WorldObject is visible if <B>__IsVisible</B>=true and <B>_worldregion</B>!=null <BR>
	 * <BR>
	 * @return the visibility state of the WorldObject.
	 */
	public boolean isVisible()
	{
		return getPosition().getWorldRegion() != null;
	}
	
	public void setVisible(boolean value)
	{
		_visible = value;
		
		if (!_visible)
		{
			getPosition().setWorldRegion(null);
		}
	}
	
	public WorldObjectKnownList getKnownList()
	{
		if (_knownList == null)
		{
			_knownList = new WorldObjectKnownList(this);
		}
		
		return _knownList;
	}
	
	public void setKnownList(WorldObjectKnownList value)
	{
		_knownList = value;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public void setName(String value)
	{
		_name = value;
	}
	
	public int getObjectId()
	{
		return _objectId;
	}
	
	public ObjectPoly getPoly()
	{
		if (_poly == null)
		{
			_poly = new ObjectPoly(this);
		}
		
		return _poly;
	}
	
	public ObjectPosition getPosition()
	{
		if (_position == null)
		{
			_position = new ObjectPosition(this);
		}
		
		return _position;
	}
	
	/**
	 * @return reference to region this object is in
	 */
	public WorldRegion getWorldRegion()
	{
		return getPosition().getWorldRegion();
	}
	
	/**
	 * @return The id of the instance zone the object is in - id 0 is global since everything like dropped items, mobs, players can be in a instantiated area, it must be in l2object
	 */
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	/**
	 * @param instanceId The id of the instance zone the object is in - id 0 is global
	 */
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
		
		// If we change it for visible objects, me must clear & revalidates knownlists
		if (_visible && (_knownList != null))
		{
			if (this instanceof PlayerInstance)
			{
				// We don't want some ugly looking disappear/appear effects, so don't update the knownlist here, but players usually enter instancezones through teleporting and the teleport will do the revalidation for us.
			}
			else
			{
				decayMe();
				spawnMe();
			}
		}
	}
	
	public PlayerInstance getActingPlayer()
	{
		return null;
	}
	
	public boolean isPlayer()
	{
		return false;
	}
	
	public boolean isPlayable()
	{
		return false;
	}
	
	public boolean isSummon()
	{
		return false;
	}
	
	public boolean isPet()
	{
		return false;
	}
	
	public boolean isCreature()
	{
		return false;
	}
	
	public boolean isAttackable()
	{
		return false;
	}
	
	public boolean isNpc()
	{
		return false;
	}
	
	public boolean isMonster()
	{
		return false;
	}
	
	public boolean isRaid()
	{
		return false;
	}
	
	public boolean isMinion()
	{
		return false;
	}
	
	public boolean isArtefact()
	{
		return false;
	}
	
	public boolean isDoor()
	{
		return false;
	}
	
	public boolean isBoat()
	{
		return false;
	}
	
	public boolean isItem()
	{
		return false;
	}
	
	@Override
	public String toString()
	{
		return "" + _objectId;
	}
}
