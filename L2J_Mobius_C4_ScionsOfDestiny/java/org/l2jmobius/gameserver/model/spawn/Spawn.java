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
package org.l2jmobius.gameserver.model.spawn;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.TerritoryTable;
import org.l2jmobius.gameserver.data.xml.WalkerRouteData;
import org.l2jmobius.gameserver.data.xml.ZoneData;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.instancemanager.IdManager;
import org.l2jmobius.gameserver.model.WorldObject;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.quest.EventType;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.zone.type.WaterZone;
import org.l2jmobius.gameserver.taskmanager.RespawnTaskManager;
import org.l2jmobius.gameserver.util.Util;

/**
 * This class manages the spawn and respawn of a group of Npcs that are in the same are and have the same type. <b><u>Concept</u>:</b><br>
 * <br>
 * Npc can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position. The heading of the Npc can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<br>
 * <br>
 * @author Nightmare
 * @version $Revision: 1.9.2.3.2.8 $ $Date: 2005/03/27 15:29:32 $
 */
public class Spawn
{
	protected static final Logger LOGGER = Logger.getLogger(Spawn.class.getName());
	
	private NpcTemplate _template;
	private int _id;
	private int _location;
	private int _maximumCount;
	private int _currentCount;
	public int _scheduledCount;
	private int _locX;
	private int _locY;
	private int _locZ;
	private int _heading;
	private int _respawnDelay;
	private int _respawnMinDelay;
	private int _respawnMaxDelay;
	private Constructor<?> _constructor;
	private boolean _doRespawn;
	private int _instanceId = 0;
	private Npc _lastSpawn;
	private static List<SpawnListener> _spawnListeners = new ArrayList<>();
	
	/**
	 * Constructor of Spawn.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Each Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...). All of those properties are stored in a different NpcTemplate for each type of Spawn. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of Spawn is created,
	 * server just create a link between the instance and the template. This link is stored in <b>_template</b><br>
	 * Each Npc is linked to a Spawn that manages its spawn and respawn (delay, location...). This link is stored in <b>_spawn</b> of the Npc<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Set the _template of the Spawn</li>
	 * <li>Calculate the implementationName used to generate the generic constructor of Npc managed by this Spawn</li>
	 * <li>Create the generic constructor of Npc managed by this Spawn</li><br>
	 * @param mobTemplate The NpcTemplate to link to this Spawn
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 */
	public Spawn(NpcTemplate mobTemplate) throws ClassNotFoundException, NoSuchMethodException
	{
		// Set the _template of the Spawn
		_template = mobTemplate;
		if (_template == null)
		{
			return;
		}
		
		// The Name of the Npc type managed by this Spawn
		String implementationName = _template.getType(); // implementing class name
		if (mobTemplate.getNpcId() == 30995)
		{
			implementationName = "RaceManager";
		}
		
		if ((mobTemplate.getNpcId() >= 31046) && (mobTemplate.getNpcId() <= 31053))
		{
			implementationName = "SymbolMaker";
		}
		
		// Create the generic constructor of Npc managed by this Spawn
		final Class<?>[] parameters =
		{
			int.class,
			NpcTemplate.class
		};
		_constructor = Class.forName("org.l2jmobius.gameserver.model.actor.instance." + implementationName).getConstructor(parameters);
	}
	
	/**
	 * @return the maximum number of Npc that this Spawn can manage.
	 */
	public int getAmount()
	{
		return _maximumCount;
	}
	
	/**
	 * @return the Identifier of this L2spawn (used as key in the SpawnTable).
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * @return the Identifier of the location area where Npc can be spawned.
	 */
	public int getLocation()
	{
		return _location;
	}
	
	/**
	 * @return the X position of the spawn point.
	 */
	public int getX()
	{
		return _locX;
	}
	
	/**
	 * @return the Y position of the spawn point.
	 */
	public int getY()
	{
		return _locY;
	}
	
	/**
	 * @return the Z position of the spawn point.
	 */
	public int getZ()
	{
		return _locZ;
	}
	
	/**
	 * @return the Identifier of the Npc manage by this L2spawn contained in the NpcTemplate.
	 */
	public int getNpcId()
	{
		if (_template == null)
		{
			return -1;
		}
		return _template.getNpcId();
	}
	
	/**
	 * @return the heading of Npc when they are spawned.
	 */
	public int getHeading()
	{
		return _heading;
	}
	
	/**
	 * @return the delay between a Npc remove and its re-spawn.
	 */
	public int getRespawnDelay()
	{
		return _respawnDelay;
	}
	
	/**
	 * @return Min RaidBoss Spawn delay.
	 */
	public int getRespawnMinDelay()
	{
		return _respawnMinDelay;
	}
	
	/**
	 * @return Max RaidBoss Spawn delay.
	 */
	public int getRespawnMaxDelay()
	{
		return _respawnMaxDelay;
	}
	
	/**
	 * Set the maximum number of Npc that this Spawn can manage.
	 * @param amount
	 */
	public void setAmount(int amount)
	{
		_maximumCount = amount;
	}
	
	/**
	 * Set the Identifier of this L2spawn (used as key in the SpawnTable).
	 * @param id
	 */
	public void setId(int id)
	{
		_id = id;
	}
	
	/**
	 * Set the Identifier of the location area where Npc can be spawned.
	 * @param location
	 */
	public void setLocation(int location)
	{
		_location = location;
	}
	
	/**
	 * Set Minimum Respawn Delay.
	 * @param date
	 */
	public void setRespawnMinDelay(int date)
	{
		_respawnMinDelay = date;
	}
	
	/**
	 * Set Maximum Respawn Delay.
	 * @param date
	 */
	public void setRespawnMaxDelay(int date)
	{
		_respawnMaxDelay = date;
	}
	
	/**
	 * Set the X position of the spawn point.
	 * @param locx
	 */
	public void setX(int locx)
	{
		_locX = locx;
	}
	
	/**
	 * Set the Y position of the spawn point.
	 * @param locy
	 */
	public void setY(int locy)
	{
		_locY = locy;
	}
	
	/**
	 * Set the Z position of the spawn point.
	 * @param locz
	 */
	public void setZ(int locz)
	{
		_locZ = locz;
	}
	
	/**
	 * Set the heading of Npc when they are spawned.
	 * @param heading
	 */
	public void setHeading(int heading)
	{
		_heading = heading;
	}
	
	public void setLoc(int locx, int locy, int locz, int heading)
	{
		_locX = locx;
		_locY = locy;
		_locZ = locz;
		_heading = heading;
	}
	
	/**
	 * Decrease the current number of Npc of this Spawn and if necessary create a SpawnTask to launch after the respawn Delay.<br>
	 * <br>
	 * <b><u>Actions</u>:</b><br>
	 * <li>Decrease the current number of Npc of this Spawn</li>
	 * <li>Check if respawn is possible to prevent multiple respawning caused by lag</li>
	 * <li>Update the current number of SpawnTask in progress or stand by of this Spawn</li>
	 * <li>Create a new SpawnTask to launch after the respawn Delay</li><br>
	 * <font color=#FF0000><b><u>Caution</u>: A respawn is possible ONLY if _doRespawn=True and _scheduledCount + _currentCount < _maximumCount</b></font>
	 * @param oldNpc
	 */
	public void decreaseCount(Npc oldNpc)
	{
		// Decrease the current number of Npc of this Spawn
		_currentCount--;
		
		// Check if respawn is possible to prevent multiple respawning caused by lag
		if (_doRespawn && ((_scheduledCount + _currentCount) < _maximumCount))
		{
			// Update the current number of SpawnTask in progress or stand by of this Spawn
			_scheduledCount++;
			
			// Schedule the next respawn.
			RespawnTaskManager.getInstance().add(oldNpc, Chronos.currentTimeMillis() + _respawnDelay);
		}
	}
	
	/**
	 * Create the initial spawning and set _doRespawn to True.
	 * @return The number of Npc that were spawned
	 */
	public int init()
	{
		while (_currentCount < _maximumCount)
		{
			doSpawn();
		}
		_doRespawn = true;
		return _currentCount;
	}
	
	/**
	 * Set _doRespawn to False to stop respawn in this Spawn.
	 */
	public void stopRespawn()
	{
		_doRespawn = false;
	}
	
	/**
	 * Set _doRespawn to True to start or restart respawn in this Spawn.
	 */
	public void startRespawn()
	{
		_doRespawn = true;
	}
	
	/**
	 * Create the Npc, add it to the world and launch its OnSpawn action.<br>
	 * <br>
	 * <b><u>Concept</u>:</b><br>
	 * <br>
	 * Npc can be spawned either in a random position into a location area (if Lox=0 and Locy=0), either at an exact position. The heading of the Npc can be a random heading if not defined (value= -1) or an exact heading (ex : merchant...).<br>
	 * <br>
	 * <b><u>Actions for an random spawn into location area</u> : <i>(if Locx=0 and Locy=0)</i></b><br>
	 * <li>Get Npc Init parameters and its generate an Identifier</li>
	 * <li>Call the constructor of the Npc</li>
	 * <li>Calculate the random position in the location area (if Locx=0 and Locy=0) or get its exact position from the Spawn</li>
	 * <li>Set the position of the Npc</li>
	 * <li>Set the HP and MP of the Npc to the max</li>
	 * <li>Set the heading of the Npc (random heading if not defined : value=-1)</li>
	 * <li>Link the Npc to this Spawn</li>
	 * <li>Init other values of the Npc (ex : from its CreatureTemplate for INT, STR, DEX...) and add it in the world</li>
	 * <li>Lauch the action OnSpawn fo the Npc</li>
	 * <li>Increase the current number of Npc managed by this Spawn</li><br>
	 * @return
	 */
	public Npc doSpawn()
	{
		Npc npc = null;
		try
		{
			// Check if the Spawn is not a Net or Minion spawn
			if (_template.getType().equalsIgnoreCase("Pet") || _template.getType().equalsIgnoreCase("Minion"))
			{
				_currentCount++;
				return npc;
			}
			
			// Get Npc Init parameters and its generate an Identifier
			final Object[] parameters =
			{
				IdManager.getInstance().getNextId(),
				_template
			};
			
			// Call the constructor of the Npc
			// (can be a Artefact, FriendlyMob, Guard, Monster, SiegeGuard, FeedableBeast, TamedBeast, Folk)
			final Object tmp = _constructor.newInstance(parameters);
			
			// Must be done before object is spawned into visible world
			((WorldObject) tmp).setInstanceId(_instanceId);
			
			// Check if the Instance is a Npc
			if (!(tmp instanceof Npc))
			{
				return npc;
			}
			
			npc = (Npc) tmp;
			return initializeNpc(npc);
		}
		catch (Exception e)
		{
			LOGGER.warning("NPC " + _template.getNpcId() + " class not found " + e);
		}
		return npc;
	}
	
	private Npc initializeNpc(Npc npc)
	{
		int newlocx;
		int newlocy;
		int newlocz;
		
		// If Locx and Locy are not defined, the Npc must be spawned in an area defined by location.
		if ((_locX == 0) && (_locY == 0))
		{
			if (_location == 0)
			{
				return npc;
			}
			
			// Calculate the random position in the location area
			final int[] p = TerritoryTable.getInstance().getRandomPoint(getLocation());
			
			// Set the calculated position of the Npc
			newlocx = p[0];
			newlocy = p[1];
			newlocz = p[3];
		}
		else
		{
			// The Npc is spawned at the exact position (Lox, Locy, Locz)
			newlocx = _locX;
			newlocy = _locY;
			newlocz = _locZ;
		}
		
		// Check if npc is in water.
		final WaterZone water = ZoneData.getInstance().getZone(newlocx, newlocy, newlocz, WaterZone.class);
		
		// If random spawn system is enabled.
		if (Config.ENABLE_RANDOM_MONSTER_SPAWNS && npc.isMonster() && !npc.isQuestMonster() && (WalkerRouteData.getInstance().getRouteForNpc(npc.getNpcId()) == null) && (getInstanceId() == 0) && !npc.isRaid() && !npc.isMinion() && !npc.isFlying() && (water == null) && !Config.MOBS_LIST_NOT_RANDOM.contains(npc.getNpcId()))
		{
			final int randX = newlocx + Rnd.get(Config.MOB_MIN_SPAWN_RANGE, Config.MOB_MAX_SPAWN_RANGE);
			final int randY = newlocy + Rnd.get(Config.MOB_MIN_SPAWN_RANGE, Config.MOB_MAX_SPAWN_RANGE);
			if (GeoEngine.getInstance().canMoveToTarget(newlocx, newlocy, newlocz, randX, randY, newlocz, getInstanceId()))
			{
				newlocx = randX;
				newlocy = randY;
			}
		}
		
		// Correct Z of monsters.
		if (npc.isMonster() && !npc.isFlying() && (water == null))
		{
			// Do not correct Z distances greater than 300.
			final int geoZ = GeoEngine.getInstance().getHeight(newlocx, newlocy, newlocz);
			if (Util.calculateDistance(newlocx, newlocy, newlocz, newlocx, newlocy, geoZ, true) < 300)
			{
				newlocz = geoZ;
			}
		}
		
		npc.stopAllEffects();
		
		// Set the HP and MP of the Npc to the max
		npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
		
		// Clear script value.
		npc.setScriptValue(0);
		
		// Set the heading of the Npc (random heading if not defined)
		if (_heading == -1)
		{
			npc.setHeading(Rnd.get(61794));
		}
		else
		{
			npc.setHeading(_heading);
		}
		
		// Reset decay info
		npc.setDecayed(false);
		
		// Link the Npc to this Spawn
		npc.setSpawn(this);
		
		// Init other values of the Npc (ex : from its CreatureTemplate for INT, STR, DEX...) and add it in the world as a visible object
		npc.spawnMe(newlocx, newlocy, newlocz);
		notifyNpcSpawned(npc);
		
		_lastSpawn = npc;
		for (Quest quest : npc.getTemplate().getEventQuests(EventType.ON_SPAWN))
		{
			quest.notifySpawn(npc);
		}
		
		// Increase the current number of Npc managed by this Spawn
		_currentCount++;
		
		return npc;
	}
	
	public static void addSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.add(listener);
		}
	}
	
	public static void removeSpawnListener(SpawnListener listener)
	{
		synchronized (_spawnListeners)
		{
			_spawnListeners.remove(listener);
		}
	}
	
	public static void notifyNpcSpawned(Npc npc)
	{
		synchronized (_spawnListeners)
		{
			for (SpawnListener listener : _spawnListeners)
			{
				listener.npcSpawned(npc);
			}
		}
	}
	
	/**
	 * @param value delay in seconds
	 */
	public void setRespawnDelay(int value)
	{
		if (value < 0)
		{
			LOGGER.warning("respawn delay is negative for spawnId:" + _id);
		}
		
		_respawnDelay = value < 10 ? 10000 : value * 1000;
	}
	
	public Npc getLastSpawn()
	{
		return _lastSpawn;
	}
	
	public void respawnNpc(Npc oldNpc)
	{
		if (_doRespawn)
		{
			// oldNpc.refreshId();
			initializeNpc(oldNpc);
		}
	}
	
	public NpcTemplate getTemplate()
	{
		return _template;
	}
	
	public int getInstanceId()
	{
		return _instanceId;
	}
	
	public void setInstanceId(int instanceId)
	{
		_instanceId = instanceId;
	}
}
