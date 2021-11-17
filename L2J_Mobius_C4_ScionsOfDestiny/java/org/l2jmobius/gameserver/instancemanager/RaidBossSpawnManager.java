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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.database.DatabaseFactory;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.data.sql.AnnouncementsTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.data.xml.AdminData;
import org.l2jmobius.gameserver.enums.RaidBossStatus;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.instance.RaidBoss;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.skills.Stat;
import org.l2jmobius.gameserver.model.spawn.Spawn;

/**
 * Raid Boss spawn manager.
 * @author godson
 */
public class RaidBossSpawnManager
{
	private static final Logger LOGGER = Logger.getLogger(RaidBossSpawnManager.class.getName());
	
	protected static final Map<Integer, RaidBoss> _bosses = new ConcurrentHashMap<>();
	protected static final Map<Integer, Spawn> _spawns = new ConcurrentHashMap<>();
	protected static final Map<Integer, StatSet> _storedInfo = new ConcurrentHashMap<>();
	protected static final Map<Integer, ScheduledFuture<?>> _schedules = new ConcurrentHashMap<>();
	
	/**
	 * Instantiates a new raid boss spawn manager.
	 */
	protected RaidBossSpawnManager()
	{
		load();
	}
	
	/**
	 * Load.
	 */
	public void load()
	{
		_bosses.clear();
		_spawns.clear();
		_storedInfo.clear();
		_schedules.clear();
		
		try (Connection con = DatabaseFactory.getConnection();
			Statement s = con.createStatement();
			ResultSet rset = s.executeQuery("SELECT * FROM raidboss_spawnlist ORDER BY boss_id"))
		{
			while (rset.next())
			{
				final NpcTemplate template = getValidTemplate(rset.getInt("boss_id"));
				if (template != null)
				{
					final Spawn spawnDat = new Spawn(template);
					spawnDat.setX(rset.getInt("loc_x"));
					spawnDat.setY(rset.getInt("loc_y"));
					spawnDat.setZ(rset.getInt("loc_z"));
					spawnDat.setAmount(rset.getInt("amount"));
					spawnDat.setHeading(rset.getInt("heading"));
					spawnDat.setRespawnMinDelay(rset.getInt("respawn_min_delay"));
					spawnDat.setRespawnMaxDelay(rset.getInt("respawn_max_delay"));
					
					addNewSpawn(spawnDat, rset.getLong("respawn_time"), rset.getDouble("currentHP"), rset.getDouble("currentMP"), false);
				}
				else
				{
					LOGGER.warning("RaidBossSpawnManager: Could not load raidboss #" + rset.getInt("boss_id") + " from DB");
				}
			}
			
			LOGGER.info("RaidBossSpawnManager: Loaded " + _bosses.size() + " instances.");
			LOGGER.info("RaidBossSpawnManager: Scheduled " + _schedules.size() + " instances.");
		}
		catch (SQLException e)
		{
			LOGGER.warning("RaidBossSpawnManager: Couldnt load raidboss_spawnlist table");
		}
		catch (Exception e)
		{
			LOGGER.warning("RaidBossSpawnManager: Error while initializing RaidBossSpawnManager: " + e.getMessage());
		}
	}
	
	private class SpawnSchedule implements Runnable
	{
		private final int bossId;
		
		/**
		 * Instantiates a new spawn schedule.
		 * @param npcId the npc id
		 */
		public SpawnSchedule(int npcId)
		{
			bossId = npcId;
		}
		
		@Override
		public void run()
		{
			RaidBoss raidboss = null;
			if (bossId == 25328)
			{
				raidboss = DayNightSpawnManager.getInstance().handleBoss(_spawns.get(bossId));
			}
			else
			{
				raidboss = (RaidBoss) _spawns.get(bossId).doSpawn();
			}
			
			if (raidboss != null)
			{
				raidboss.setRaidStatus(RaidBossStatus.ALIVE);
				
				final StatSet info = new StatSet();
				info.set("currentHP", raidboss.getCurrentHp());
				info.set("currentMP", raidboss.getCurrentMp());
				info.set("respawnTime", 0);
				_storedInfo.put(bossId, info);
				
				AdminData.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName() + ".");
				if (Config.ANNOUNCE_TO_ALL_SPAWN_RB)
				{
					AnnouncementsTable.getInstance().announceToAll("Raid boss " + raidboss.getName() + " spawned in world.");
				}
				_bosses.put(bossId, raidboss);
			}
			
			_schedules.remove(bossId);
			
			// To update immediately the database, used for website to show up RaidBoss status.
			if (Config.SAVE_RAIDBOSS_STATUS_INTO_DB)
			{
				updateDb();
			}
		}
	}
	
	/**
	 * Update status.
	 * @param boss the boss
	 * @param isBossDead the is boss dead
	 */
	public void updateStatus(RaidBoss boss, boolean isBossDead)
	{
		final StatSet info = _storedInfo.get(boss.getNpcId());
		if (info == null)
		{
			return;
		}
		
		if (isBossDead)
		{
			boss.setRaidStatus(RaidBossStatus.DEAD);
			
			final int RespawnMinDelay = boss.getSpawn().getRespawnMinDelay();
			final int RespawnMaxDelay = boss.getSpawn().getRespawnMaxDelay();
			final long respawnDelay = Rnd.get((int) (RespawnMinDelay * 1000 * Config.RAID_MIN_RESPAWN_MULTIPLIER), (int) (RespawnMaxDelay * 1000 * Config.RAID_MAX_RESPAWN_MULTIPLIER));
			final long respawnTime = Calendar.getInstance().getTimeInMillis() + respawnDelay;
			info.set("currentHP", boss.getMaxHp());
			info.set("currentMP", boss.getMaxMp());
			info.set("respawnTime", respawnTime);
			_schedules.put(boss.getNpcId(), ThreadPool.schedule(new SpawnSchedule(boss.getNpcId()), respawnDelay));
			
			// To update immediately the database, used for website to show up RaidBoss status.
			if (Config.SAVE_RAIDBOSS_STATUS_INTO_DB)
			{
				updateDb();
			}
		}
		else
		{
			boss.setRaidStatus(RaidBossStatus.ALIVE);
			
			info.set("currentHP", boss.getCurrentHp());
			info.set("currentMP", boss.getCurrentMp());
			info.set("respawnTime", 0);
		}
		_storedInfo.put(boss.getNpcId(), info);
	}
	
	/**
	 * Adds the new spawn.
	 * @param spawnDat the spawn dat
	 * @param respawnTime the respawn time
	 * @param currentHP the current hp
	 * @param currentMP the current mp
	 * @param storeInDb the store in db
	 */
	public void addNewSpawn(Spawn spawnDat, long respawnTime, double currentHP, double currentMP, boolean storeInDb)
	{
		if ((spawnDat == null) || _spawns.containsKey(spawnDat.getId()))
		{
			return;
		}
		
		double hp = currentHP;
		final int bossId = spawnDat.getNpcId();
		final long time = Calendar.getInstance().getTimeInMillis();
		SpawnTable.getInstance().addNewSpawn(spawnDat, false);
		if ((respawnTime == 0) || (time > respawnTime))
		{
			final RaidBoss raidboss = bossId == 25328 ? DayNightSpawnManager.getInstance().handleBoss(spawnDat) : (RaidBoss) spawnDat.doSpawn();
			if (raidboss != null)
			{
				final double bonus = raidboss.getStat().calcStat(Stat.MAX_HP, 1, raidboss, null);
				
				// if new spawn, the currentHp is equal to maxHP/bonus, so set it to max
				if ((int) (bonus * hp) == raidboss.getMaxHp())
				{
					hp = (raidboss.getMaxHp());
				}
				
				raidboss.setCurrentHp(hp);
				raidboss.setCurrentMp(currentMP);
				raidboss.setRaidStatus(RaidBossStatus.ALIVE);
				
				_bosses.put(bossId, raidboss);
				
				final StatSet info = new StatSet();
				info.set("currentHP", hp);
				info.set("currentMP", currentMP);
				info.set("respawnTime", 0);
				_storedInfo.put(bossId, info);
			}
		}
		else
		{
			ScheduledFuture<?> futureSpawn;
			final long spawnTime = respawnTime - Calendar.getInstance().getTimeInMillis();
			futureSpawn = ThreadPool.schedule(new SpawnSchedule(bossId), spawnTime);
			_schedules.put(bossId, futureSpawn);
		}
		
		_spawns.put(bossId, spawnDat);
		
		if (storeInDb)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,amount,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) VALUES(?,?,?,?,?,?,?,?,?)"))
			{
				statement.setInt(1, spawnDat.getNpcId());
				statement.setInt(2, spawnDat.getAmount());
				statement.setInt(3, spawnDat.getX());
				statement.setInt(4, spawnDat.getY());
				statement.setInt(5, spawnDat.getZ());
				statement.setInt(6, spawnDat.getHeading());
				statement.setLong(7, respawnTime);
				statement.setDouble(8, hp);
				statement.setDouble(9, currentMP);
				statement.execute();
			}
			catch (Exception e)
			{
				// problem with storing spawn
				LOGGER.warning("RaidBossSpawnManager: Could not store raidboss #" + bossId + " in the DB:" + e);
			}
		}
	}
	
	/**
	 * Delete spawn.
	 * @param spawnDat the spawn dat
	 * @param updateDb the update db
	 */
	public void deleteSpawn(Spawn spawnDat, boolean updateDb)
	{
		if (spawnDat == null)
		{
			return;
		}
		
		final int bossId = spawnDat.getId();
		if (!_spawns.containsKey(bossId))
		{
			return;
		}
		
		SpawnTable.getInstance().deleteSpawn(spawnDat, false);
		_spawns.remove(bossId);
		
		if (_bosses.containsKey(bossId))
		{
			_bosses.remove(bossId);
		}
		
		if (_schedules.containsKey(bossId))
		{
			_schedules.remove(bossId).cancel(true);
		}
		
		if (_storedInfo.containsKey(bossId))
		{
			_storedInfo.remove(bossId);
		}
		
		if (updateDb)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement ps = con.prepareStatement("DELETE FROM raidboss_spawnlist WHERE boss_id=?"))
			{
				ps.setInt(1, bossId);
				ps.execute();
			}
			catch (Exception e)
			{
				// problem with deleting spawn
				LOGGER.warning("RaidBossSpawnManager: Could not remove raidboss #" + bossId + " from DB: " + e);
			}
		}
	}
	
	/**
	 * Update database.
	 */
	void updateDb()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("UPDATE raidboss_spawnlist SET respawn_time = ?, currentHP = ?, currentMP = ? WHERE boss_id = ?"))
		{
			for (Integer bossId : _storedInfo.keySet())
			{
				if (bossId == null)
				{
					continue;
				}
				
				final RaidBoss boss = _bosses.get(bossId);
				if (boss == null)
				{
					continue;
				}
				
				if (boss.getRaidStatus().equals(RaidBossStatus.ALIVE))
				{
					updateStatus(boss, false);
				}
				
				final StatSet info = _storedInfo.get(bossId);
				if (info == null)
				{
					continue;
				}
				
				try
				{
					// TODO(Zoey76): Change this to use batch.
					statement.setLong(1, info.getLong("respawnTime"));
					statement.setDouble(2, boss.isDead() ? boss.getMaxHp() : info.getDouble("currentHP"));
					statement.setDouble(3, boss.isDead() ? boss.getMaxMp() : info.getDouble("currentMP"));
					statement.setInt(4, bossId);
					statement.executeUpdate();
					statement.clearParameters();
				}
				catch (SQLException e)
				{
					LOGGER.warning(getClass().getSimpleName() + ": Couldnt update raidboss_spawnlist table " + e.getMessage());
				}
			}
		}
		catch (SQLException e)
		{
			LOGGER.warning(getClass().getSimpleName() + ": SQL error while updating RaidBoss spawn to database: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the all raid boss status.
	 * @return the all raid boss status
	 */
	public String[] getAllRaidBossStatus()
	{
		final String[] msg = new String[(_bosses == null) ? 0 : _bosses.size()];
		if (_bosses == null)
		{
			msg[0] = "None";
			return msg;
		}
		
		int index = 0;
		for (RaidBoss boss : _bosses.values())
		{
			msg[index++] = boss.getName() + ": " + boss.getRaidStatus().name();
		}
		
		return msg;
	}
	
	/**
	 * Gets the raid boss status.
	 * @param bossId the boss id
	 * @return the raid boss status
	 */
	public String getRaidBossStatus(int bossId)
	{
		String msg = "RaidBoss Status..." + Config.EOL;
		if (_bosses == null)
		{
			return msg += "None";
		}
		
		if (_bosses.containsKey(bossId))
		{
			final RaidBoss boss = _bosses.get(bossId);
			msg += boss.getName() + ": " + boss.getRaidStatus().name();
		}
		
		return msg;
	}
	
	/**
	 * Gets the raid boss status id.
	 * @param bossId the boss id
	 * @return the raid boss status id
	 */
	public RaidBossStatus getRaidBossStatusId(int bossId)
	{
		if (_bosses.containsKey(bossId))
		{
			return _bosses.get(bossId).getRaidStatus();
		}
		else if (_schedules.containsKey(bossId))
		{
			return RaidBossStatus.DEAD;
		}
		else
		{
			return RaidBossStatus.UNDEFINED;
		}
	}
	
	public NpcTemplate getValidTemplate(int bossId)
	{
		final NpcTemplate template = NpcTable.getInstance().getTemplate(bossId);
		if (template == null)
		{
			return null;
		}
		
		if (!template.getType().equalsIgnoreCase("RaidBoss"))
		{
			return null;
		}
		
		return template;
	}
	
	/**
	 * Notify spawn night boss.
	 * @param raidboss the raidboss
	 */
	public void notifySpawnNightBoss(RaidBoss raidboss)
	{
		final StatSet info = new StatSet();
		info.set("currentHP", raidboss.getCurrentHp());
		info.set("currentMP", raidboss.getCurrentMp());
		info.set("respawnTime", 0);
		raidboss.setRaidStatus(RaidBossStatus.ALIVE);
		
		_storedInfo.put(raidboss.getNpcId(), info);
		
		AdminData.broadcastMessageToGMs("Spawning Raid Boss " + raidboss.getName());
		_bosses.put(raidboss.getNpcId(), raidboss);
	}
	
	/**
	 * Checks if the boss is defined.
	 * @param bossId the boss id
	 * @return {@code true} if is defined
	 */
	public boolean isDefined(int bossId)
	{
		return _spawns.containsKey(bossId);
	}
	
	/**
	 * Gets the bosses.
	 * @return the bosses
	 */
	public Map<Integer, RaidBoss> getBosses()
	{
		return _bosses;
	}
	
	/**
	 * Gets the spawns.
	 * @return the spawns
	 */
	public Map<Integer, Spawn> getSpawns()
	{
		return _spawns;
	}
	
	/**
	 * Gets the stored info.
	 * @return the stored info
	 */
	public Map<Integer, StatSet> getStoredInfo()
	{
		return _storedInfo;
	}
	
	/**
	 * Saves and clears the raid bosses status, including all schedules.
	 */
	public void cleanUp()
	{
		updateDb();
		
		_bosses.clear();
		
		for (ScheduledFuture<?> schedule : _schedules.values())
		{
			schedule.cancel(true);
		}
		_schedules.clear();
		
		_storedInfo.clear();
		_spawns.clear();
	}
	
	public StatSet getStatSet(int bossId)
	{
		return _storedInfo.get(bossId);
	}
	
	public RaidBoss getBoss(int bossId)
	{
		return _bosses.get(bossId);
	}
	
	/**
	 * Gets the single instance of RaidBossSpawnManager.
	 * @return single instance of RaidBossSpawnManager
	 */
	public static RaidBossSpawnManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final RaidBossSpawnManager INSTANCE = new RaidBossSpawnManager();
	}
}
