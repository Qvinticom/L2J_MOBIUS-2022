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
package com.l2jmobius.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.l2jmobius.commons.util.Point3D;
import com.l2jmobius.commons.util.object.L2ObjectMap;
import com.l2jmobius.commons.util.object.L2ObjectSet;
import com.l2jmobius.gameserver.datatables.GmListTable;
import com.l2jmobius.gameserver.instancemanager.PlayerCountManager;
import com.l2jmobius.gameserver.model.actor.Creature;
import com.l2jmobius.gameserver.model.actor.Playable;
import com.l2jmobius.gameserver.model.actor.instance.PetInstance;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;

/**
 * @version $Revision: 1.21.2.5.2.7 $ $Date: 2005/03/27 15:29:32 $
 */
public final class World
{
	private static Logger LOGGER = Logger.getLogger(World.class.getName());
	
	public static final int SHIFT_BY = 12;
	
	// Geodata min/max tiles
	public static final int TILE_X_MIN = 16;
	public static final int TILE_X_MAX = 26;
	public static final int TILE_Y_MIN = 10;
	public static final int TILE_Y_MAX = 25;
	
	// Map dimensions
	public static final int TILE_SIZE = 32768;
	public static final int MAP_MIN_X = (TILE_X_MIN - 20) * TILE_SIZE;
	public static final int MAP_MAX_X = (TILE_X_MAX - 19) * TILE_SIZE;
	public static final int MAP_MIN_Y = (TILE_Y_MIN - 18) * TILE_SIZE;
	public static final int MAP_MAX_Y = (TILE_Y_MAX - 17) * TILE_SIZE;
	
	/** calculated offset used so top left region is 0,0. */
	public static final int OFFSET_X = Math.abs(MAP_MIN_X >> SHIFT_BY);
	
	/** The Constant OFFSET_Y. */
	public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> SHIFT_BY);
	
	/** number of regions. */
	private static final int REGIONS_X = (MAP_MAX_X >> SHIFT_BY) + OFFSET_X;
	
	/** The Constant REGIONS_Y. */
	private static final int REGIONS_Y = (MAP_MAX_Y >> SHIFT_BY) + OFFSET_Y;
	
	/** HashMap(String Player name, PlayerInstance) containing all the players in game. */
	private static Map<String, PlayerInstance> _allPlayers = new ConcurrentHashMap<>();
	
	/** WorldObjectHashMap(WorldObject) containing all visible objects. */
	private static L2ObjectMap<WorldObject> _allObjects;
	
	/** List with the pets instances and their owner id. */
	private final Map<Integer, PetInstance> _petsInstance;
	
	/** The _instance. */
	private static World _instance = null;
	
	/** The _world regions. */
	private WorldRegion[][] _worldRegions;
	
	private World()
	{
		_petsInstance = new ConcurrentHashMap<>();
		_allObjects = L2ObjectMap.createL2ObjectMap();
		
		initRegions();
	}
	
	/**
	 * Gets the single instance of World.
	 * @return the current instance of World.
	 */
	public static World getInstance()
	{
		if (_instance == null)
		{
			_instance = new World();
		}
		return _instance;
	}
	
	/**
	 * Add WorldObject object in _allObjects.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Withdraw an item from the warehouse, create an item</li>
	 * <li>Spawn a Creature (PC, NPC, Pet)</li><BR>
	 * @param object the object
	 */
	public void storeObject(WorldObject object)
	{
		if (_allObjects.get(object.getObjectId()) != null)
		{
			return;
		}
		_allObjects.put(object);
	}
	
	/**
	 * Time store object.
	 * @param object the object
	 * @return the long
	 */
	public long timeStoreObject(WorldObject object)
	{
		long time = System.currentTimeMillis();
		_allObjects.put(object);
		time -= System.currentTimeMillis();
		
		return time;
	}
	
	/**
	 * Remove WorldObject object from _allObjects of World.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Delete item from inventory, tranfer Item from inventory to warehouse</li>
	 * <li>Crystallize item</li>
	 * <li>Remove NPC/PC/Pet from the world</li><BR>
	 * @param object WorldObject to remove from _allObjects of World
	 */
	public void removeObject(WorldObject object)
	{
		_allObjects.remove(object);
	}
	
	/**
	 * Removes the objects.
	 * @param list the list
	 */
	public void removeObjects(List<WorldObject> list)
	{
		for (WorldObject o : list)
		{
			_allObjects.remove(o);
		}
	}
	
	/**
	 * Removes the objects.
	 * @param objects the objects
	 */
	public void removeObjects(WorldObject[] objects)
	{
		for (WorldObject o : objects)
		{
			_allObjects.remove(o);
		}
	}
	
	/**
	 * Time remove object.
	 * @param object the object
	 * @return the long
	 */
	public long timeRemoveObject(WorldObject object)
	{
		long time = System.currentTimeMillis();
		_allObjects.remove(object);
		time -= System.currentTimeMillis();
		
		return time;
	}
	
	/**
	 * Return the WorldObject object that belongs to an ID or null if no object found.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Client packets : Action, AttackRequest, RequestJoinParty, RequestJoinPledge...</li><BR>
	 * @param oID Identifier of the WorldObject
	 * @return the l2 object
	 */
	public WorldObject findObject(int oID)
	{
		return _allObjects.get(oID);
	}
	
	/**
	 * Time find object.
	 * @param objectID the object id
	 * @return the long
	 */
	public long timeFindObject(int objectID)
	{
		long time = System.currentTimeMillis();
		_allObjects.get(objectID);
		time -= System.currentTimeMillis();
		
		return time;
	}
	
	/**
	 * Added by Tempy - 08 Aug 05 Allows easy retrevial of all visible objects in world. -- do not use that fucntion, its unsafe!
	 * @return the all visible objects
	 */
	public final L2ObjectMap<WorldObject> getAllVisibleObjects()
	{
		return _allObjects;
	}
	
	/**
	 * Get the count of all visible objects in world.<br>
	 * <br>
	 * @return count off all World objects
	 */
	public final int getAllVisibleObjectsCount()
	{
		return _allObjects.size();
	}
	
	/**
	 * Return a table containing all GMs.<BR>
	 * <BR>
	 * @return the all g ms
	 */
	public List<PlayerInstance> getAllGMs()
	{
		return GmListTable.getInstance().getAllGms(true);
	}
	
	/**
	 * Return a collection containing all players in game.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Read-only, please! </B></FONT><BR>
	 * <BR>
	 * @return the all players
	 */
	public Collection<PlayerInstance> getAllPlayers()
	{
		return _allPlayers.values();
	}
	
	/**
	 * Return how many players are online.<BR>
	 * <BR>
	 * @return number of online players.
	 */
	public static Integer getAllPlayersCount()
	{
		return _allPlayers.size();
	}
	
	/**
	 * Return the player instance corresponding to the given name.
	 * @param name Name of the player to get Instance
	 * @return the player
	 */
	public PlayerInstance getPlayer(String name)
	{
		return _allPlayers.get(name.toLowerCase());
	}
	
	/**
	 * Gets the player.
	 * @param playerObjId the player obj id
	 * @return the player
	 */
	public PlayerInstance getPlayer(int playerObjId)
	{
		for (PlayerInstance actual : _allPlayers.values())
		{
			if (actual.getObjectId() == playerObjId)
			{
				return actual;
			}
		}
		return null;
	}
	
	/**
	 * Return a collection containing all pets in game.<BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Read-only, please! </B></FONT><BR>
	 * <BR>
	 * @return the all pets
	 */
	public Collection<PetInstance> getAllPets()
	{
		return _petsInstance.values();
	}
	
	/**
	 * Return the pet instance from the given ownerId.<BR>
	 * <BR>
	 * @param ownerId ID of the owner
	 * @return the pet
	 */
	public PetInstance getPet(int ownerId)
	{
		return _petsInstance.get(ownerId);
	}
	
	/**
	 * Add the given pet instance from the given ownerId.<BR>
	 * <BR>
	 * @param ownerId ID of the owner
	 * @param pet PetInstance of the pet
	 * @return the l2 pet instance
	 */
	public PetInstance addPet(int ownerId, PetInstance pet)
	{
		return _petsInstance.put(ownerId, pet);
	}
	
	/**
	 * Remove the given pet instance.<BR>
	 * <BR>
	 * @param ownerId ID of the owner
	 */
	public void removePet(int ownerId)
	{
		_petsInstance.remove(ownerId);
	}
	
	/**
	 * Remove the given pet instance.<BR>
	 * <BR>
	 * @param pet the pet to remove
	 */
	public void removePet(PetInstance pet)
	{
		_petsInstance.values().remove(pet);
	}
	
	/**
	 * Add a WorldObject in the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * WorldObject (including PlayerInstance) are identified in <B>_visibleObjects</B> of his current WorldRegion and in <B>_knownObjects</B> of other surrounding Creatures <BR>
	 * PlayerInstance are identified in <B>_allPlayers</B> of World, in <B>_allPlayers</B> of his current WorldRegion and in <B>_knownPlayer</B> of other surrounding Creatures <BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Add the WorldObject object in _allPlayers* of World</li>
	 * <li>Add the WorldObject object in _gmList** of GmListTable</li>
	 * <li>Add object in _knownObjects and _knownPlayer* of all surrounding WorldRegion Creatures</li> <BR>
	 * <li>If object is a Creature, add all surrounding WorldObject in its _knownObjects and all surrounding PlayerInstance in its _knownPlayer</li><BR>
	 * <I>* only if object is a PlayerInstance</I><BR>
	 * <I>** only if object is a GM PlayerInstance</I><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object in _visibleObjects and _allPlayers* of WorldRegion (need synchronisation)</B></FONT><BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _allObjects and _allPlayers* of World (need synchronisation)</B></FONT><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Drop an Item</li>
	 * <li>Spawn a Creature</li>
	 * <li>Apply Death Penalty of a PlayerInstance</li><BR>
	 * <BR>
	 * @param object L2object to add in the world
	 * @param newRegion the new region
	 * @param dropper Creature who has dropped the object (if necessary)
	 */
	public void addVisibleObject(WorldObject object, WorldRegion newRegion, Creature dropper)
	{
		if (object instanceof PlayerInstance)
		{
			PlayerCountManager.getInstance().incConnectedCount();
			
			PlayerInstance player = (PlayerInstance) object;
			PlayerInstance tmp = _allPlayers.get(player.getName().toLowerCase());
			if ((tmp != null) && (tmp != player)) // just kick the player previous instance
			{
				tmp.store(); // Store character and items
				tmp.logout();
				
				if (tmp.getClient() != null)
				{
					tmp.getClient().setPlayer(null); // prevent deleteMe from being called a second time on disconnection
				}
			}
			
			if (!newRegion.isActive())
			{
				return;
			}
			// Get all visible objects contained in the _visibleObjects of WorldRegions
			// in a circular area of 2000 units
			final List<WorldObject> visibles = getVisibleObjects(object, 2000);
			
			// tell the player about the surroundings
			// Go through the visible objects contained in the circular area
			for (WorldObject visible : visibles)
			{
				if (visible == null)
				{
					continue;
				}
				
				// Add the object in WorldObjectHashSet(WorldObject) _knownObjects of the visible Creature according to conditions :
				// - Creature is visible
				// - object is not already known
				// - object is in the watch distance
				// If WorldObject is a PlayerInstance, add WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the visible Creature
				visible.getKnownList().addKnownObject(object);
				
				// Add the visible WorldObject in WorldObjectHashSet(WorldObject) _knownObjects of the object according to conditions
				// If visible WorldObject is a PlayerInstance, add visible WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the object
				object.getKnownList().addKnownObject(visible);
			}
			
			if (!player.isTeleporting())
			{
				// PlayerInstance tmp = _allPlayers.get(player.getName().toLowerCase());
				if (tmp != null)
				{
					LOGGER.warning("Teleporting: Duplicate character!? Closing both characters (" + player.getName() + ")");
					player.closeNetConnection();
					tmp.closeNetConnection();
					return;
				}
			}
			
			synchronized (_allPlayers)
			{
				_allPlayers.put(player.getName().toLowerCase(), player);
			}
		}
		
		// Get all visible objects contained in the _visibleObjects of WorldRegions in a circular area of 2000 units
		List<WorldObject> visibles = getVisibleObjects(object, 2000);
		
		// tell the player about the surroundings
		// Go through the visible objects contained in the circular area
		for (WorldObject visible : visibles)
		{
			// Add the object in WorldObjectHashSet(WorldObject) _knownObjects of the visible Creature according to conditions :
			// - Creature is visible
			// - object is not already known
			// - object is in the watch distance
			// If WorldObject is a PlayerInstance, add WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the visible Creature
			visible.getKnownList().addKnownObject(object, dropper);
			
			// Add the visible WorldObject in WorldObjectHashSet(WorldObject) _knownObjects of the object according to conditions
			// If visible WorldObject is a PlayerInstance, add visible WorldObject in WorldObjectHashSet(PlayerInstance) _knownPlayer of the object
			object.getKnownList().addKnownObject(visible, dropper);
		}
	}
	
	/**
	 * Add the PlayerInstance to _allPlayers of World.
	 * @param player the cha
	 */
	public void addToAllPlayers(PlayerInstance player)
	{
		_allPlayers.put(player.getName().toLowerCase(), player);
	}
	
	/**
	 * Remove the PlayerInstance from _allPlayers of World.<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Remove a player fom the visible objects</li><BR>
	 * @param player the cha
	 */
	public void removeFromAllPlayers(PlayerInstance player)
	{
		if ((player != null) && !player.isTeleporting())
		{
			_allPlayers.remove(player.getName().toLowerCase());
		}
	}
	
	/**
	 * Remove a WorldObject from the world.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * WorldObject (including PlayerInstance) are identified in <B>_visibleObjects</B> of his current WorldRegion and in <B>_knownObjects</B> of other surrounding Creatures <BR>
	 * PlayerInstance are identified in <B>_allPlayers</B> of World, in <B>_allPlayers</B> of his current WorldRegion and in <B>_knownPlayer</B> of other surrounding Creatures <BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Remove the WorldObject object from _allPlayers* of World</li>
	 * <li>Remove the WorldObject object from _visibleObjects and _allPlayers* of WorldRegion</li>
	 * <li>Remove the WorldObject object from _gmList** of GmListTable</li>
	 * <li>Remove object from _knownObjects and _knownPlayer* of all surrounding WorldRegion Creatures</li><BR>
	 * <li>If object is a Creature, remove all WorldObject from its _knownObjects and all PlayerInstance from its _knownPlayer</li><BR>
	 * <BR>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _allObjects of World</B></FONT><BR>
	 * <BR>
	 * <I>* only if object is a PlayerInstance</I><BR>
	 * <I>** only if object is a GM PlayerInstance</I><BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Pickup an Item</li>
	 * <li>Decay a Creature</li><BR>
	 * <BR>
	 * @param object L2object to remove from the world
	 * @param oldRegion the old region
	 */
	public void removeVisibleObject(WorldObject object, WorldRegion oldRegion)
	{
		if (object == null)
		{
			return;
		}
		
		if (oldRegion != null)
		{
			// Remove the object from the WorldObjectHashSet(WorldObject) _visibleObjects of WorldRegion
			// If object is a PlayerInstance, remove it from the WorldObjectHashSet(PlayerInstance) _allPlayers of this WorldRegion
			oldRegion.removeVisibleObject(object);
			
			// Go through all surrounding WorldRegion Creatures
			for (WorldRegion reg : oldRegion.getSurroundingRegions())
			{
				for (WorldObject obj : reg.getVisibleObjects())
				{
					// Remove the WorldObject from the WorldObjectHashSet(WorldObject) _knownObjects of the surrounding WorldRegion Creatures
					// If object is a PlayerInstance, remove the WorldObject from the WorldObjectHashSet(PlayerInstance) _knownPlayer of the surrounding WorldRegion Creatures
					// If object is targeted by one of the surrounding WorldRegion Creatures, cancel ATTACK and cast
					if ((obj != null) && (obj.getKnownList() != null))
					{
						obj.getKnownList().removeKnownObject(object);
					}
					
					// Remove surrounding WorldRegion Creatures from the WorldObjectHashSet(WorldObject) _KnownObjects of object
					// If surrounding WorldRegion Creatures is a PlayerInstance, remove it from the WorldObjectHashSet(PlayerInstance) _knownPlayer of object
					//
					if (object.getKnownList() != null)
					{
						object.getKnownList().removeKnownObject(obj);
					}
				}
			}
			
			// If object is a Creature :
			// Remove all WorldObject from WorldObjectHashSet(WorldObject) containing all WorldObject detected by the Creature
			// Remove all PlayerInstance from WorldObjectHashSet(PlayerInstance) containing all player ingame detected by the Creature
			object.getKnownList().removeAllKnownObjects();
			
			// If selected WorldObject is a NcIntance, remove it from WorldObjectHashSet(PlayerInstance) _allPlayers of World
			if (object instanceof PlayerInstance)
			{
				PlayerCountManager.getInstance().decConnectedCount();
				if (object.getActingPlayer().isInOfflineMode())
				{
					PlayerCountManager.getInstance().decOfflineTradeCount();
				}
				
				if (!((PlayerInstance) object).isTeleporting())
				{
					removeFromAllPlayers((PlayerInstance) object);
				}
			}
		}
	}
	
	/**
	 * Return all visible objects of the WorldRegion object's and of its surrounding WorldRegion.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>_surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Find Close Objects for Creature</li><BR>
	 * @param object L2object that determine the current WorldRegion
	 * @return the visible objects
	 */
	public List<WorldObject> getVisibleObjects(WorldObject object)
	{
		if (object == null)
		{
			return null;
		}
		
		final WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		// Create an FastList in order to contain all visible WorldObject
		final List<WorldObject> result = new ArrayList<>();
		
		// Create a FastList containing all regions around the current region
		List<WorldRegion> regions = reg.getSurroundingRegions();
		
		// Go through the FastList of region
		for (int i = 0; (regions != null) && (i < regions.size()); i++)
		{
			// Go through visible objects of the selected region
			L2ObjectSet<WorldObject> actual_objectSet = null;
			if (regions.get(i) != null)
			{
				actual_objectSet = regions.get(i).getVisibleObjects();
			}
			
			if ((actual_objectSet != null) && (actual_objectSet.size() > 0))
			{
				for (WorldObject _object : actual_objectSet)
				{
					if (_object == null)
					{
						continue;
					}
					
					if (_object.equals(object))
					{
						continue; // skip our own character
					}
					
					if (!_object.isVisible())
					{
						continue; // skip dying objects
					}
					
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the WorldRegions in the circular area (radius) centered on the object.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>_surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Define the aggrolist of monster</li>
	 * <li>Define visible objects of a WorldObject</li>
	 * <li>Skill : Confusion...</li> <BR>
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the circular area
	 * @return the visible objects
	 */
	public List<WorldObject> getVisibleObjects(WorldObject object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final WorldRegion region = object.getWorldRegion();
		
		if (region == null)
		{
			return new ArrayList<>();
		}
		
		final int x = object.getX();
		final int y = object.getY();
		final int sqRadius = radius * radius;
		
		// Create an FastList in order to contain all visible WorldObject
		final List<WorldObject> result = new ArrayList<>();
		
		// Create an FastList containing all regions around the current region
		List<WorldRegion> regions = region.getSurroundingRegions();
		
		// Go through the FastList of region
		for (int i = 0; (regions != null) && (i < regions.size()); i++)
		{
			// Go through visible objects of the selected region
			for (WorldObject _object : regions.get(i).getVisibleObjects())
			{
				if (_object == null)
				{
					continue;
				}
				
				if (_object.equals(object))
				{
					continue; // skip our own character
				}
				
				final int x1 = _object.getX();
				final int y1 = _object.getY();
				
				final double dx = x1 - x;
				final double dy = y1 - y;
				
				// If the visible object is inside the circular area add the object to the FastList result
				if (((dx * dx) + (dy * dy)) < sqRadius)
				{
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible objects of the WorldRegions in the spheric area (radius) centered on the object.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>_surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Define the target list of a skill</li>
	 * <li>Define the target list of a polearme attack</li><BR>
	 * <BR>
	 * @param object L2object that determine the center of the circular area
	 * @param radius Radius of the spheric area
	 * @return the visible objects3 d
	 */
	public List<WorldObject> getVisibleObjects3D(WorldObject object, int radius)
	{
		if ((object == null) || !object.isVisible())
		{
			return new ArrayList<>();
		}
		
		final int x = object.getX();
		final int y = object.getY();
		final int z = object.getZ();
		final int sqRadius = radius * radius;
		
		// Create an FastList in order to contain all visible WorldObject
		final List<WorldObject> result = new ArrayList<>();
		
		// Create an FastList containing all regions around the current region
		List<WorldRegion> regions = object.getWorldRegion().getSurroundingRegions();
		
		// Go through visible object of the selected region
		for (WorldRegion region : regions)
		{
			for (WorldObject _object : region.getVisibleObjects())
			{
				if (_object == null)
				{
					continue;
				}
				
				if (_object.equals(object))
				{
					continue; // skip our own character
				}
				
				final int x1 = _object.getX();
				final int y1 = _object.getY();
				final int z1 = _object.getZ();
				
				final long dx = x1 - x;
				final long dy = y1 - y;
				final long dz = z1 - z;
				
				if (((dx * dx) + (dy * dy) + (dz * dz)) < sqRadius)
				{
					result.add(_object);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Return all visible players of the WorldRegion object's and of its surrounding WorldRegion.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All visible object are identified in <B>_visibleObjects</B> of their current WorldRegion <BR>
	 * All surrounding WorldRegion are identified in <B>_surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Find Close Objects for Creature</li><BR>
	 * @param object L2object that determine the current WorldRegion
	 * @return the visible playable
	 */
	public List<Playable> getVisiblePlayable(WorldObject object)
	{
		WorldRegion reg = object.getWorldRegion();
		
		if (reg == null)
		{
			return null;
		}
		
		// Create an FastList in order to contain all visible WorldObject
		final List<Playable> result = new ArrayList<>();
		
		// Create a FastList containing all regions around the current region
		List<WorldRegion> regions = reg.getSurroundingRegions();
		
		// Go through the FastList of region
		for (WorldRegion region : regions)
		{
			// Create an Iterator to go through the visible WorldObject of the WorldRegion
			Iterator<Playable> playables = region.iterateAllPlayers();
			
			// Go through visible object of the selected region
			while (playables.hasNext())
			{
				Playable _object = playables.next();
				
				if (_object == null)
				{
					continue;
				}
				
				if (_object.equals(object))
				{
					continue; // skip our own character
				}
				
				if (!_object.isVisible())
				{
					continue; // skip dying objects
				}
				
				result.add(_object);
				
				_object = null;
			}
		}
		
		return result;
	}
	
	/**
	 * Calculate the current WorldRegions of the object according to its position (x,y).<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Set position of a new WorldObject (drop, spawn...)</li>
	 * <li>Update position of a WorldObject after a mouvement</li><BR>
	 * @param point the point
	 * @return the region
	 */
	public WorldRegion getRegion(Point3D point)
	{
		return _worldRegions[(point.getX() >> SHIFT_BY) + OFFSET_X][(point.getY() >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Gets the region.
	 * @param x the x
	 * @param y the y
	 * @return the region
	 */
	public WorldRegion getRegion(int x, int y)
	{
		return _worldRegions[(x >> SHIFT_BY) + OFFSET_X][(y >> SHIFT_BY) + OFFSET_Y];
	}
	
	/**
	 * Returns the whole 2d array containing the world regions used by ZoneData.java to setup zones inside the world regions
	 * @return the all world regions
	 */
	public WorldRegion[][] getAllWorldRegions()
	{
		return _worldRegions;
	}
	
	/**
	 * Check if the current WorldRegions of the object is valid according to its position (x,y).<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Init WorldRegions</li><BR>
	 * @param x X position of the object
	 * @param y Y position of the object
	 * @return True if the WorldRegion is valid
	 */
	private boolean validRegion(int x, int y)
	{
		return (x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y);
	}
	
	/**
	 * Init each WorldRegion and their surrounding table.<BR>
	 * <BR>
	 * <B><U> Concept</U> :</B><BR>
	 * <BR>
	 * All surrounding WorldRegion are identified in <B>_surroundingRegions</B> of the selected WorldRegion in order to scan a large area around a WorldObject<BR>
	 * <BR>
	 * <B><U> Example of use </U> :</B><BR>
	 * <BR>
	 * <li>Constructor of World</li><BR>
	 */
	private void initRegions()
	{
		LOGGER.info("World: Setting up World Regions");
		
		_worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j] = new WorldRegion(i, j);
			}
		}
		
		for (int x = 0; x <= REGIONS_X; x++)
		{
			for (int y = 0; y <= REGIONS_Y; y++)
			{
				for (int a = -1; a <= 1; a++)
				{
					for (int b = -1; b <= 1; b++)
					{
						if (validRegion(x + a, y + b))
						{
							_worldRegions[x + a][y + b].addSurroundingRegion(_worldRegions[x][y]);
						}
					}
				}
			}
		}
		LOGGER.info("World: (" + REGIONS_X + "x" + REGIONS_Y + ") World Region Grid set up.");
	}
	
	/**
	 * Deleted all spawns in the world.
	 */
	public synchronized void deleteVisibleNpcSpawns()
	{
		LOGGER.info("Deleting all visible NPCs.");
		
		for (int i = 0; i <= REGIONS_X; i++)
		{
			for (int j = 0; j <= REGIONS_Y; j++)
			{
				_worldRegions[i][j].deleteVisibleNpcSpawns();
			}
		}
		LOGGER.info("All visible NPCs deleted.");
	}
	
	/**
	 * Gets the account players.
	 * @param account_name the account_name
	 * @return the account players
	 */
	public List<PlayerInstance> getAccountPlayers(String account_name)
	{
		final List<PlayerInstance> players_for_account = new ArrayList<>();
		for (PlayerInstance actual : _allPlayers.values())
		{
			if (actual.getAccountName().equals(account_name))
			{
				players_for_account.add(actual);
			}
		}
		return players_for_account;
	}
}
