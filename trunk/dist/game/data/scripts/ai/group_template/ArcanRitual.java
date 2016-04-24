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
package ai.group_template;

import java.util.ArrayList;

import com.l2jmobius.gameserver.ThreadPoolManager;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Character;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.quest.Quest;
import com.l2jmobius.gameserver.model.zone.L2ZoneType;
import com.l2jmobius.gameserver.model.zone.type.L2PeaceZone;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;
import com.l2jmobius.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jmobius.gameserver.network.serverpackets.OnEventTrigger;

/**
 * @author Mobius
 */
final class ArcanRitual extends Quest
{
	private static final int BLUE_TRIGGER = 262001;
	private static final int RED_TRIGGER = 262003;
	private static final int ARCAN_RITUAL_INTERVAL = 30 * 60000; // 30 minutes
	private static final boolean ENABLED = true; // In case we want to disable spawning NPCs
	private static final Location ARCAN_TOWN_LOC = new Location(207096, 88696, -1129);
	// @formatter:off
	static final int[][] RITUAL_NPCS =
	{
		{32908, 205942, 88712, -1104, 65004},
		{32908, 205945, 88609, -1104, 824},
		{32908, 205967, 88970, -1104, 63984},
		{32908, 205999, 89071, -1104, 61656},
		{32908, 206000, 88351, -1104, 1332},
		{32908, 206028, 88246, -1104, 4776},
		{32908, 206104, 89313, -1104, 60448},
		{32908, 206168, 89397, -1104, 57736},
		{32908, 206171, 88023, -1104, 6108},
		{32908, 206228, 87941, -1104, 9516},
		{32908, 206352, 89593, -1104, 56684},
		{32908, 206418, 87762, -1104, 9748},
		{32908, 206424, 89647, -1104, 54504},
		{32908, 206509, 87706, -1104, 12268},
		{32908, 206737, 87599, -1104, 12008},
		{32908, 206846, 87561, -1104, 15824},
		{32908, 207101, 87543, -1104, 15556},
		{32908, 207193, 87540, -1104, 18256},
		{32908, 207375, 89840, -1104, 47260},
		{32908, 207464, 87594, -1104, 18800},
		{32908, 207476, 89812, -1104, 44388},
		{32908, 207552, 87632, -1104, 21740},
		{32908, 207702, 89708, -1104, 44292},
		{32908, 207786, 87751, -1104, 22544},
		{32908, 207800, 89647, -1104, 41272},
		{32908, 207867, 87820, -1104, 24788},
		{32908, 207992, 89470, -1104, 40724},
		{32908, 208052, 88008, -1104, 25440},
		{32908, 208064, 89381, -1104, 37436},
		{32908, 208113, 88097, -1104, 27416},
		{32908, 208177, 89159, -1104, 37128},
		{32908, 208207, 88336, -1104, 28092},
		{32908, 208220, 89052, -1104, 34956},
		{32908, 208238, 88440, -1104, 32300},
		{32908, 208262, 88686, -1104, 31772},
		{32908, 208262, 88804, -1104, 34384},
		{32909, 207327, 87556, -1104, 20068},
		{33093, 206327, 88723, -1120, 12},
		{33093, 206332, 88619, -1120, 1048},
		{33093, 206348, 88511, -1120, 2112},
		{33093, 206348, 88832, -1120, 65240},
		{33093, 206387, 88958, -1120, 62432},
		{33093, 206398, 88387, -1120, 3440},
		{33093, 206432, 89072, -1120, 61240},
		{33093, 206469, 88270, -1120, 6076},
		{33093, 206496, 89184, -1120, 59428},
		{33093, 206546, 88189, -1120, 7984},
		{33093, 206592, 89280, -1120, 57492},
		{33093, 206640, 88112, -1120, 10032},
		{33093, 206656, 89328, -1120, 55888},
		{33093, 206736, 88048, -1120, 9652},
		{33093, 206783, 89414, -1120, 53180},
		{33093, 206848, 87984, -1120, 11096},
		{33093, 206894, 89443, -1120, 52140},
		{33093, 207020, 89483, -1120, 51980},
		{33093, 207136, 89488, -1120, 48660},
		{33093, 207263, 89460, -1120, 47644},
		{33093, 207360, 89428, -1120, 45772},
		{33093, 207408, 88016, -1120, 20748},
		{33093, 207488, 89376, -1120, 43204},
		{33093, 207520, 88080, -1120, 22588},
		{33093, 207584, 89312, -1120, 42744},
		{33093, 207599, 88149, -1120, 25132},
		{33093, 207680, 89216, -1120, 41004},
		{33093, 207692, 88226, -1120, 25712},
		{33093, 207744, 89136, -1120, 38748},
		{33093, 207781, 88313, -1120, 28304},
		{33093, 207808, 89024, -1120, 37420},
		{33093, 207824, 88448, -1120, 30616},
		{33093, 207851, 88540, -1120, 30468},
		{33093, 207853, 88919, -1120, 36060},
		{33093, 207867, 88672, -1120, 31864},
		{33093, 207871, 88791, -1120, 34168},
		{33343, 207128, 88132, -1120, 48772},
		{33361, 207008, 88742, -1128, 26824},
		{33361, 207020, 88639, -1128, 40004},
		{33361, 207111, 88797, -1128, 14544},
		{33361, 207143, 88616, -1128, 53192},
		{33361, 207191, 88702, -1128, 0},
		{33363, 206704, 88608, -1128, 35608},
		{33363, 206704, 88816, -1128, 29976},
		{33363, 206816, 88400, -1128, 40564},
		{33363, 206816, 88992, -1128, 26844},
		{33363, 207008, 88288, -1120, 45576},
		{33363, 207008, 89104, -1120, 17652},
		{33363, 207200, 89120, -1120, 14728},
		{33363, 207208, 88289, -1120, 52112},
		{33363, 207392, 88400, -1128, 58228},
		{33363, 207408, 88992, -1120, 7804},
		{33363, 207504, 88592, -1128, 62012},
		{33363, 207504, 88784, -1128, 3128}
	};
	// @formatter:on
	private static L2ZoneType arcanZone = null;
	static int ritualStage;
	static ArrayList<L2Npc> ritualSpawns = new ArrayList<>();
	
	public ArcanRitual()
	{
		super(-1, "Arcan Ritual", "Arcan Ritual");
		for (L2ZoneType zone : L2World.getInstance().getRegion(ARCAN_TOWN_LOC).getZones())
		{
			if (zone instanceof L2PeaceZone)
			{
				arcanZone = zone;
				break;
			}
		}
		addEnterZoneId(arcanZone.getId());
		ritualStage = BLUE_TRIGGER;
		if (ENABLED)
		{
			ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new ChangeStage(), ARCAN_RITUAL_INTERVAL, ARCAN_RITUAL_INTERVAL);
		}
	}
	
	@Override
	public String onEnterZone(L2Character character, L2ZoneType zone)
	{
		if (character.isPlayer())
		{
			if (ritualStage == BLUE_TRIGGER)
			{
				character.sendPacket(new OnEventTrigger(BLUE_TRIGGER, true));
				character.sendPacket(new OnEventTrigger(RED_TRIGGER, false));
			}
			else
			{
				character.sendPacket(new OnEventTrigger(RED_TRIGGER, true));
				character.sendPacket(new OnEventTrigger(BLUE_TRIGGER, false));
			}
		}
		return super.onEnterZone(character, zone);
	}
	
	private class ChangeStage implements Runnable
	{
		public ChangeStage()
		{
		}
		
		@Override
		public void run()
		{
			if (ritualStage == RED_TRIGGER)
			{
				ritualStage = BLUE_TRIGGER;
				broadcastPacket(BLUE_TRIGGER, true, false);
				broadcastPacket(RED_TRIGGER, false, false);
				for (L2Npc spawn : ritualSpawns)
				{
					if (spawn != null)
					{
						spawn.deleteMe();
					}
				}
				ritualSpawns.clear();
			}
			else
			{
				ritualStage = RED_TRIGGER;
				broadcastPacket(RED_TRIGGER, true, true);
				broadcastPacket(BLUE_TRIGGER, false, false);
				for (int[] spawn : RITUAL_NPCS)
				{
					ritualSpawns.add(addSpawn(spawn[0], spawn[1], spawn[2], spawn[3], spawn[4], false, 0, false, 0));
				}
			}
		}
	}
	
	void broadcastPacket(int triggerId, boolean enabled, boolean message)
	{
		final L2GameServerPacket trigger = new OnEventTrigger(triggerId, enabled);
		for (L2PcInstance player : arcanZone.getPlayersInside())
		{
			player.sendPacket(trigger);
			if (message)
			{
				player.sendPacket(new ExShowScreenMessage(NpcStringId.DARK_POWER_SEEPS_OUT_FROM_THE_MIDDLE_OF_THE_TOWN, ExShowScreenMessage.TOP_CENTER, 5000));
			}
		}
	}
	
	public static void main(String[] args)
	{
		new ArcanRitual();
	}
}