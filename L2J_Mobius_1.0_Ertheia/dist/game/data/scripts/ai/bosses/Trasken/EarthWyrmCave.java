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
package ai.bosses.Trasken;

import com.l2jmobius.gameserver.enums.Movie;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.instancezone.Instance;
import com.l2jmobius.gameserver.network.NpcStringId;
import com.l2jmobius.gameserver.network.serverpackets.ExShowScreenMessage;

import instances.AbstractInstance;

/**
 * Earth Wyrm Cave instance zone. (simple implementation)
 * @Video http://www.youtube.com/watch?v=0Vyu7GJvuBo
 * @author Mobius
 */
public final class EarthWyrmCave extends AbstractInstance
{
	// NPCs
	private static final int DAICHIR = 30537;
	private static final int TRASKEN = 19159;
	// Location
	private static final Location TRASKEN_SPAWN_LOC = new Location(82383, -183527, -9892, 26533);
	// Door
	private static final int DOOR_ID = 22120001;
	// Misc
	private static final int OPEN_DOOR = 5; // minutes
	private static final int TEMPLATE_ID = 192;
	
	public EarthWyrmCave()
	{
		super(TEMPLATE_ID);
		addStartNpc(DAICHIR);
		addTalkId(DAICHIR);
		addKillId(TRASKEN);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				startQuestTimer("OPEN_DOOR", OPEN_DOOR * 60 * 1000, null, player, false);
				break;
			}
			case "OPEN_DOOR":
			{
				if ((player == null) || (player.getInstanceId() == 0))
				{
					return null;
				}
				final Instance world = player.getInstanceWorld();
				world.openCloseDoor(DOOR_ID, true);
				world.broadcastPacket(new ExShowScreenMessage(NpcStringId.ELIMINATE_THOSE_WHO_PROTECT_THE_HEART_OF_THE_EARTH_WYRM, ExShowScreenMessage.TOP_CENTER, 7000));
				startQuestTimer("CLOSE_DOOR", 60000, null, player, false); // close door after a minute
				startQuestTimer("WAIT_TO_CLEAR_MONSTERS", 5000, null, player, false);
				break;
			}
			case "CLOSE_DOOR":
			{
				if ((player == null) || (player.getInstanceId() == 0))
				{
					return null;
				}
				final Instance world = player.getInstanceWorld();
				world.openCloseDoor(DOOR_ID, false);
				break;
			}
			case "WAIT_TO_CLEAR_MONSTERS":
			{
				if ((player == null) || (player.getInstanceId() == 0))
				{
					return null;
				}
				final Instance world = player.getInstanceWorld();
				if (world.getAliveNpcs(L2MonsterInstance.class).isEmpty())
				{
					addSpawn(TRASKEN, TRASKEN_SPAWN_LOC, false, 0, false, player.getInstanceId());
					
					world.broadcastPacket(new ExShowScreenMessage(NpcStringId.FIND_THE_EARTH_WYRM_S_WEAKNESS_TO_DEFEAT_IT, ExShowScreenMessage.TOP_CENTER, 5000));
				}
				else
				{
					startQuestTimer("WAIT_TO_CLEAR_MONSTERS", 5000, null, player, false);
				}
				return null;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(L2Npc npc, L2PcInstance killer, boolean isSummon)
	{
		final Instance instance = npc.getInstanceWorld();
		if (isInInstance(instance))
		{
			switch (npc.getId())
			{
				case TRASKEN:
				{
					npc.deleteMe();
					instance.broadcastPacket(new ExShowScreenMessage(NpcStringId.HEART_OF_EARTH_WYRM_HAS_BEEN_DESTROYED, ExShowScreenMessage.TOP_CENTER, 5000));
					playMovie(instance.getPlayers(), Movie.SC_EARTHWORM_ENDING);
					instance.openCloseDoor(DOOR_ID, true);
					instance.finishInstance();
					break;
				}
			}
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new EarthWyrmCave();
	}
}