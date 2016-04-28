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
package ai.npc.Teleports.GiantServant;

import java.util.HashMap;
import java.util.Map;

import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;

import ai.AbstractNpcAI;

/**
 * Hermuncus Minion AI.
 * @author ChaosPaladin
 */
final class GiantServant extends AbstractNpcAI
{
	// NPCs
	private static final int[] NPCs =
	{
		33560,
		33561,
		33562,
		33563,
		33564,
		33565,
		33566,
		33567,
		33568,
		33569,
		33747,
		33779,
	};
	// Locations
	private static final Map<String, Location> SERVANT_TELEPORTS = new HashMap<>();
	
	static
	{
		SERVANT_TELEPORTS.put("SeedOfAnnihilation", new Location(-178445, 154072, 2568)); // 1010721 Seed of Annihilation (Lv. 85)
		SERVANT_TELEPORTS.put("BloodySwampland", new Location(-15826, 30477, -3616)); // 1010722 Bloody Swampland (Lv. 85)
		SERVANT_TELEPORTS.put("RuinsOfYeSagira", new Location(-116021, 236167, -3088)); // 1010723 Ruins of Ye Sagira (Lv. 85)
		SERVANT_TELEPORTS.put("AncientCityArcan", new Location(207688, 84720, -1144)); // 1010724 Ancient City Arcan
		SERVANT_TELEPORTS.put("GardenOfGenesis", new Location(207129, 111132, -2040)); // 1010725 Garden of Genesis (Lv. 90)
		SERVANT_TELEPORTS.put("FairySettlement", new Location(214432, 79587, 824)); // 1010726 Fairy Settlement (Lv. 90)
		SERVANT_TELEPORTS.put("SealOfShilen", new Location(187383, 20498, -3584)); // 1010727 Seal of Shilen (Lv. 95)
		SERVANT_TELEPORTS.put("OrbisTempleEntrance", new Location(198703, 86034, -192)); // 1010728 Orbis Temple Entrance (Lv. 95)
		SERVANT_TELEPORTS.put("Parnassus", new Location(149358, 172479, -952)); // 1010729 Parnassus (Lv. 97)
		SERVANT_TELEPORTS.put("GuilloutineFortress", new Location(44725, 146026, -3512)); // 1010114 Guilloutine Fortress (Lv. 95)
	}
	
	private GiantServant()
	{
		super(GiantServant.class.getSimpleName(), "ai/npc/Teleports");
		addStartNpc(NPCs);
		addFirstTalkId(NPCs);
		addTalkId(NPCs);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "teleport_request":
			{
				if (player.isAwaken())
				{
					if ((player.getLevel() < 90) && (player.getLevel() >= 85))
					{
						htmltext = "awake_gatekeeper85.htm";
					}
					if (player.getLevel() >= 90)
					{
						htmltext = "awake_gatekeeper90.htm";
					}
				}
				else
				{
					htmltext = "non_awakened.htm";
				}
				break;
			}
			case "SeedOfAnnihilation":
			case "BloodySwampland":
			case "RuinsOfYeSagira":
			case "AncientCityArcan":
			case "GardenOfGenesis":
			case "FairySettlement":
			case "SealOfShilen":
			case "OrbisTempleEntrance":
			case "Parnassus":
			case "GuilloutineFortress":
			{
				player.teleToLocation(SERVANT_TELEPORTS.get(event));
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "first_talk.htm";
	}
	
	public static void main(String[] args)
	{
		new GiantServant();
	}
}