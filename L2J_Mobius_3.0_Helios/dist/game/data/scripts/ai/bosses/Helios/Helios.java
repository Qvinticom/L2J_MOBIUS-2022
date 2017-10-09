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

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.instancemanager.ZoneManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.StatsSet;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.zone.type.L2NoSummonFriendZone;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Helios extends AbstractNpcAI
{
	// NPC
	private static final int HELIOS = 29305;
	// Location
	private static final Location HELIOS_SPAWN_LOC = new Location(92771, 161909, 3494, 38329);
	// Zone
	private final static int ZONE_ID = 210109;
	// Status
	private static final int ALIVE = 0;
	private static final int WAITING = 1;
	private static final int DEAD = 3;
	// Misc
	private static final int HELIOS_RAID_DURATION = 5; // hours
	private static L2Npc bossInstance;
	protected L2NoSummonFriendZone bossZone;
	
	private Helios()
	{
		addAttackId(HELIOS);
		addKillId(HELIOS);
		// Unlock
		final StatsSet info = GrandBossManager.getInstance().getStatsSet(HELIOS);
		final long time = info.getLong("respawn_time") - System.currentTimeMillis();
		if (time > 0)
		{
			startQuestTimer("unlock_helios", time, null, null);
		}
		// Zone
		bossZone = ZoneManager.getInstance().getZoneById(ZONE_ID, L2NoSummonFriendZone.class);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "unlock_helios":
			{
				GrandBossManager.getInstance().setBossStatus(HELIOS, ALIVE);
				break;
			}
			case "beginning":
			{
				GrandBossManager.getInstance().setBossStatus(HELIOS, WAITING);
				bossInstance = addSpawn(HELIOS, HELIOS_SPAWN_LOC.getX(), HELIOS_SPAWN_LOC.getY(), HELIOS_SPAWN_LOC.getZ(), HELIOS_SPAWN_LOC.getHeading(), false, 0, false);
				startQuestTimer("resetRaid", HELIOS_RAID_DURATION * 60 * 60 * 1000, bossInstance, null);
				break;
			}
			case "resetRaid":
			{
				final int status = GrandBossManager.getInstance().getBossStatus(HELIOS);
				if ((status > ALIVE) && (status < DEAD))
				{
					GrandBossManager.getInstance().setBossStatus(HELIOS, ALIVE);
					npc.deleteMe();
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		if (npc.getId() == HELIOS)
		{
			GrandBossManager.getInstance().setBossStatus(HELIOS, DEAD);
			final long respawnTime = (Config.HELIOS_SPAWN_INTERVAL + getRandom(-Config.HELIOS_SPAWN_RANDOM, Config.HELIOS_SPAWN_RANDOM)) * 3600000;
			final StatsSet info = GrandBossManager.getInstance().getStatsSet(HELIOS);
			info.set("respawn_time", System.currentTimeMillis() + respawnTime);
			GrandBossManager.getInstance().setStatsSet(HELIOS, info);
			startQuestTimer("unlock_helios", respawnTime, null, null);
			bossZone.broadcastPacket(new ExShowScreenMessage(NpcStringId.HELIOS_DEFEATED_TAKES_FLIGHT_DEEP_IN_TO_THE_SUPERION_FORT_HIS_THRONE_IS_RENDERED_INACTIVE, ExShowScreenMessage.TOP_CENTER, 10000, true));
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Helios();
	}
}
