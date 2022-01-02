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
package ai.areas.RaidersCrossroads.NervasTemporaryPrison;

import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * @author Index
 */
public class NervasTemporaryPrison extends AbstractNpcAI
{
	// NPCs
	private static final int KAYSEN = 19458;
	private static final int NERVAS_TEMPORARY_PRISON = 19459;
	// Locations
	private static final Location[] SPAWN_LOCATIONS =
	{
		new Location(10595, -136216, -1192),
		new Location(11924, -141102, -592),
		new Location(18263, -137084, -896),
		new Location(19991, -142252, -576),
		new Location(22752, -139032, -744),
		new Location(23220, -146000, -464),
		new Location(6516, -139680, -656),
		new Location(8555, -146514, -312),
	};
	// Item
	private static final int NERVA_KEY = 36665;
	
	private NervasTemporaryPrison()
	{
		addStartNpc(NERVAS_TEMPORARY_PRISON);
		addFirstTalkId(NERVAS_TEMPORARY_PRISON);
		addTalkId(NERVAS_TEMPORARY_PRISON);
		addSpawnId(NERVAS_TEMPORARY_PRISON);
		
		for (Location location : SPAWN_LOCATIONS)
		{
			addSpawn(NERVAS_TEMPORARY_PRISON, location);
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "OPEN":
			{
				if (hasQuestItems(player, NERVA_KEY))
				{
					for (Door door : World.getInstance().getVisibleObjectsInRange(npc, Door.class, Npc.INTERACTION_DISTANCE))
					{
						door.openMe();
					}
					
					for (Npc nearby : World.getInstance().getVisibleObjectsInRange(npc, Npc.class, Npc.INTERACTION_DISTANCE))
					{
						if (nearby.getId() == KAYSEN)
						{
							nearby.broadcastSay(ChatType.NPC_GENERAL, NpcStringId.YOU_TOOK_DOWN_THE_NERVA_ORCS_AND_GOT_THEIR_TEMPORARY_PRISON_KEY);
							break;
						}
					}
					
					takeItems(player, NERVA_KEY, 1);
					
					npc.deleteMe();
					startQuestTimer("PRISON_RESPAWN", 3600000, npc, null);
				}
				else
				{
					return "19459-no.html";
				}
				break;
			}
			case "PRISON_RESPAWN":
			{
				addSpawn(NERVAS_TEMPORARY_PRISON, npc);
				break;
			}
		}
		return null;
	}
	
	@Override
	public String onSpawn(Npc npc)
	{
		for (Door door : World.getInstance().getVisibleObjectsInRange(npc, Door.class, Npc.INTERACTION_DISTANCE))
		{
			door.closeMe();
		}
		return super.onSpawn(npc);
	}
	
	public static void main(String[] args)
	{
		new NervasTemporaryPrison();
	}
}