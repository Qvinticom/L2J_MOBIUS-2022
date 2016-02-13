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
package instances.IncubatorOfEvil;

import com.l2jmobius.gameserver.instancemanager.InstanceManager;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.instancezone.InstanceWorld;
import com.l2jmobius.gameserver.model.quest.QuestState;

import instances.AbstractInstance;
import quests.Q10341_DayOfDestinyHumansFate.Q10341_DayOfDestinyHumansFate;
import quests.Q10342_DayOfDestinyElvenFate.Q10342_DayOfDestinyElvenFate;
import quests.Q10343_DayOfDestinyDarkElfsFate.Q10343_DayOfDestinyDarkElfsFate;
import quests.Q10344_DayOfDestinyOrcsFate.Q10344_DayOfDestinyOrcsFate;
import quests.Q10345_DayOfDestinyDwarfsFate.Q10345_DayOfDestinyDwarfsFate;
import quests.Q10346_DayOfDestinyKamaelsFate.Q10346_DayOfDestinyKamaelsFate;

/**
 * Incubator of Evil Instance Zone.<br>
 * TODO: Implement this!
 * @author Mobius
 */
public final class IncubatorOfEvil extends AbstractInstance
{
	// NPCs
	private static final int OFFICER = 19155;
	// Locations
	private static final Location START_LOC = new Location(56180, -172898, -7952);
	@SuppressWarnings("unused")
	private static final Location FIGHT_LOC = new Location(56177, -175627, -7952);
	@SuppressWarnings("unused")
	private static final Location EXIT_LOC = new Location(185798, 20400, -3345);
	// Misc
	private static final int TEMPLATE_ID = 185;
	
	protected class IOE extends InstanceWorld
	{
	}
	
	public IncubatorOfEvil()
	{
		super(IncubatorOfEvil.class.getSimpleName());
		addStartNpc(OFFICER);
		addTalkId(OFFICER);
		addFirstTalkId(OFFICER);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("enter_instance"))
		{
			if (getPlayerQuestState(player) == null)
			{
				return null;
			}
			enterInstance(player, new IOE(), "IncubatorOfEvil.xml", TEMPLATE_ID);
			return null;
		}
		
		final InstanceWorld tmpworld = InstanceManager.getInstance().getPlayerWorld(player);
		if ((tmpworld == null) || !(tmpworld instanceof IOE))
		{
			return null;
		}
		@SuppressWarnings("unused")
		final IOE world = (IOE) tmpworld;
		
		switch (event)
		{
			
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public void onEnterInstance(L2PcInstance player, InstanceWorld world, boolean firstEntrance)
	{
		if (firstEntrance)
		{
			world.setStatus(1);
			world.addAllowed(player.getObjectId());
		}
		teleportPlayer(player, START_LOC, world.getInstanceId());
	}
	
	private QuestState getPlayerQuestState(L2PcInstance player)
	{
		QuestState qs = null;
		switch (player.getRace())
		{
			case HUMAN:
			{
				qs = player.getQuestState(Q10341_DayOfDestinyHumansFate.class.getSimpleName());
				break;
			}
			case ELF:
			{
				qs = player.getQuestState(Q10342_DayOfDestinyElvenFate.class.getSimpleName());
				break;
			}
			case DARK_ELF:
			{
				qs = player.getQuestState(Q10343_DayOfDestinyDarkElfsFate.class.getSimpleName());
				break;
			}
			case ORC:
			{
				qs = player.getQuestState(Q10344_DayOfDestinyOrcsFate.class.getSimpleName());
				break;
			}
			case DWARF:
			{
				qs = player.getQuestState(Q10345_DayOfDestinyDwarfsFate.class.getSimpleName());
				break;
			}
			case KAMAEL:
			{
				qs = player.getQuestState(Q10346_DayOfDestinyKamaelsFate.class.getSimpleName());
				break;
			}
		}
		return qs;
	}
}