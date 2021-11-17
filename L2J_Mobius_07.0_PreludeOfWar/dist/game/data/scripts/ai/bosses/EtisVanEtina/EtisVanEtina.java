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
package ai.bosses.EtisVanEtina;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.l2jmobius.Config;
import org.l2jmobius.commons.threads.ThreadPool;
import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.commons.util.CommonUtil;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.enums.TeleportWhereType;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.instancemanager.MapRegionManager;
import org.l2jmobius.gameserver.instancemanager.ZoneManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Spawn;
import org.l2jmobius.gameserver.model.StatSet;
import org.l2jmobius.gameserver.model.actor.Attackable;
import org.l2jmobius.gameserver.model.actor.Creature;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.GrandBoss;
import org.l2jmobius.gameserver.model.actor.instance.Monster;
import org.l2jmobius.gameserver.model.holders.SkillHolder;
import org.l2jmobius.gameserver.model.holders.SpawnHolder;
import org.l2jmobius.gameserver.model.quest.QuestTimer;
import org.l2jmobius.gameserver.model.zone.ZoneType;
import org.l2jmobius.gameserver.model.zone.type.NoRestartZone;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import org.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;

import ai.AbstractNpcAI;

/**
 * @author NviX
 */
public class EtisVanEtina extends AbstractNpcAI
{
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int FIGHTING = 2;
	private static final int DEAD = 3;
	// NPCs
	private static final int ETIS_VAN_ETINA1 = 29318;
	private static final int ETIS_VAN_ETINA2 = 29319;
	private static final int KAIN_VAN_HALTER = 29320;
	// Corridor Mobs
	private static final int TEMPLE_ARCHON = 24085;
	private static final int TEMPLE_RAIDER = 24079;
	private static final int TEMPLE_GUARD_CAPTAIN = 24081;
	private static final int TEMPLE_ELITE_CAPTAIN = 24082;
	private static final int TEMPLE_FLAME_MASTER = 24084;
	private static final int TEMPLE_PASSIONATE_SOLDIER = 24080;
	private static final int TEMPLE_DARK_WIZARD = 24083;
	private static final int GREAT_TEMPLE_DECANUS = 24093;
	private static final int GREAT_TEMPLE_SIEGE_CHARIOT = 24096;
	private static final int GREAT_TEMPLE_SOUL_GUIDE = 24094;
	private static final int SOUL_DESTROYER = 24076;
	private static final int BLIND_BERSERKER = 24075;
	private static final int SOUL_REAPER = 24074;
	private static final int JUDGE_OF_HERESY = 24077;
	private static final int GREAT_TEMPLE_DARK_JUDGE = 24095;
	private static final int GREAT_TEMPLE_BUTCHER = 24092;
	private static final int CHOIR_OF_DARKNESS = 24078;
	private static final int TEMPLE_HIGH_PRIEST = 24086;
	private static final int CREED_GUARDIAN = 24073;
	private static final int PARAGON = 24072;
	//@formatter:off
	private static final int[] CORRIDOR_MOBS = {24085, 24079, 24081, 24082, 24084, 24080, 24083, 24093, 24096, 24094, 24076, 24075, 24074, 24077, 24095, 24092, 24078, 24086, 24073};
	//@formatter:on
	// Minions
	private static final int MARTYR_OF_GREED = 29321;
	private static final int LIBERATOR_OF_LUST = 29324;
	private static final int SEEKER_OF_DESPAIR = 29323;
	private static final int GUIDE_OF_PRIDE = 29322;
	// Seals
	private static final int SEAL_OF_GNOSIS = 19677;
	private static final int SEAL_OF_STRIFE = 19678;
	private static final int SEAL_OF_AVARICE = 19679;
	private static final int SEAL_OF_PUNISHMENT = 19680;
	private static final int SEAL_OF_AWAKENING = 19681;
	private static final int SEAL_OF_CALAMITY = 19682;
	private static final int SEAL_OF_DESTRUCTION = 19683;
	// Others
	private static final int BARRICADE = 19724;
	private static final int DOOR1 = 12230702;
	private static final int DOOR2 = 12230704;
	private static final int DOOR3 = 12230802;
	private static final int DOOR4 = 12230804;
	// Location
	private static final Location PARAGON_LOC = new Location(-245757, 187778, 3042);
	private static final Location ETINA_LOC = new Location(-245765, 194229, 3200);
	private static final Location KAIN_LOC = new Location(-245766, 192148, 3054);
	private static final Location SEAL_OF_GNOSIS_LOC = new Location(-246859, 193321, 3045);
	private static final Location SEAL_OF_STRIFE_LOC = new Location(-246888, 192729, 3045);
	private static final Location SEAL_OF_AVARICE_LOC = new Location(-246896, 192148, 3045);
	private static final Location SEAL_OF_PUNISHMENT_LOC = new Location(-244640, 193331, 3044);
	private static final Location SEAL_OF_AWAKENING_LOC = new Location(-244647, 192739, 3045);
	private static final Location SEAL_OF_CALAMITY_LOC = new Location(-244641, 192155, 3045);
	private static final Location SEAL_OF_DESTRUCTION_LOC = new Location(-244640, 191566, 3045);
	
	private static final int ZONE_ID = 85001;
	private static final NoRestartZone BOSS_ZONE = ZoneManager.getInstance().getZoneById(ZONE_ID, NoRestartZone.class);
	// Spawns
	private static final List<SpawnHolder> SPAWNS_CORRIDOR = new ArrayList<>();
	static
	{
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -246089, 182518, 2861, 62581, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -245778, 182749, 2861, 57797, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -245538, 182930, 2861, 34847, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -246012, 183159, 2870, 40700, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -245798, 183316, 2900, 59692, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -245509, 183575, 2950, 59859, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ARCHON, -246020, 183718, 2977, 46596, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_RAIDER, -245913, 182571, 2860, 18939, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_RAIDER, -245682, 182685, 2860, 59705, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_RAIDER, -245583, 182903, 2860, 38901, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_RAIDER, -245949, 183012, 2860, 21081, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_RAIDER, -245925, 183334, 2904, 10282, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_RAIDER, -245626, 183509, 2937, 22384, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_GUARD_CAPTAIN, -245682, 182497, 2861, 48877, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_GUARD_CAPTAIN, -245930, 182710, 2861, 44855, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_GUARD_CAPTAIN, -245482, 183005, 2861, 24815, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_GUARD_CAPTAIN, -245874, 183345, 2906, 62359, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_GUARD_CAPTAIN, -245578, 183496, 2935, 58218, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_GUARD_CAPTAIN, -246033, 183721, 2978, 10200, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ELITE_CAPTAIN, -245864, 182593, 2860, 39881, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ELITE_CAPTAIN, -245512, 182948, 2860, 17444, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ELITE_CAPTAIN, -245987, 183275, 2893, 24335, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ELITE_CAPTAIN, -246008, 183765, 2986, 65472, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_ELITE_CAPTAIN, -245595, 183581, 2951, 35910, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_FLAME_MASTER, -245643, 182807, 2860, 52098, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_FLAME_MASTER, -245871, 183166, 2872, 62597, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_FLAME_MASTER, -245615, 183452, 2927, 15114, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_FLAME_MASTER, -245874, 183716, 2977, 38121, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_FLAME_MASTER, -245602, 183883, 3009, 25130, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_PASSIONATE_SOLDIER, -245923, 182788, 2860, 7524, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_PASSIONATE_SOLDIER, -245569, 182962, 2860, 45906, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_PASSIONATE_SOLDIER, -245949, 183312, 2899, 56352, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_PASSIONATE_SOLDIER, -245651, 183439, 2924, 57404, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_PASSIONATE_SOLDIER, -246156, 183790, 2991, 33176, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_PASSIONATE_SOLDIER, -245587, 183823, 2997, 33966, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_DARK_WIZARD, -245762, 182706, 2860, 46796, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_DARK_WIZARD, -245638, 182913, 2860, 63991, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_DARK_WIZARD, -245867, 183306, 2899, 14493, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_DARK_WIZARD, -245804, 183800, 2993, 47355, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_DARK_WIZARD, -245598, 183551, 2945, 37399, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(GREAT_TEMPLE_DECANUS, -245754, 184113, 3025, 47953, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(GREAT_TEMPLE_SIEGE_CHARIOT, -245762, 184863, 3029, 49151, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(GREAT_TEMPLE_SOUL_GUIDE, -245764, 185502, 3032, 45247, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(GREAT_TEMPLE_DARK_JUDGE, -245753, 186185, 3036, 48708, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(GREAT_TEMPLE_BUTCHER, -245767, 186756, 3039, 47997, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -246081, 184205, 3026, 21350, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245571, 184303, 3026, 21662, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -246034, 184943, 3029, 981, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245755, 184691, 3028, 11973, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245493, 185001, 3030, 22853, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245770, 185342, 3032, 46786, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -246034, 185701, 3033, 38598, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245530, 185713, 3033, 22746, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245772, 186029, 3035, 53127, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -246032, 186381, 3037, 57199, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245503, 186312, 3037, 25473, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245905, 186684, 3038, 20419, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245585, 186936, 3041, 25482, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245999, 187393, 3042, 2323, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_DESTROYER, -245435, 187610, 3042, 30564, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245482, 184347, 3027, 5631, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245885, 184457, 3027, 31148, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245604, 184817, 3029, 55428, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245954, 185193, 3031, 56275, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245566, 185624, 3033, 19880, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245882, 185959, 3035, 53078, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245860, 186723, 3039, 50920, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245556, 186293, 3036, 27689, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245524, 187226, 3042, 33041, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(BLIND_BERSERKER, -245903, 187548, 3042, 10250, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245898, 184129, 3025, 56279, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245720, 184633, 3028, 48934, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245639, 185160, 3031, 57162, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245940, 185549, 3033, 43554, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245635, 185848, 3034, 34721, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245858, 186299, 3036, 60699, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245610, 186904, 3040, 64217, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -245394, 187605, 3042, 36104, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(SOUL_REAPER, -246023, 187593, 3042, 58782, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245659, 184163, 3025, 36852, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245910, 184620, 3028, 43041, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245751, 185242, 3031, 49837, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245519, 185631, 3033, 18471, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245876, 185902, 3034, 46344, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245872, 186422, 3037, 29263, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245609, 186755, 3039, 31045, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245992, 187241, 3042, 24909, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(JUDGE_OF_HERESY, -245429, 187640, 3042, 21779, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CHOIR_OF_DARKNESS, -245434, 184197, 3026, 48401, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CHOIR_OF_DARKNESS, -245998, 184978, 3030, 45247, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CHOIR_OF_DARKNESS, -245499, 185745, 3033, 6215, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CHOIR_OF_DARKNESS, -246027, 186380, 3037, 56728, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CHOIR_OF_DARKNESS, -245738, 187327, 3042, 19747, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_HIGH_PRIEST, -245765, 184466, 3027, 60537, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_HIGH_PRIEST, -245770, 185150, 3030, 50987, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_HIGH_PRIEST, -245766, 185731, 3033, 25432, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_HIGH_PRIEST, -245761, 186396, 3037, 48080, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(TEMPLE_HIGH_PRIEST, -245759, 187011, 3041, 23104, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CREED_GUARDIAN, -245877, 184118, 3025, 41582, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CREED_GUARDIAN, -245627, 184861, 3029, 46344, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CREED_GUARDIAN, -245933, 185634, 3033, 58408, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CREED_GUARDIAN, -245569, 186351, 3037, 24389, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CREED_GUARDIAN, -245920, 186924, 3040, 32558, false));
		SPAWNS_CORRIDOR.add(new SpawnHolder(CREED_GUARDIAN, -245510, 187534, 3042, 27370, false));
	}
	private static final List<SpawnHolder> SPAWNS_BARRICADE = new ArrayList<>();
	static
	{
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246068, 182171, 2860, 47916, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245523, 182378, 2860, 48500, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246044, 182760, 2860, 48949, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245731, 183026, 2860, 47934, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246044, 183466, 2929, 51168, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245486, 183398, 2916, 47429, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245760, 183696, 2973, 49488, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246034, 183972, 3024, 49151, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245484, 183965, 3024, 49075, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245762, 184305, 3026, 48894, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246040, 184694, 3028, 48838, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245478, 184653, 3028, 48932, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245763, 185026, 3030, 49099, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246035, 185335, 3031, 49151, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245483, 185328, 3031, 49151, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245764, 185620, 3033, 49389, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246035, 185973, 3035, 49208, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245482, 185972, 3035, 49360, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245757, 186296, 3036, 49060, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -246038, 186578, 3038, 49151, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245481, 186569, 3038, 49041, false));
		SPAWNS_BARRICADE.add(new SpawnHolder(BARRICADE, -245763, 186884, 3040, 48798, false));
	}
	private static final List<SpawnHolder> SPAWNS_MINIONS = new ArrayList<>();
	static
	{
		SPAWNS_MINIONS.add(new SpawnHolder(MARTYR_OF_GREED, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(MARTYR_OF_GREED, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(MARTYR_OF_GREED, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(MARTYR_OF_GREED, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(LIBERATOR_OF_LUST, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(LIBERATOR_OF_LUST, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(LIBERATOR_OF_LUST, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(LIBERATOR_OF_LUST, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(SEEKER_OF_DESPAIR, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(SEEKER_OF_DESPAIR, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(SEEKER_OF_DESPAIR, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(SEEKER_OF_DESPAIR, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(GUIDE_OF_PRIDE, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(GUIDE_OF_PRIDE, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(GUIDE_OF_PRIDE, KAIN_LOC, 0, false));
		SPAWNS_MINIONS.add(new SpawnHolder(GUIDE_OF_PRIDE, KAIN_LOC, 0, false));
	}
	// Skills
	private static final SkillHolder CALL_OF_SEVEN_SIGNS = new SkillHolder(32317, 1);
	private static final SkillHolder CALL_OF_SEVEN_SIGNS_SEAL_N = new SkillHolder(32004, 1);
	private static final SkillHolder ETINA_REVELATION = new SkillHolder(32014, 2);
	private static final SkillHolder STIGMA_OF_REVELATION = new SkillHolder(32015, 1);
	private static final SkillHolder CRY_OF_HOLY_WAR = new SkillHolder(32017, 2);
	private static final SkillHolder STIGMA_OF_MARTYR = new SkillHolder(32018, 1);
	private static final SkillHolder UNDEAD_CREATURE = new SkillHolder(32020, 2);
	private static final SkillHolder ETINA_DIVINE_PUNISHMENT = new SkillHolder(32023, 2);
	private static final SkillHolder ETINA_OBSERVATION = new SkillHolder(32024, 2);
	private static final SkillHolder RAGE_OF_THE_OPEN_EYE = new SkillHolder(32026, 2);
	// Camille -> Horseshoe Trampling, Horizon Bash, Joust Lunge, Call of Etina (summon minions?)
	// Vars
	private static long _lastAction;
	protected ScheduledFuture<?> _collapseTask;
	protected ScheduledFuture<?> _gnosisCastTask;
	protected ScheduledFuture<?> _strifeCastTask;
	protected ScheduledFuture<?> _avariceCastTask;
	protected ScheduledFuture<?> _punishmentCastTask;
	protected ScheduledFuture<?> _awakeningCastTask;
	// summon 5 black holes, that uses skill Explosion of Calamity(32029, 1)
	protected ScheduledFuture<?> _calamityCastTask;
	// summon 5 black holes, that uses skill Cyclone of Chaos(32028, 1)
	protected ScheduledFuture<?> _destructionCastTask;
	private static List<Npc> _spawns = new ArrayList<>();
	private static List<Npc> _barricadeSpawns = new ArrayList<>();
	private static List<Npc> _minionSpawns = new ArrayList<>();
	private Npc _sealOfGnosis;
	private Npc _sealOfStrife;
	private Npc _sealOfAvarice;
	private Npc _sealOfPunishment;
	private Npc _sealOfAwakening;
	private Npc _sealOfCalamity;
	private Npc _sealOfDestruction;
	private static Monster _paragon;
	private static GrandBoss _kain;
	private static GrandBoss _etina;
	private boolean _spawned = false;
	private boolean _kain30 = false;
	private boolean _kain60 = false;
	private boolean _etina80 = false;
	private boolean _etina15 = false;
	
	public EtisVanEtina()
	{
		addEnterZoneId(ZONE_ID);
		addExitZoneId(ZONE_ID);
		addAttackId(CORRIDOR_MOBS);
		addAttackId(KAIN_VAN_HALTER, ETIS_VAN_ETINA1, ETIS_VAN_ETINA2);
		addKillId(CORRIDOR_MOBS);
		addKillId(PARAGON, KAIN_VAN_HALTER, ETIS_VAN_ETINA1, ETIS_VAN_ETINA2);
		
		// Unlock
		final StatSet info = GrandBossManager.getInstance().getStatSet(ETIS_VAN_ETINA1);
		final int status = GrandBossManager.getInstance().getBossStatus(ETIS_VAN_ETINA1);
		if (status == DEAD)
		{
			final long time = info.getLong("respawn_time") - Chronos.currentTimeMillis();
			if (time > 0)
			{
				startQuestTimer("unlock_etina", time, null, null);
			}
			else
			{
				GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, ALIVE);
			}
		}
		else
		{
			GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, ALIVE);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "unlock_etina":
			{
				GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, ALIVE);
				break;
			}
			case "check_activity_task":
			{
				if ((_lastAction + 900000) < Chronos.currentTimeMillis())
				{
					if (GrandBossManager.getInstance().getBossStatus(ETIS_VAN_ETINA1) != ALIVE)
					{
						GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, ALIVE);
					}
					for (Creature creature : BOSS_ZONE.getCharactersInside())
					{
						if (creature != null)
						{
							if (creature.isNpc())
							{
								creature.deleteMe();
							}
							else if (creature.isPlayer())
							{
								creature.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(creature, TeleportWhereType.TOWN));
							}
						}
					}
					startQuestTimer("end_etina", 2000, null, null);
				}
				else if (GrandBossManager.getInstance().getBossStatus(ETIS_VAN_ETINA1) == FIGHTING)
				{
					startQuestTimer("check_activity_task", 60000, null, null);
				}
				break;
			}
			case "openInnerDoor":
			{
				openDoor(DOOR3, 0);
				openDoor(DOOR4, 0);
				startQuestTimer("show_intro_movie", 60000, null, null);
				break;
			}
			case "show_intro_movie":
			{
				BOSS_ZONE.getPlayersInside().forEach(p ->
				{
					playMovie(p, Movie.SC_INZONE_KAIN_INTRO);
				});
				startQuestTimer("spawn_kain", 23000, null, null);
				break;
			}
			case "spawn_kain":
			{
				closeDoor(DOOR1, 0);
				closeDoor(DOOR2, 0);
				closeDoor(DOOR3, 0);
				closeDoor(DOOR4, 0);
				_kain = (GrandBoss) addSpawn(KAIN_VAN_HALTER, KAIN_LOC, false, 0, true);
				_etina = (GrandBoss) addSpawn(ETIS_VAN_ETINA1, ETINA_LOC, false, 0, true);
				_etina.setInvul(true);
				_etina.setTargetable(false);
				_etina.setImmobilized(true);
				BOSS_ZONE.getPlayersInside().forEach(p -> p.sendPacket(new ExShowScreenMessage(NpcStringId.ETIS_VAN_ETINA_AND_HIS_APOSTLES_HAVE_APPEARED, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				break;
			}
			case "spawnTransformedEtina":
			{
				_etina = (GrandBoss) addSpawn(ETIS_VAN_ETINA2, KAIN_LOC, false, 0, true);
				for (SpawnHolder spawn : SPAWNS_MINIONS)
				{
					_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
				break;
			}
			case "cancel_timers":
			{
				final QuestTimer activityTimer = getQuestTimer("check_activity_task", null, null);
				if (activityTimer != null)
				{
					activityTimer.cancel();
				}
				
				QuestTimer forceEnd = getQuestTimer("end_etina", null, null);
				if (forceEnd != null)
				{
					forceEnd.cancel();
				}
				break;
			}
			case "end_etina":
			{
				Clean();
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	protected void Clean()
	{
		BOSS_ZONE.getCharactersInside().forEach(mob ->
		{
			if (mob.isNpc())
			{
				mob.deleteMe();
			}
		});
		if (_gnosisCastTask != null)
		{
			_gnosisCastTask.cancel(false);
			_gnosisCastTask = null;
		}
		if (_strifeCastTask != null)
		{
			_strifeCastTask.cancel(false);
			_strifeCastTask = null;
		}
		if (_avariceCastTask != null)
		{
			_avariceCastTask.cancel(false);
			_avariceCastTask = null;
		}
		if (_punishmentCastTask != null)
		{
			_punishmentCastTask.cancel(false);
			_punishmentCastTask = null;
		}
		if (_awakeningCastTask != null)
		{
			_awakeningCastTask.cancel(false);
			_awakeningCastTask = null;
		}
		if (_calamityCastTask != null)
		{
			_calamityCastTask.cancel(false);
			_calamityCastTask = null;
		}
		if (_destructionCastTask != null)
		{
			_destructionCastTask.cancel(false);
			_destructionCastTask = null;
		}
		if (_collapseTask != null)
		{
			_collapseTask.cancel(false);
			_collapseTask = null;
		}
		notifyEvent("cancel_timers", null, null);
		BOSS_ZONE.oustAllPlayers();
		closeDoor(DOOR1, 0);
		closeDoor(DOOR2, 0);
		closeDoor(DOOR3, 0);
		closeDoor(DOOR4, 0);
		if ((GrandBossManager.getInstance().getBossStatus(ETIS_VAN_ETINA1) != DEAD) && (GrandBossManager.getInstance().getBossStatus(ETIS_VAN_ETINA1) != ALIVE))
		{
			GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, ALIVE);
		}
	}
	
	@Override
	public String onAttack(Npc npc, Player attacker, int damage, boolean isSummon)
	{
		_lastAction = Chronos.currentTimeMillis();
		// Anti BUGGERS
		if (!BOSS_ZONE.isInsideZone(attacker))
		{
			attacker.doDie(null);
			LOGGER.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " out of the boss zone!");
		}
		if (!BOSS_ZONE.isInsideZone(npc))
		{
			Spawn spawn = npc.getSpawn();
			if (spawn != null)
			{
				npc.teleToLocation(spawn.getX(), spawn.getY(), spawn.getZ());
			}
			LOGGER.warning(getName() + ": Character: " + attacker.getName() + " attacked: " + npc.getName() + " wich is out of the boss zone!");
		}
		if (npc.getId() == KAIN_VAN_HALTER)
		{
			if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.3)) && !_kain30)
			{
				_kain30 = true;
				BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.ETIS_VAN_ETINA_SUMMONS_HIS_APOSTLES_MINIONS, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				for (SpawnHolder spawn : SPAWNS_MINIONS)
				{
					_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
			}
			else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.6)) && !_kain60)
			{
				_kain60 = true;
				BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.ETIS_VAN_ETINA_SUMMONS_HIS_APOSTLES_MINIONS, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				for (SpawnHolder spawn : SPAWNS_MINIONS)
				{
					_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
			}
		}
		else if ((npc.getId() == ETIS_VAN_ETINA1))
		{
			if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.8)) && !_etina80)
			{
				_etina80 = true;
				for (SpawnHolder spawn : SPAWNS_MINIONS)
				{
					_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
				_etina.abortCast();
				_etina.broadcastPacket(new MagicSkillUse(_etina, _etina, CALL_OF_SEVEN_SIGNS.getSkillId(), CALL_OF_SEVEN_SIGNS.getSkillLevel(), 3000, 0));
				BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.ETIS_VAN_ETINA_USES_THE_POWER_OF_THE_SEVEN_SIGNS_TO_SUMMON_ALL_7_SEALS_INSIDE_THE_TEMPLE, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				_sealOfGnosis = addSpawn(SEAL_OF_GNOSIS, SEAL_OF_GNOSIS_LOC);
				_sealOfStrife = addSpawn(SEAL_OF_STRIFE, SEAL_OF_STRIFE_LOC);
				_sealOfAvarice = addSpawn(SEAL_OF_AVARICE, SEAL_OF_AVARICE_LOC);
				_sealOfPunishment = addSpawn(SEAL_OF_PUNISHMENT, SEAL_OF_PUNISHMENT_LOC);
				_sealOfAwakening = addSpawn(SEAL_OF_AWAKENING, SEAL_OF_AWAKENING_LOC);
				_sealOfCalamity = addSpawn(SEAL_OF_CALAMITY, SEAL_OF_CALAMITY_LOC);
				_sealOfDestruction = addSpawn(SEAL_OF_DESTRUCTION, SEAL_OF_DESTRUCTION_LOC);
				_gnosisCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfGnosis.setDisplayEffect(3);
					_sealOfGnosis.broadcastPacket(new MagicSkillUse(_sealOfGnosis, _sealOfGnosis, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 1, 10000, 0));
					int rnd = getRandom(BOSS_ZONE.getPlayersInside().size());
					Player member = BOSS_ZONE.getPlayersInside().get(rnd);
					STIGMA_OF_REVELATION.getSkill().applyEffects(member, member);
					_minionSpawns.forEach(minion ->
					{
						if (minion != null)
						{
							ETINA_REVELATION.getSkill().applyEffects(minion, minion);
							((Attackable) minion).addDamageHate(member, 0, 999999999);
						}
					});
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_GNOSIS_ACTIVATES_AND_ENORMOUS_POWER_BEGINS_TO_FLOW_OUT, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 10000, 120000);
				_strifeCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfStrife.setDisplayEffect(3);
					_sealOfStrife.broadcastPacket(new MagicSkillUse(_sealOfStrife, _sealOfStrife, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 2, 10000, 0));
					_minionSpawns.forEach(minion ->
					{
						if (minion != null)
						{
							CRY_OF_HOLY_WAR.getSkill().applyEffects(minion, minion);
						}
					});
					BOSS_ZONE.getPlayersInside().forEach(player -> STIGMA_OF_MARTYR.getSkill().applyEffects(player, player));
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_STRIFE_ACTIVATES_AND_ENORMOUS_POWER_BEGINS_TO_FLOW_OUT, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 20000, 120000);
				_avariceCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfAvarice.setDisplayEffect(3);
					_sealOfAvarice.broadcastPacket(new MagicSkillUse(_sealOfAvarice, _sealOfAvarice, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 3, 10000, 0));
					_minionSpawns.forEach(minion ->
					{
						if (minion != null)
						{
							UNDEAD_CREATURE.getSkill().applyEffects(minion, minion);
						}
					});
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_AVARICE_ACTIVATES_AND_ENORMOUS_POWER_BEGINS_TO_FLOW_OUT, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 30000, 120000);
				_punishmentCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfPunishment.setDisplayEffect(3);
					_sealOfPunishment.broadcastPacket(new MagicSkillUse(_sealOfPunishment, _sealOfPunishment, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 4, 10000, 0));
					_minionSpawns.forEach(minion ->
					{
						if (minion != null)
						{
							ETINA_DIVINE_PUNISHMENT.getSkill().applyEffects(minion, minion);
						}
					});
					BOSS_ZONE.getPlayersInside().forEach(player -> ETINA_OBSERVATION.getSkill().applyEffects(player, player));
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_PUNISHMENT_ACTIVATES_AND_ENORMOUS_POWER_BEGINS_TO_FLOW_OUT, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 40000, 120000);
				_awakeningCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfAwakening.setDisplayEffect(3);
					_sealOfAwakening.broadcastPacket(new MagicSkillUse(_sealOfAwakening, _sealOfAwakening, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 5, 10000, 0));
					_minionSpawns.forEach(minion ->
					{
						if (minion != null)
						{
							RAGE_OF_THE_OPEN_EYE.getSkill().applyEffects(minion, minion);
						}
					});
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_AWAKENING_ACTIVATES_AND_ENORMOUS_POWER_BEGINS_TO_FLOW_OUT, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 50000, 120000);
				_calamityCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfCalamity.setDisplayEffect(3);
					// TODO: Seal of Calamity skill cast.
					_sealOfCalamity.broadcastPacket(new MagicSkillUse(_sealOfCalamity, _sealOfCalamity, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 6, 10000, 0));
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_CALAMITY_ACTIVATES_AND_ENORMOUS_POWER_BEGINS_TO_FLOW_OUT, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 60000, 120000);
				_destructionCastTask = ThreadPool.scheduleAtFixedRate(() ->
				{
					_sealOfDestruction.setDisplayEffect(3);
					// TODO: Seal of Destruction skill cast.
					_sealOfDestruction.broadcastPacket(new MagicSkillUse(_sealOfDestruction, _sealOfDestruction, CALL_OF_SEVEN_SIGNS_SEAL_N.getSkillId(), 7, 10000, 0));
					BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.THE_SEAL_OF_DESTRUCTION_ACTIVATES_AND_THE_ENTIRE_ETINA_GREAT_TEMPLE_IS_NOW_UNDER_THE_INFLUENCE_OF_THE_SEAL, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				}, 70000, 120000);
			}
			else if ((npc.getCurrentHp() <= (npc.getMaxHp() * 0.15)) && !_etina15)
			{
				_etina15 = true;
				for (SpawnHolder spawn : SPAWNS_MINIONS)
				{
					_minionSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
				// TODO: decrease etina and seals power
				BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.ETIS_VAN_ETINA_AND_THE_POWER_OF_SEVEN_SEALS_ARE_WEAKENING, ExShowScreenMessage.TOP_CENTER, 7000, true)));
			}
		}
		return super.onAttack(npc, attacker, damage, isSummon);
	}
	
	@Override
	public String onEnterZone(Creature character, ZoneType zone)
	{
		if (zone.getId() == ZONE_ID)
		{
			if (_collapseTask != null)
			{
				_collapseTask.cancel(true);
				_collapseTask = null;
			}
			if ((GrandBossManager.getInstance().getBossStatus(ETIS_VAN_ETINA1) == WAITING) && !_spawned)
			{
				_spawned = true;
				_spawns.clear();
				_barricadeSpawns.clear();
				for (SpawnHolder spawn : SPAWNS_CORRIDOR)
				{
					_spawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
				for (SpawnHolder spawn : SPAWNS_BARRICADE)
				{
					_barricadeSpawns.add(addSpawn(spawn.getNpcId(), spawn.getLocation()));
				}
				_paragon = (Monster) addSpawn(PARAGON, PARAGON_LOC);
				_paragon.setInvul(true);
				BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.YOU_CAN_T_DEFEAT_PARAGON_WHILE_PARAGON_S_MINIONS_ARE_ALIVE, ExShowScreenMessage.TOP_CENTER, 7000, true)));
				GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, FIGHTING);
				_lastAction = Chronos.currentTimeMillis();
				startQuestTimer("check_activity_task", 60000, null, null);
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	@Override
	public String onExitZone(Creature character, ZoneType zone)
	{
		if (zone.getId() == ZONE_ID)
		{
			if (zone.getPlayersInside().isEmpty())
			{
				_collapseTask = ThreadPool.schedule(() -> Clean(), 900000);
			}
		}
		return super.onExitZone(character, zone);
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isPet)
	{
		if (npc.getId() == ETIS_VAN_ETINA1)
		{
			BOSS_ZONE.getPlayersInside().forEach(player ->
			{
				playMovie(player, Movie.SC_ETIS_VAN_ETINA_TRANS);
			});
			startQuestTimer("spawnTransformedEtina", 15000, null, null);
		}
		else if (npc.getId() == ETIS_VAN_ETINA2)
		{
			notifyEvent("cancel_timers", null, null);
			BOSS_ZONE.getPlayersInside().forEach(player ->
			{
				playMovie(player, Movie.SC_ETIS_VAN_ETINA_ENDING);
			});
			GrandBossManager.getInstance().setBossStatus(ETIS_VAN_ETINA1, DEAD);
			final long respawnTime = (Config.ETINA_SPAWN_INTERVAL + getRandom(-Config.ETINA_SPAWN_RANDOM, Config.ETINA_SPAWN_RANDOM)) * 3600000;
			final StatSet info = GrandBossManager.getInstance().getStatSet(ETIS_VAN_ETINA1);
			info.set("respawn_time", Chronos.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatSet(ETIS_VAN_ETINA1, info);
			
			startQuestTimer("unlock_etina", respawnTime, null, null);
			startQuestTimer("end_etina", 900000, null, null);
		}
		else if (CommonUtil.contains(CORRIDOR_MOBS, npc.getId()))
		{
			_spawns.remove(npc);
			if (_spawns.isEmpty())
			{
				_paragon.setInvul(false);
				BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.PARAGON_IS_NO_LONGER_INVINCIBLE, ExShowScreenMessage.TOP_CENTER, 7000, true)));
			}
		}
		else if (npc.getId() == PARAGON)
		{
			openDoor(DOOR1, 0);
			openDoor(DOOR2, 0);
			startQuestTimer("openInnerDoor", 30000, null, null);
		}
		else if (npc.getId() == KAIN_VAN_HALTER)
		{
			_minionSpawns.forEach(minion ->
			{
				if (minion != null)
				{
					minion.doDie(null);
				}
			});
			_etina.setInvul(false);
			_etina.setImmobilized(false);
			_etina.setTargetable(true);
			BOSS_ZONE.getPlayersInside().forEach(player -> player.sendPacket(new ExShowScreenMessage(NpcStringId.ETIS_VAN_ETINA_APPROACHES, ExShowScreenMessage.TOP_CENTER, 7000, true)));
			_kain.deleteMe();
			BOSS_ZONE.getPlayersInside().forEach(player ->
			{
				playMovie(player, Movie.SC_KAIN_BOSS_ENDING);
			});
		}
		return super.onKill(npc, killer, isPet);
	}
	
	public static void main(String[] args)
	{
		new EtisVanEtina();
	}
}
