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
package org.l2jmobius.gameserver.instancemanager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.RaidBoss;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.taskmanager.GameTimeTaskManager;

/**
 * @author godson
 */
public class DayNightSpawnManager
{
	private static final Logger LOGGER = Logger.getLogger(DayNightSpawnManager.class.getName());
	
	private static final int EILHALDER_VON_HELLMAN = 25328;
	
	private final List<Spawn> _dayCreatures = new ArrayList<>();
	private final List<Spawn> _nightCreatures = new ArrayList<>();
	private final Map<Spawn, RaidBoss> _bosses = new ConcurrentHashMap<>();
	
	protected DayNightSpawnManager()
	{
		// Prevent external initialization.
	}
	
	public void addDayCreature(Spawn spawnDat)
	{
		_dayCreatures.add(spawnDat);
	}
	
	public void addNightCreature(Spawn spawnDat)
	{
		_nightCreatures.add(spawnDat);
	}
	
	/**
	 * Spawn Day Creatures, and Unspawn Night Creatures
	 */
	public void spawnDayCreatures()
	{
		spawnCreatures(_nightCreatures, _dayCreatures, "night", "day");
	}
	
	/**
	 * Spawn Night Creatures, and Unspawn Day Creatures
	 */
	public void spawnNightCreatures()
	{
		spawnCreatures(_dayCreatures, _nightCreatures, "day", "night");
	}
	
	/**
	 * Manage Spawn/Respawn
	 * @param unSpawnCreatures List with spawns must be unspawned
	 * @param spawnCreatures List with spawns must be spawned
	 * @param unspawnLogInfo String for log info for unspawned Npc
	 * @param spawnLogInfo String for log info for spawned Npc
	 */
	private void spawnCreatures(List<Spawn> unSpawnCreatures, List<Spawn> spawnCreatures, String unspawnLogInfo, String spawnLogInfo)
	{
		try
		{
			if (!unSpawnCreatures.isEmpty())
			{
				int i = 0;
				for (Spawn spawn : unSpawnCreatures)
				{
					if (spawn == null)
					{
						continue;
					}
					
					spawn.stopRespawn();
					final Npc last = spawn.getLastSpawn();
					if (last != null)
					{
						last.deleteMe();
						i++;
					}
				}
				LOGGER.info("DayNightSpawnManager: Removed " + i + " " + unspawnLogInfo + " creatures");
			}
			
			int i = 0;
			for (Spawn spawnDat : spawnCreatures)
			{
				if (spawnDat == null)
				{
					continue;
				}
				spawnDat.startRespawn();
				spawnDat.doSpawn();
				i++;
			}
			
			LOGGER.info("DayNightSpawnManager: Spawned " + i + " " + spawnLogInfo + " creatures");
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while spawning creatures: " + e.getMessage(), e);
		}
	}
	
	private void changeMode(int mode)
	{
		if (_nightCreatures.isEmpty() && _dayCreatures.isEmpty() && _bosses.isEmpty())
		{
			return;
		}
		
		switch (mode)
		{
			case 0:
			{
				spawnDayCreatures();
				specialNightBoss(0);
				ShadowSenseMsg(0);
				break;
			}
			case 1:
			{
				spawnNightCreatures();
				specialNightBoss(1);
				ShadowSenseMsg(1);
				break;
			}
			default:
			{
				LOGGER.warning("DayNightSpawnManager: Wrong mode sent");
				break;
			}
		}
	}
	
	public DayNightSpawnManager trim()
	{
		((ArrayList<?>) _nightCreatures).trimToSize();
		((ArrayList<?>) _dayCreatures).trimToSize();
		return this;
	}
	
	public void notifyChangeMode()
	{
		try
		{
			changeMode(GameTimeTaskManager.getInstance().isNight() ? 1 : 0);
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while notifyChangeMode(): " + e.getMessage(), e);
		}
	}
	
	public void cleanUp()
	{
		_nightCreatures.clear();
		_dayCreatures.clear();
		_bosses.clear();
	}
	
	private void specialNightBoss(int mode)
	{
		try
		{
			RaidBoss boss;
			
			if (_bosses.isEmpty() && (mode == 1))
			{
				final Spawn nightBossSpawn = getNightBossSpawn();
				boss = handleBoss(nightBossSpawn);
				_bosses.put(nightBossSpawn, boss);
				handleHellman(boss, mode);
				return;
			}
			
			for (Entry<Spawn, RaidBoss> entry : _bosses.entrySet())
			{
				boss = entry.getValue();
				if ((boss == null) && (mode == 1))
				{
					final Spawn spawn = entry.getKey();
					boss = (RaidBoss) spawn.doSpawn();
					RaidBossSpawnManager.getInstance().notifySpawnNightBoss(boss);
					_bosses.put(spawn, boss);
					continue;
				}
				
				if ((boss == null) && (mode == 0))
				{
					continue;
				}
				
				if ((boss != null) && (boss.getNpcId() == EILHALDER_VON_HELLMAN) && (boss.getRaidStatus() == RaidBossStatus.ALIVE))
				{
					handleHellman(boss, mode);
				}
				return;
			}
		}
		catch (Exception e)
		{
			LOGGER.log(Level.WARNING, "Error while specialNightBoss(): " + e.getMessage(), e);
		}
	}
	
	private void handleHellman(RaidBoss boss, int mode)
	{
		switch (mode)
		{
			case 0:
			{
				boss.deleteMe();
				LOGGER.info(getClass().getSimpleName() + ": Deleting Hellman raidboss");
				break;
			}
			case 1:
			{
				if (!boss.isSpawned())
				{
					boss.spawnMe();
				}
				LOGGER.warning(getClass().getSimpleName() + ": Spawning Hellman raidboss");
				break;
			}
		}
	}
	
	private void ShadowSenseMsg(int mode)
	{
		final Skill skill = SkillTable.getInstance().getSkill(294, 1);
		if (skill == null)
		{
			return;
		}
		
		final SystemMessageId msg = (mode == 1 ? SystemMessageId.IT_IS_NOW_MIDNIGHT_AND_THE_EFFECT_OF_S1_CAN_BE_FELT : SystemMessageId.IT_IS_DAWN_AND_THE_EFFECT_OF_S1_WILL_NOW_DISAPPEAR);
		final Collection<Player> pls = World.getInstance().getAllPlayers();
		for (Player onlinePlayer : pls)
		{
			if ((onlinePlayer.getRace().ordinal() == 2) && (onlinePlayer.getSkillLevel(294) > 0))
			{
				final SystemMessage sm = SystemMessage.getSystemMessage(msg);
				sm.addSkillName(294);
				onlinePlayer.sendPacket(sm);
			}
		}
	}
	
	public RaidBoss handleBoss(Spawn spawnDat)
	{
		if (_bosses.containsKey(spawnDat))
		{
			return _bosses.get(spawnDat);
		}
		
		if (GameTimeTaskManager.getInstance().isNight())
		{
			final RaidBoss raidboss = (RaidBoss) spawnDat.doSpawn();
			_bosses.put(spawnDat, raidboss);
			
			return raidboss;
		}
		return null;
	}
	
	public Spawn getNightBossSpawn()
	{
		Spawn spawnDat = null;
		
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rs = s.executeQuery("SELECT * FROM raidboss_spawnlist WHERE boss_id=" + EILHALDER_VON_HELLMAN))
		{
			if (rs.next())
			{
				spawnDat = new Spawn(NpcTable.getInstance().getTemplate(EILHALDER_VON_HELLMAN));
				spawnDat.setX(rs.getInt("loc_x"));
				spawnDat.setY(rs.getInt("loc_y"));
				spawnDat.setZ(rs.getInt("loc_z"));
				spawnDat.setAmount(rs.getInt("amount"));
				spawnDat.setHeading(rs.getInt("heading"));
				spawnDat.setRespawnMinDelay(rs.getInt("respawn_min_delay"));
				spawnDat.setRespawnMaxDelay(rs.getInt("respawn_max_delay"));
				return spawnDat;
			}
		}
		catch (Exception e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": Could not load Eilhalder Von Hellman spawn.");
		}
		
		return spawnDat;
	}
	
	public static DayNightSpawnManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final DayNightSpawnManager INSTANCE = new DayNightSpawnManager();
	}
}
