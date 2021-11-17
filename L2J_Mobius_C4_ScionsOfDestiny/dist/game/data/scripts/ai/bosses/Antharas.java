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
package ai.bosses;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.Rnd;
import org.l2jmobius.gameserver.ai.CtrlIntention;
import org.l2jmobius.gameserver.data.SkillTable;
import org.l2jmobius.gameserver.data.sql.NpcTable;
import org.l2jmobius.gameserver.data.sql.SpawnTable;
import org.l2jmobius.gameserver.geoengine.GeoEngine;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.actor.templates.NpcTemplate;
import org.l2jmobius.gameserver.model.quest.Quest;
import org.l2jmobius.gameserver.model.spawn.Spawn;
import org.l2jmobius.gameserver.model.zone.type.BossZone;
import org.l2jmobius.gameserver.network.serverpackets.Earthquake;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;
import org.l2jmobius.gameserver.network.serverpackets.SpecialCamera;

/**
 * @author L2J_JP SANDMAN
 */
public class Antharas extends Quest
{
	protected static final Logger LOGGER = Logger.getLogger(Antharas.class.getName());
	
	// Antharas status.
	private static final int DORMANT = 0; // Antharas is spawned and no one has entered yet. Entry is unlocked
	private static final int WAITING = 1; // Antharas is spawend and someone has entered, triggering a 30 minute window for additional people to enter before he unleashes his attack. Entry is unlocked.
	private static final int FIGHTING = 2; // Antharas is engaged in battle, annihilating his foes. Entry is locked
	private static final int DEAD = 3; // Antharas has been killed. Entry is locked
	// Monsters.
	private static final int ANTHARAS_OLD = 29019;
	private static final int ANTHARAS_WEAK = 29066;
	private static final int ANTHARAS_NORMAL = 29067;
	private static final int ANTHARAS_STRONG = 29068;
	// Configs.
	private static final int FWA_ACTIVITYTIMEOFANTHARAS = 120;
	protected static final boolean FWA_OLDANTHARAS = Config.ANTHARAS_OLD; // Use antharas Interlude with minions.
	private static final boolean FWA_MOVEATRANDOM = true;
	private static final boolean FWA_DOSERVEREARTHQUAKE = true;
	private static final int FWA_LIMITOFWEAK = 45;
	private static final int FWA_LIMITOFNORMAL = 63;
	private static final int FWA_MAXMOBS = 10; // This includes Antharas.
	private static final int FWA_INTERVALOFMOBSWEAK = 180000;
	private static final int FWA_INTERVALOFMOBSNORMAL = 150000;
	private static final int FWA_INTERVALOFMOBSSTRONG = 120000;
	private static final int FWA_PERCENTOFBEHEMOTH = 60;
	private static final int FWA_SELFDESTRUCTTIME = 15000;
	// Location of teleport cube.
	private static final int TELEPORT_CUBE = 31859;
	private static final int[] TELEPORT_CUBE_LOCATION =
	{
		177615,
		114941,
		-7709,
		0
	};
	// Tasks.
	protected ScheduledFuture<?> _cubeSpawnTask = null;
	protected ScheduledFuture<?> _monsterSpawnTask = null;
	protected ScheduledFuture<?> _activityCheckTask = null;
	protected ScheduledFuture<?> _socialTask = null;
	protected ScheduledFuture<?> _mobiliseTask = null;
	protected ScheduledFuture<?> _mobsSpawnTask = null;
	protected ScheduledFuture<?> _selfDestructionTask = null;
	protected ScheduledFuture<?> _moveAtRandomTask = null;
	protected ScheduledFuture<?> _movieTask = null;
	// Misc.
	protected Collection<Spawn> _teleportCubeSpawn = ConcurrentHashMap.newKeySet();
	protected Collection<Npc> _teleportCube = ConcurrentHashMap.newKeySet();
	protected Map<Integer, Spawn> _monsterSpawn = new ConcurrentHashMap<>();
	protected Collection<Npc> _monsters = ConcurrentHashMap.newKeySet();
	protected GrandBoss _antharas = null;
	protected static long _lastAction = 0;
	protected static BossZone _zone;
	
	public Antharas()
	{
		super(-1, "ai/bosses");
		registerMobs(ANTHARAS_OLD, ANTHARAS_WEAK, ANTHARAS_NORMAL, ANTHARAS_STRONG, 29069, 29070, 29071, 29072, 29073, 29074, 29075, 29076);
		init();
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("setAntharasSpawnTask"))
		{
			setAntharasSpawnTask();
		}
		return null;
	}
	
	private void init()
	{
		// Setting spawn data of monsters.
		try
		{
			_zone = GrandBossManager.getInstance().getZone(179700, 113800, -7709);
			NpcTemplate template;
			Spawn tempSpawn;
			
			// Old Antharas.
			template = NpcTable.getInstance().getTemplate(ANTHARAS_OLD);
			tempSpawn = new Spawn(template);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(ANTHARAS_OLD, tempSpawn);
			
			// Weak Antharas.
			template = NpcTable.getInstance().getTemplate(ANTHARAS_WEAK);
			tempSpawn = new Spawn(template);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(ANTHARAS_WEAK, tempSpawn);
			
			// Normal Antharas.
			template = NpcTable.getInstance().getTemplate(ANTHARAS_NORMAL);
			tempSpawn = new Spawn(template);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(ANTHARAS_NORMAL, tempSpawn);
			
			// Strong Antharas.
			template = NpcTable.getInstance().getTemplate(ANTHARAS_STRONG);
			tempSpawn = new Spawn(template);
			tempSpawn.setX(181323);
			tempSpawn.setY(114850);
			tempSpawn.setZ(-7623);
			tempSpawn.setHeading(32542);
			tempSpawn.setAmount(1);
			tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
			SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
			_monsterSpawn.put(ANTHARAS_STRONG, tempSpawn);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		// Setting spawn data of teleport cube.
		try
		{
			final NpcTemplate cube = NpcTable.getInstance().getTemplate(TELEPORT_CUBE);
			Spawn spawnDat;
			spawnDat = new Spawn(cube);
			spawnDat.setAmount(1);
			spawnDat.setX(TELEPORT_CUBE_LOCATION[0]);
			spawnDat.setY(TELEPORT_CUBE_LOCATION[1]);
			spawnDat.setZ(TELEPORT_CUBE_LOCATION[2]);
			spawnDat.setHeading(TELEPORT_CUBE_LOCATION[3]);
			spawnDat.setRespawnDelay(60);
			spawnDat.setLocation(0);
			SpawnTable.getInstance().addNewSpawn(spawnDat, false);
			_teleportCubeSpawn.add(spawnDat);
		}
		catch (Exception e)
		{
			LOGGER.warning(e.getMessage());
		}
		
		Integer status = GrandBossManager.getInstance().getBossStatus(ANTHARAS_OLD);
		if (FWA_OLDANTHARAS || (status == WAITING))
		{
			final StatSet info = GrandBossManager.getInstance().getStatSet(ANTHARAS_OLD);
			final Long respawnTime = info.getLong("respawn_time");
			if ((status == DEAD) && (respawnTime <= Chronos.currentTimeMillis()))
			{
				// The time has already expired while the server was offline. Immediately spawn antharas in his cave.
				// Also, the status needs to be changed to DORMANT.
				GrandBossManager.getInstance().setBossStatus(ANTHARAS_OLD, DORMANT);
				status = DORMANT;
			}
			else if (status == FIGHTING)
			{
				final int x = info.getInt("loc_x");
				final int y = info.getInt("loc_y");
				final int z = info.getInt("loc_z");
				final int heading = info.getInt("heading");
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_antharas = (GrandBoss) addSpawn(ANTHARAS_OLD, x, y, z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_lastAction = Chronos.currentTimeMillis();
				// Start repeating timer to check for inactivity.
				_activityCheckTask = ThreadPool.scheduleAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if (status == DEAD)
			{
				ThreadPool.schedule(new UnlockAntharas(ANTHARAS_OLD), respawnTime - Chronos.currentTimeMillis());
			}
			else if (status == DORMANT)
			{
				// Here status is 0 on Database, don't do anything.
			}
			else
			{
				setAntharasSpawnTask();
			}
		}
		else
		{
			final Integer statusWeak = GrandBossManager.getInstance().getBossStatus(ANTHARAS_WEAK);
			final Integer statusNormal = GrandBossManager.getInstance().getBossStatus(ANTHARAS_NORMAL);
			final Integer statusStrong = GrandBossManager.getInstance().getBossStatus(ANTHARAS_STRONG);
			int antharasId = 0;
			if ((statusWeak == FIGHTING) || (statusWeak == DEAD))
			{
				antharasId = ANTHARAS_WEAK;
				status = statusWeak;
			}
			else if ((statusNormal == FIGHTING) || (statusNormal == DEAD))
			{
				antharasId = ANTHARAS_NORMAL;
				status = statusNormal;
			}
			else if ((statusStrong == FIGHTING) || (statusStrong == DEAD))
			{
				antharasId = ANTHARAS_STRONG;
				status = statusStrong;
			}
			if ((antharasId != 0) && (status == FIGHTING))
			{
				final StatSet info = GrandBossManager.getInstance().getStatSet(antharasId);
				final int loc_x = info.getInt("loc_x");
				final int loc_y = info.getInt("loc_y");
				final int loc_z = info.getInt("loc_z");
				final int heading = info.getInt("heading");
				final int hp = info.getInt("currentHP");
				final int mp = info.getInt("currentMP");
				_antharas = (GrandBoss) addSpawn(antharasId, loc_x, loc_y, loc_z, heading, false, 0);
				GrandBossManager.getInstance().addBoss(_antharas);
				_antharas.setCurrentHpMp(hp, mp);
				_lastAction = Chronos.currentTimeMillis();
				// Start repeating timer to check for inactivity.
				_activityCheckTask = ThreadPool.scheduleAtFixedRate(new CheckActivity(), 60000, 60000);
			}
			else if ((antharasId != 0) && (status == DEAD))
			{
				final StatSet info = GrandBossManager.getInstance().getStatSet(antharasId);
				final Long respawnTime = info.getLong("respawn_time");
				if (respawnTime <= Chronos.currentTimeMillis())
				{
					// The time has already expired while the server was offline. Immediately spawn antharas in his cave.
					// Also, the status needs to be changed to DORMANT.
					GrandBossManager.getInstance().setBossStatus(antharasId, DORMANT);
					status = DORMANT;
				}
				else
				{
					ThreadPool.schedule(new UnlockAntharas(antharasId), respawnTime - Chronos.currentTimeMillis());
				}
			}
		}
	}
	
	public void spawnCube()
	{
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		
		for (Spawn spawnDat : _teleportCubeSpawn)
		{
			_teleportCube.add(spawnDat.doSpawn());
		}
	}
	
	public void setAntharasSpawnTask()
	{
		if (_monsterSpawnTask == null)
		{
			synchronized (this)
			{
				if (_monsterSpawnTask == null)
				{
					GrandBossManager.getInstance().setBossStatus(ANTHARAS_OLD, WAITING);
					_monsterSpawnTask = ThreadPool.schedule(new AntharasSpawn(1), 60000 * Config.ANTHARAS_WAIT_TIME);
				}
			}
		}
	}
	
	protected void startMinionSpawns(int antharasId)
	{
		int intervalOfMobs;
		
		// Interval of minions is decided by the type of Antharas that invaded the lair.
		switch (antharasId)
		{
			case ANTHARAS_WEAK:
			{
				intervalOfMobs = FWA_INTERVALOFMOBSWEAK;
				break;
			}
			case ANTHARAS_NORMAL:
			{
				intervalOfMobs = FWA_INTERVALOFMOBSNORMAL;
				break;
			}
			default:
			{
				intervalOfMobs = FWA_INTERVALOFMOBSSTRONG;
				break;
			}
		}
		
		// Spawn mobs.
		_mobsSpawnTask = ThreadPool.scheduleAtFixedRate(new MinionsSpawn(), intervalOfMobs, intervalOfMobs);
	}
	
	private class AntharasSpawn implements Runnable
	{
		private int _taskId = 0;
		private Collection<Creature> _players;
		
		AntharasSpawn(int taskId)
		{
			_taskId = taskId;
			if (_zone.getCharactersInside() != null)
			{
				_players = _zone.getCharactersInside();
			}
		}
		
		@Override
		public void run()
		{
			int npcId;
			Spawn antharasSpawn = null;
			
			switch (_taskId)
			{
				case 1: // Spawn.
				{
					// Strength of Antharas is decided by the number of players that invaded the lair.
					_monsterSpawnTask.cancel(false);
					_monsterSpawnTask = null;
					if (FWA_OLDANTHARAS)
					{
						npcId = ANTHARAS_OLD;
					}
					else if ((_players == null) || (_players.size() <= FWA_LIMITOFWEAK))
					{
						npcId = ANTHARAS_WEAK;
					}
					else if (_players.size() > FWA_LIMITOFNORMAL)
					{
						npcId = ANTHARAS_STRONG;
					}
					else
					{
						npcId = ANTHARAS_NORMAL;
					}
					// Do spawn.
					antharasSpawn = _monsterSpawn.get(npcId);
					_antharas = (GrandBoss) antharasSpawn.doSpawn();
					GrandBossManager.getInstance().addBoss(_antharas);
					_monsters.add(_antharas);
					_antharas.setImmobilized(true);
					GrandBossManager.getInstance().setBossStatus(ANTHARAS_OLD, DORMANT);
					GrandBossManager.getInstance().setBossStatus(npcId, FIGHTING);
					_lastAction = Chronos.currentTimeMillis();
					// Start repeating timer to check for inactivity.
					_activityCheckTask = ThreadPool.scheduleAtFixedRate(new CheckActivity(), 60000, 60000);
					// Setting 1st time of minions spawn task.
					if (!FWA_OLDANTHARAS)
					{
						startMinionSpawns(npcId);
					}
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(2), 16);
					break;
				}
				case 2:
				{
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, -19, 0, 20000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(3), 3000);
					break;
				}
				case 3:
				{
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 700, 13, 0, 6000, 20000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(4), 10000);
					break;
				}
				case 4:
				{
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 3700, 0, -3, 0, 10000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(5), 200);
					break;
				}
				case 5:
				{
					// Do social.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 22000, 30000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(6), 10800);
					break;
				}
				case 6:
				{
					// Set camera.
					broadcastPacket(new SpecialCamera(_antharas.getObjectId(), 1100, 0, -3, 300, 7000));
					// Set next task.
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					_socialTask = ThreadPool.schedule(new AntharasSpawn(7), 1900);
					break;
				}
				case 7:
				{
					_antharas.abortCast();
					_mobiliseTask = ThreadPool.schedule(new SetMobilised(_antharas), 16);
					// Move at random.
					if (FWA_MOVEATRANDOM)
					{
						final Location pos = new Location(Rnd.get(175000, 178500), Rnd.get(112400, 116000), -7707, 0);
						_moveAtRandomTask = ThreadPool.schedule(new MoveAtRandom(_antharas, pos), 500);
					}
					if (_socialTask != null)
					{
						_socialTask.cancel(true);
						_socialTask = null;
					}
					break;
				}
			}
		}
	}
	
	protected void broadcastPacket(IClientOutgoingPacket mov)
	{
		if (_zone != null)
		{
			for (Creature creatures : _zone.getCharactersInside())
			{
				if (creatures instanceof Player)
				{
					creatures.sendPacket(mov);
				}
			}
		}
	}
	
	private class MinionsSpawn implements Runnable
	{
		public MinionsSpawn()
		{
		}
		
		@Override
		public void run()
		{
			NpcTemplate template1;
			Spawn tempSpawn;
			final boolean isBehemoth = Rnd.get(100) < FWA_PERCENTOFBEHEMOTH;
			try
			{
				final int mobNumber = (isBehemoth ? 2 : 3);
				// Set spawn.
				for (int i = 0; i < mobNumber; i++)
				{
					if (_monsters.size() >= FWA_MAXMOBS)
					{
						break;
					}
					int npcId;
					if (isBehemoth)
					{
						npcId = 29069;
					}
					else
					{
						npcId = Rnd.get(29070, 29076);
					}
					template1 = NpcTable.getInstance().getTemplate(npcId);
					tempSpawn = new Spawn(template1);
					// Allocates it at random in the lair of Antharas.
					int tried = 0;
					boolean notFound = true;
					int x = 175000;
					int y = 112400;
					int dt = ((_antharas.getX() - x) * (_antharas.getX() - x)) + ((_antharas.getY() - y) * (_antharas.getY() - y));
					while ((tried++ < 25) && notFound)
					{
						final int rx = Rnd.get(175000, 179900);
						final int ry = Rnd.get(112400, 116000);
						final int rdt = ((_antharas.getX() - rx) * (_antharas.getX() - rx)) + ((_antharas.getY() - ry) * (_antharas.getY() - ry));
						final Location randomLocation = new Location(rx, ry, -7704);
						if (GeoEngine.getInstance().canSeeLocation(_antharas, randomLocation) && (rdt < dt))
						{
							x = rx;
							y = ry;
							dt = rdt;
							if (rdt <= 900000)
							{
								notFound = false;
							}
						}
					}
					tempSpawn.setX(x);
					tempSpawn.setY(y);
					tempSpawn.setZ(-7704);
					tempSpawn.setHeading(0);
					tempSpawn.setAmount(1);
					tempSpawn.setRespawnDelay(FWA_ACTIVITYTIMEOFANTHARAS * 2);
					SpawnTable.getInstance().addNewSpawn(tempSpawn, false);
					// Do spawn.
					_monsters.add(tempSpawn.doSpawn());
				}
			}
			catch (Exception e)
			{
				LOGGER.warning(e.getMessage());
			}
		}
	}
	
	@Override
	public String onAggroRangeEnter(Npc npc, Player player, boolean isPet)
	{
		switch (npc.getNpcId())
		{
			case 29070:
			case 29071:
			case 29072:
			case 29073:
			case 29074:
			case 29075:
			case 29076:
			{
				if ((_selfDestructionTask == null) && !npc.isDead())
				{
					_selfDestructionTask = ThreadPool.schedule(new SelfDestructionOfBomber(npc), FWA_SELFDESTRUCTTIME);
				}
				break;
			}
		}
		return super.onAggroRangeEnter(npc, player, isPet);
	}
	
	private class SelfDestructionOfBomber implements Runnable
	{
		private final Npc _bomber;
		
		public SelfDestructionOfBomber(Npc bomber)
		{
			_bomber = bomber;
		}
		
		@Override
		public void run()
		{
			Skill skill = null;
			switch (_bomber.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				{
					skill = SkillTable.getInstance().getSkill(5097, 1);
					break;
				}
				case 29076:
				{
					skill = SkillTable.getInstance().getSkill(5094, 1);
					break;
				}
			}
			
			_bomber.doCast(skill);
			
			if (_selfDestructionTask != null)
			{
				_selfDestructionTask.cancel(false);
				_selfDestructionTask = null;
			}
		}
	}
	
	@Override
	public String onSpellFinished(Npc npc, Player player, Skill skill)
	{
		if (npc.isInvul())
		{
			return null;
		}
		else if ((skill != null) && ((skill.getId() == 5097) || (skill.getId() == 5094)))
		{
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				case 29076:
				{
					npc.doDie(npc);
					break;
				}
			}
		}
		return super.onSpellFinished(npc, player, skill);
	}
	
	protected class CheckActivity implements Runnable
	{
		@Override
		public void run()
		{
			final Long temp = (Chronos.currentTimeMillis() - _lastAction);
			if (temp > (Config.ANTHARAS_DESPAWN_TIME * 60000))
			{
				GrandBossManager.getInstance().setBossStatus(_antharas.getNpcId(), DORMANT);
				finishRaid();
			}
		}
	}
	
	public void finishRaid()
	{
		// Eliminate players.
		_zone.oustAllPlayers();
		
		// Not executed tasks is canceled.
		if (_cubeSpawnTask != null)
		{
			_cubeSpawnTask.cancel(true);
			_cubeSpawnTask = null;
		}
		if (_monsterSpawnTask != null)
		{
			_monsterSpawnTask.cancel(true);
			_monsterSpawnTask = null;
		}
		if (_activityCheckTask != null)
		{
			_activityCheckTask.cancel(false);
			_activityCheckTask = null;
		}
		if (_socialTask != null)
		{
			_socialTask.cancel(true);
			_socialTask = null;
		}
		if (_mobiliseTask != null)
		{
			_mobiliseTask.cancel(true);
			_mobiliseTask = null;
		}
		if (_mobsSpawnTask != null)
		{
			_mobsSpawnTask.cancel(true);
			_mobsSpawnTask = null;
		}
		if (_selfDestructionTask != null)
		{
			_selfDestructionTask.cancel(true);
			_selfDestructionTask = null;
		}
		if (_moveAtRandomTask != null)
		{
			_moveAtRandomTask.cancel(true);
			_moveAtRandomTask = null;
		}
		
		// Delete monsters.
		for (Npc mob : _monsters)
		{
			mob.getSpawn().stopRespawn();
			mob.deleteMe();
		}
		_monsters.clear();
		
		// Delete teleport cube.
		for (Npc cube : _teleportCube)
		{
			cube.getSpawn().stopRespawn();
			cube.deleteMe();
		}
		_teleportCube.clear();
	}
	
	private class CubeSpawn implements Runnable
	{
		private final int _type;
		
		CubeSpawn(int type)
		{
			_type = type;
		}
		
		@Override
		public void run()
		{
			if (_type == 0)
			{
				spawnCube();
				_cubeSpawnTask = ThreadPool.schedule(new CubeSpawn(1), 1800000);
			}
			else
			{
				finishRaid();
			}
		}
	}
	
	private static class UnlockAntharas implements Runnable
	{
		private final int _bossId;
		
		public UnlockAntharas(int bossId)
		{
			_bossId = bossId;
		}
		
		@Override
		public void run()
		{
			GrandBossManager.getInstance().setBossStatus(_bossId, DORMANT);
			if (FWA_DOSERVEREARTHQUAKE)
			{
				for (Player p : World.getInstance().getAllPlayers())
				{
					p.broadcastPacket(new Earthquake(185708, 114298, -8221, 20, 10));
				}
			}
		}
	}
	
	private class SetMobilised implements Runnable
	{
		private final GrandBoss _boss;
		
		public SetMobilised(GrandBoss boss)
		{
			_boss = boss;
		}
		
		@Override
		public void run()
		{
			_boss.setImmobilized(false);
			
			// When it is possible to act, a social action is canceled.
			if (_socialTask != null)
			{
				_socialTask.cancel(true);
				_socialTask = null;
			}
		}
	}
	
	private static class MoveAtRandom implements Runnable
	{
		private final Npc _npc;
		private final Location _pos;
		
		public MoveAtRandom(Npc npc, Location pos)
		{
			_npc = npc;
			_pos = pos;
		}
		
		@Override
		public void run()
		{
			_npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, _pos);
		}
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isPet)
	{
		if ((npc.getNpcId() == ANTHARAS_OLD) || (npc.getNpcId() == ANTHARAS_WEAK) || (npc.getNpcId() == ANTHARAS_NORMAL) || (npc.getNpcId() == ANTHARAS_STRONG))
		{
			_lastAction = Chronos.currentTimeMillis();
			if (!FWA_OLDANTHARAS && (_mobsSpawnTask == null))
			{
				startMinionSpawns(npc.getNpcId());
			}
		}
		else if ((npc.getNpcId() > 29069) && (npc.getNpcId() < 29077) && (npc.getCurrentHp() <= damage))
		{
			Skill skill = null;
			switch (npc.getNpcId())
			{
				case 29070:
				case 29071:
				case 29072:
				case 29073:
				case 29074:
				case 29075:
				{
					skill = SkillTable.getInstance().getSkill(5097, 1);
					break;
				}
				case 29076:
				{
					skill = SkillTable.getInstance().getSkill(5094, 1);
					break;
				}
			}
			npc.doCast(skill);
		}
		return super.onAttack(npc, attacker, damage, isPet);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if ((npc.getNpcId() == ANTHARAS_OLD) || (npc.getNpcId() == ANTHARAS_WEAK) || (npc.getNpcId() == ANTHARAS_NORMAL) || (npc.getNpcId() == ANTHARAS_STRONG))
		{
			npc.broadcastPacket(new PlaySound(1, "BS01_D", npc));
			_cubeSpawnTask = ThreadPool.schedule(new CubeSpawn(0), 10000);
			GrandBossManager.getInstance().setBossStatus(npc.getNpcId(), DEAD);
			final long respawnTime = (Config.ANTHARAS_RESP_FIRST + Rnd.get(Config.ANTHARAS_RESP_SECOND)) * 3600000;
			ThreadPool.schedule(new UnlockAntharas(npc.getNpcId()), respawnTime);
			// Also save the respawn time so that the info is maintained past restarts.
			final StatSet info = GrandBossManager.getInstance().getStatSet(npc.getNpcId());
			info.set("respawn_time", (Chronos.currentTimeMillis() + respawnTime));
			GrandBossManager.getInstance().setStatSet(npc.getNpcId(), info);
		}
		else if (npc.getNpcId() == 29069)
		{
			final int hpHerbCount = Rnd.get(6, 18);
			final int mpHerbCount = Rnd.get(6, 18);
			for (int i = 0; i < hpHerbCount; i++)
			{
				((Monster) npc).dropItem(killer, 8602, 1);
			}
			for (int i = 0; i < mpHerbCount; i++)
			{
				((Monster) npc).dropItem(killer, 8605, 1);
			}
		}
		if (_monsters.contains(npc))
		{
			_monsters.remove(npc);
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new Antharas();
	}
}