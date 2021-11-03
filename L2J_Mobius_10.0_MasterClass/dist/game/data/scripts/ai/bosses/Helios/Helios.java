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
package ai.bosses.Helios;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.GrandBossInstance;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.holders.SpawnHolder;
import org.l2jmobius.gameserver.model.zone.type.NoSummonFriendZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.util.Broadcast;

import ai.AbstractNpcAI;

/**
 * @author Mobius, NviX
 */
public class Helios extends AbstractNpcAI
{
	// Raid
	private static final int HELIOS1 = 29303;
	private static final int HELIOS2 = 29304;
	private static final int HELIOS3 = 29305;
	// Minions
	private static final int LEOPOLD = 29306;
	private static final int HELIOS_RED_LIGHTNING = 29307;
	private static final int HELIOS_BLUE_LIGHTNING = 29308;
	private static final int LEOPOLD_ORIGIN = 29309;
	private static final int ENUMA_ELISH_ORIGIN = 29310;
	private static final int ROYAL_TEMPLAR_COLONEL = 29311;
	private static final int ROYAL_SHARPSHOOTER = 29312;
	private static final int ROYAL_ARCHMAGE = 29313;
	private static final int ROYAL_GATEKEEPER = 29314;
	private static final int MIMILLION = 29315;
	private static final int MIMILLUS = 29316;
	// Location
	private static final Location HELIOS_SPAWN_LOC = new Location(92771, 161909, 3494, 38329);
	private static final Location BLUE_LIGHTNING_SPEAR_LOC = new Location(93208, 161269, 3489);
	private static final Location RED_LIGHTNING_SPEAR_LOC = new Location(92348, 162558, 3489);
	private static final Location MIMILLION_LOC = new Location(92465, 162465, 3487);
	private static final Location MIMILLUS_LOC = new Location(93174, 161394, 3487);
	private static final Location LEOPOLD_LOC = new Location(93531, 162415, 3487);
	private static final Location LEOPOLD_ORIGIN_LOC = new Location(92601, 162196, 3464);
	private static final Location ENUMA_ELISH_ORIGIN_LOC = new Location(92957, 161640, 3485);
	// Zone
	private static final int ZONE_ID = 210109;
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int FIGHTING = 2;
	private static final int DEAD = 3;
	// Skills
	private static final SkillHolder AUDIENCE_DEBUFF = new SkillHolder(16613, 1);
	private static final SkillHolder RED_LIGHTNING_SPEAR = new SkillHolder(16617, 1);
	private static final SkillHolder BLUE_LIGHTNING_SPEAR = new SkillHolder(16618, 1);
	private static final SkillHolder PRANARACH = new SkillHolder(16624, 1);
	private static final SkillHolder HELIOS_RAGE1 = new SkillHolder(16625, 1);
	private static final SkillHolder HELIOS_RAGE2 = new SkillHolder(16625, 2);
	private static final SkillHolder HELIOS_RAGE3 = new SkillHolder(16625, 3);
	private static final SkillHolder HELIOS_RAGE4 = new SkillHolder(16625, 4);
	private static final SkillHolder LEOPOLD_BOMB = new SkillHolder(16629, 1);
	private static final SkillHolder LEOPOLD_PLASMA_BOMB = new SkillHolder(16630, 1);
	private static final SkillHolder LEOPOLD_ENERGY_BOMB = new SkillHolder(16631, 1);
	private static final SkillHolder LEOPOLD_MINI_GUN = new SkillHolder(16632, 1);
	private static final SkillHolder LEOPOLD_SPRAY_SHOT = new SkillHolder(16633, 1);
	private static final SkillHolder LEOPOLD_HARPOON = new SkillHolder(16634, 1);
	// Spawns
	private static final List<SpawnHolder> SPAWNS_MINIONS = new ArrayList<>();
	static
	{
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_TEMPLAR_COLONEL, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_TEMPLAR_COLONEL, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_TEMPLAR_COLONEL, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_TEMPLAR_COLONEL, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_TEMPLAR_COLONEL, HELIOS_SPAWN_LOC, 0, false));
		
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_SHARPSHOOTER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_SHARPSHOOTER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_SHARPSHOOTER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_SHARPSHOOTER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_SHARPSHOOTER, HELIOS_SPAWN_LOC, 0, false));
		
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_ARCHMAGE, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_ARCHMAGE, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_ARCHMAGE, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_ARCHMAGE, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_ARCHMAGE, HELIOS_SPAWN_LOC, 0, false));
		
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_GATEKEEPER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_GATEKEEPER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_GATEKEEPER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_GATEKEEPER, HELIOS_SPAWN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(ROYAL_GATEKEEPER, HELIOS_SPAWN_LOC, 0, false));
	}
	// Misc
	private static final int HELIOS_RAID_DURATION = 5; // hours
	private static Npc bossInstance;
	private final NoSummonFriendZone bossZone;
	private GrandBossInstance _tempHelios;
	private static List<Npc> _minionSpawns = new ArrayList<>();
	private static Npc blueLightning;
	private static Npc redLightning;
	private static Npc leopold;
	private boolean activated = false;
	private boolean stage1_50 = false;
	private boolean stage2_50 = false;
	private boolean helios80 = false;
	private boolean helios50 = false;
	private boolean _announce = false;
	protected ScheduledFuture<?> _blueSpearTask;
	protected ScheduledFuture<?> _redSpearTask;
	protected ScheduledFuture<?> _leopoldTask;
	protected ScheduledFuture<?> _debuffTask;
	
	private Helios()
	{
		addAttackId(HELIOS1, HELIOS2, HELIOS3);
		addKillId(HELIOS1, HELIOS2, HELIOS3, MIMILLION, MIMILLUS);
		// Zone
		bossZone = ZoneManager.getInstance().getZoneById(ZONE_ID, NoSummonFriendZone.class);
		// Unlock
		final StatSet info = GrandBossManager.getInstance().getStatSet(HELIOS3);
		final int status = GrandBossManager.getInstance().getBossStatus(HELIOS3);
		if (status == DEAD)
		{
			final long time = info.getLong("respawn_time") - Chronos.currentTimeMillis();
			if (time > 0)
			{
				startQuestTimer("unlock_helios", time, null, null);
			}
			else
			{
				_tempHelios = (GrandBossInstance) addSpawn(HELIOS3, -126920, -234182, -15563, 0, false, 0);
				GrandBossManager.getInstance().addBoss(_tempHelios);
				GrandBossManager.getInstance().setBossStatus(HELIOS3, ALIVE);
			}
		}
		else
		{
			_tempHelios = (GrandBossInstance) addSpawn(HELIOS3, -126920, -234182, -15563, 0, false, 0);
			GrandBossManager.getInstance().addBoss(_tempHelios);
			GrandBossManager.getInstance().setBossStatus(HELIOS3, ALIVE);
		}
	}
	
	@Override
	public String onAttack(Npc npc, PlayerInstance attacker, int damage, boolean isSummon)
	{
		if ((npc.getId() == HELIOS1) && !_announce)
		{
			_announce = true;
			_debuffTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				bossZone.getPlayersInside().forEach(player ->
				{
					AUDIENCE_DEBUFF.getSkill().applyEffects(player, player);
				});
			}, 5000, 20000);
			Broadcast.toAllOnlinePlayers(new ExShowScreenMessage(NpcStringId.THE_ADEN_WARRIORS_BEGIN_BATTLE_WITH_THE_GIANT_EMPEROR_HELIOS, ExShowScreenMessage.TOP_CENTER, 10000, true));
		}
		if ((npc.getId() == HELIOS1) && !stage1_50 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
		{
			stage1_50 = true;
			HELIOS_RAGE1.getSkill().applyEffects(bossInstance, bossInstance);
		}
		if ((npc.getId() == HELIOS2) && !activated)
		{
			activated = true;
			HELIOS_RAGE1.getSkill().applyEffects(bossInstance, bossInstance);
			_blueSpearTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				int count = bossZone.getPlayersInside().size();
				if (count > 0)
				{
					PlayerInstance randomPlayer = bossZone.getPlayersInside().get(getRandom(count));
					if (blueLightning != null)
					{
						blueLightning.setTarget(randomPlayer);
						blueLightning.doCast(BLUE_LIGHTNING_SPEAR.getSkill());
					}
				}
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_PICKS_UP_THE_BLUE_LIGHTNING_SPEAR_AND_BEGINS_GATHERING_HIS_POWER, ExShowScreenMessage.TOP_CENTER, 10000, true));
			}, 10000, 120000);
			_redSpearTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				int count = bossZone.getPlayersInside().size();
				if (count > 0)
				{
					PlayerInstance randomPlayer = bossZone.getPlayersInside().get(getRandom(count));
					if (redLightning != null)
					{
						redLightning.setTarget(randomPlayer);
						redLightning.doCast(RED_LIGHTNING_SPEAR.getSkill());
					}
				}
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_PICKS_UP_THE_RED_LIGHTNING_SPEAR_AND_BEGINS_GATHERING_HIS_POWER, ExShowScreenMessage.TOP_CENTER, 10000, true));
			}, 30000, 120000);
			_leopoldTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				int count = bossZone.getPlayersInside().size();
				if (count > 0)
				{
					PlayerInstance randomPlayer = bossZone.getPlayersInside().get(getRandom(count));
					if (leopold != null)
					{
						leopold.setTarget(randomPlayer);
						int rnd = getRandom(100);
						if (rnd < 16)
						{
							leopold.doCast(LEOPOLD_BOMB.getSkill());
						}
						else if (rnd < 32)
						{
							leopold.doCast(LEOPOLD_PLASMA_BOMB.getSkill());
						}
						else if (rnd < 48)
						{
							leopold.doCast(LEOPOLD_ENERGY_BOMB.getSkill());
						}
						else if (rnd < 64)
						{
							leopold.doCast(LEOPOLD_MINI_GUN.getSkill());
						}
						else if (rnd < 80)
						{
							leopold.doCast(LEOPOLD_SPRAY_SHOT.getSkill());
						}
						else
						{
							leopold.doCast(LEOPOLD_HARPOON.getSkill());
						}
					}
				}
			}, 5000, 10000);
		}
		if ((npc.getId() == HELIOS2) && !stage2_50 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
		{
			stage2_50 = true;
			HELIOS_RAGE2.getSkill().applyEffects(bossInstance, bossInstance);
		}
		if ((npc.getId() == HELIOS3) && !activated)
		{
			activated = true;
			HELIOS_RAGE3.getSkill().applyEffects(bossInstance, bossInstance);
			_leopoldTask = ThreadPool.scheduleAtFixedRate(() ->
			{
				int count = bossZone.getPlayersInside().size();
				if (count > 0)
				{
					PlayerInstance randomPlayer = bossZone.getPlayersInside().get(getRandom(count));
					if (leopold != null)
					{
						leopold.setTarget(randomPlayer);
						int rnd = getRandom(100);
						if (rnd < 16)
						{
							leopold.doCast(LEOPOLD_BOMB.getSkill());
						}
						else if (rnd < 32)
						{
							leopold.doCast(LEOPOLD_PLASMA_BOMB.getSkill());
						}
						else if (rnd < 48)
						{
							leopold.doCast(LEOPOLD_ENERGY_BOMB.getSkill());
						}
						else if (rnd < 64)
						{
							leopold.doCast(LEOPOLD_MINI_GUN.getSkill());
						}
						else if (rnd < 80)
						{
							leopold.doCast(LEOPOLD_SPRAY_SHOT.getSkill());
						}
						else
						{
							leopold.doCast(LEOPOLD_HARPOON.getSkill());
						}
					}
				}
			}, 5000, 10000);
		}
		if ((npc.getId() == HELIOS3) && !helios80 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.8)))
		{
			helios80 = true;
			addSpawn(LEOPOLD_ORIGIN, LEOPOLD_ORIGIN_LOC, false, 0);
			addSpawn(ENUMA_ELISH_ORIGIN, ENUMA_ELISH_ORIGIN_LOC, false, 0);
			bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_KAMAEL_ORIGINS_ABOVE_THE_THRONE_OF_HELIOS_BEGIN_TO_SOAR, ExShowScreenMessage.TOP_CENTER, 10000, true));
		}
		else if ((npc.getId() == HELIOS3) && !helios50 && (npc.getCurrentHp() <= (npc.getMaxHp() * 0.5)))
		{
			helios50 = true;
			HELIOS_RAGE4.getSkill().applyEffects(bossInstance, bossInstance);
			bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_USES_THE_PRANARACH_SHIELD_OF_LIGHT_TO_MINIMIZE_DAMAGE, ExShowScreenMessage.TOP_CENTER, 10000, true));
			bossInstance.abortCast();
			bossInstance.doCast(PRANARACH.getSkill());
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "unlock_helios":
			{
				_tempHelios = (GrandBossInstance) addSpawn(HELIOS3, -126920, -234182, -15563, 0, false, 0);
				GrandBossManager.getInstance().addBoss(_tempHelios);
				GrandBossManager.getInstance().setBossStatus(HELIOS3, ALIVE);
				break;
			}
			case "beginning":
			{
				if (GrandBossManager.getInstance().getBossStatus(HELIOS3) == WAITING)
				{
					GrandBossManager.getInstance().setBossStatus(HELIOS3, FIGHTING);
					bossInstance = addSpawn(HELIOS1, HELIOS_SPAWN_LOC.getX(), HELIOS_SPAWN_LOC.getY(), HELIOS_SPAWN_LOC.getZ(), HELIOS_SPAWN_LOC.getHeading(), false, 0, false);
					for (SpawnHolder spawn : SPAWNS_MINIONS)
					{
						_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
					}
					startQuestTimer("resetRaid", HELIOS_RAID_DURATION * 60 * 60 * 1000, bossInstance, null);
				}
				break;
			}
			case "resetRaid":
			{
				final int status = GrandBossManager.getInstance().getBossStatus(HELIOS3);
				if ((status > ALIVE) && (status < DEAD))
				{
					bossZone.oustAllPlayers();
					Broadcast.toAllOnlinePlayers(new ExShowScreenMessage(NpcStringId.THE_HEROES_DRAINED_OF_THEIR_POWERS_HAVE_BEEN_BANISHED_FROM_THE_THRONE_OF_HELIOS_BY_HELIOS_POWERS, ExShowScreenMessage.TOP_CENTER, 10000, true));
					GrandBossManager.getInstance().setBossStatus(HELIOS3, ALIVE);
					Clean();
				}
				break;
			}
			case "stage2":
			{
				bossInstance = addSpawn(HELIOS2, HELIOS_SPAWN_LOC.getX(), HELIOS_SPAWN_LOC.getY(), HELIOS_SPAWN_LOC.getZ(), HELIOS_SPAWN_LOC.getHeading(), false, 0, false);
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_APPEARANCE_CHANGES_AND_HE_BEGINS_TO_GROW_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, true));
				for (SpawnHolder spawn : SPAWNS_MINIONS)
				{
					_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
				startQuestTimer("spheresSpawn", 10000, null, null);
				break;
			}
			case "stage3":
			{
				activated = false;
				bossInstance = addSpawn(HELIOS3, HELIOS_SPAWN_LOC.getX(), HELIOS_SPAWN_LOC.getY(), HELIOS_SPAWN_LOC.getZ(), HELIOS_SPAWN_LOC.getHeading(), false, 0, false);
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_APPEARANCE_CHANGES_AND_HE_BEGINS_TO_GROW_STRONGER, ExShowScreenMessage.TOP_CENTER, 10000, true));
				startQuestTimer("leopoldSpawn", 10000, null, null);
				break;
			}
			case "spheresSpawn":
			{
				blueLightning = addSpawn(HELIOS_BLUE_LIGHTNING, BLUE_LIGHTNING_SPEAR_LOC, false, 0);
				redLightning = addSpawn(HELIOS_RED_LIGHTNING, RED_LIGHTNING_SPEAR_LOC, false, 0);
				blueLightning.setInvul(true);
				redLightning.setInvul(true);
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_ENUMA_ELISH_SPEAR_ON_THE_THRONE_OF_HELIOS_IS_PREPARED_AND_PLACED_IN_POSITION, ExShowScreenMessage.TOP_CENTER, 10000, true));
				startQuestTimer("protectorsSpawn", 10000, null, null);
				break;
			}
			case "protectorsSpawn":
			{
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.MIMILLION_AND_MIMILLUS_APPEAR_IN_ORDER_TO_PROTECT_THE_ENUMA_ELISH_OF_RED_LIGHTNING_AND_THE_ENUMA_ELISH_OF_BLUE_LIGHTNING, ExShowScreenMessage.TOP_CENTER, 10000, true));
				addSpawn(MIMILLION, MIMILLION_LOC, false, 0);
				addSpawn(MIMILLUS, MIMILLUS_LOC, false, 0);
				startQuestTimer("leopoldSpawn", 10000, null, null);
				break;
			}
			case "leopoldSpawn":
			{
				leopold = addSpawn(LEOPOLD, LEOPOLD_LOC, false, 0);
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.THE_SIEGE_CANNON_LEOPOLD_ON_THE_THRONE_OF_HELIOS_BEGINS_TO_PREPARE_TO_FIRE, ExShowScreenMessage.TOP_CENTER, 10000, true));
				break;
			}
		}
		return htmltext;
	}
	
	private void Clean()
	{
		bossZone.getCharactersInside().forEach(mob ->
		{
			if (mob.isNpc())
			{
				mob.deleteMe();
			}
		});
		if (_blueSpearTask != null)
		{
			_blueSpearTask.cancel(true);
			_blueSpearTask = null;
		}
		if (_redSpearTask != null)
		{
			_redSpearTask.cancel(true);
			_redSpearTask = null;
		}
		if (_leopoldTask != null)
		{
			_leopoldTask.cancel(true);
			_leopoldTask = null;
		}
		if (_debuffTask != null)
		{
			_debuffTask.cancel(true);
			_debuffTask = null;
		}
	}
	
	@Override
	public String onKill(Npc npc, PlayerInstance killer, boolean isSummon)
	{
		switch (npc.getId())
		{
			case MIMILLION:
			{
				if (redLightning != null)
				{
					redLightning.deleteMe();
					bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.MIMILLION_FALLS_AND_THE_RED_LIGHTNING_SPEAR_VANISHES, ExShowScreenMessage.TOP_CENTER, 10000, true));
				}
				break;
			}
			case MIMILLUS:
			{
				if (blueLightning != null)
				{
					blueLightning.deleteMe();
					bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.MIMILLUS_FALLS_AND_THE_BLUE_LIGHTNING_SPEAR_VANISHES, ExShowScreenMessage.TOP_CENTER, 10000, true));
				}
				break;
			}
			case HELIOS1:
			{
				bossInstance.deleteMe();
				bossZone.getPlayersInside().forEach(player ->
				{
					playMovie(player, Movie.SC_HELIOS_TRANS_A);
				});
				startQuestTimer("stage2", 15000, null, null);
				break;
			}
			case HELIOS2:
			{
				bossInstance.deleteMe();
				if (leopold != null)
				{
					leopold.deleteMe();
				}
				bossZone.getPlayersInside().forEach(player ->
				{
					playMovie(player, Movie.SC_HELIOS_TRANS_B);
				});
				startQuestTimer("stage3", 15000, null, null);
				break;
			}
			case HELIOS3:
			{
				Clean();
				bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_DEFEATED_TAKES_FLIGHT_DEEP_IN_TO_THE_SUPERION_FORT_HIS_THRONE_IS_RENDERED_INACTIVE, ExShowScreenMessage.TOP_CENTER, 10000, true));
				GrandBossManager.getInstance().setBossStatus(HELIOS3, DEAD);
				final long respawnTime = (Config.HELIOS_SPAWN_INTERVAL + getRandom(-Config.HELIOS_SPAWN_RANDOM, Config.HELIOS_SPAWN_RANDOM)) * 3600000;
				final StatSet info = GrandBossManager.getInstance().getStatSet(HELIOS3);
				info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
				GrandBossManager.getInstance().setStatSet(HELIOS3, info);
				startQuestTimer("unlock_helios", respawnTime, null, null);
				break;
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Helios();
	}
}