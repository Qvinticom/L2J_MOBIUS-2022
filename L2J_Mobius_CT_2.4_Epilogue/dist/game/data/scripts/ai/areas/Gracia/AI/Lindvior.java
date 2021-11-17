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
package ai.areas.Gracia.AI;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.data.SpawnTable;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.NpcStringId;

import ai.AbstractNpcAI;

/**
 * Lindvior Scene AI.
 * @author nonom
 */
public class Lindvior extends AbstractNpcAI
{
	private static final int LINDVIOR_CAMERA = 18669;
	private static final int TOMARIS = 32552;
	private static final int ARTIUS = 32559;
	
	private static final int RESET_HOUR = 18;
	private static final int RESET_MIN = 58;
	private static final int RESET_DAY_1 = Calendar.TUESDAY;
	private static final int RESET_DAY_2 = Calendar.FRIDAY;
	
	private static boolean ALT_MODE = false;
	private static int ALT_MODE_MIN = 60; // schedule delay in minutes if ALT_MODE enabled
	
	public Lindvior()
	{
		scheduleNextLindviorVisit();
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "tomaris_shout1":
			{
				npc.broadcastSay(ChatType.NPC_SHOUT, NpcStringId.HUH_THE_SKY_LOOKS_FUNNY_WHAT_S_THAT);
				break;
			}
			case "artius_shout":
			{
				npc.broadcastSay(ChatType.NPC_SHOUT, NpcStringId.A_POWERFUL_SUBORDINATE_IS_BEING_HELD_BY_THE_BARRIER_ORB_THIS_REACTION_MEANS);
				break;
			}
			case "tomaris_shout2":
			{
				npc.broadcastSay(ChatType.NPC_SHOUT, NpcStringId.BE_CAREFUL_SOMETHING_S_COMING);
				break;
			}
			case "lindvior_scene":
			{
				if (npc != null)
				{
					World.getInstance().forEachVisibleObjectInRange(npc, Player.class, 4000, pl ->
					{
						if ((pl.getZ() >= 1100) && (pl.getZ() <= 3100))
						{
							playMovie(pl, Movie.SC_LINDVIOR);
						}
					});
				}
				break;
			}
			case "start":
			{
				final Npc lindviorCamera = SpawnTable.getInstance().getAnySpawn(LINDVIOR_CAMERA).getLastSpawn();
				final Npc tomaris = SpawnTable.getInstance().getAnySpawn(TOMARIS).getLastSpawn();
				final Npc artius = SpawnTable.getInstance().getAnySpawn(ARTIUS).getLastSpawn();
				startQuestTimer("tomaris_shout1", 1000, tomaris, null);
				startQuestTimer("artius_shout", 60000, artius, null);
				startQuestTimer("tomaris_shout2", 90000, tomaris, null);
				startQuestTimer("lindvior_scene", 120000, lindviorCamera, null);
				scheduleNextLindviorVisit();
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	public void scheduleNextLindviorVisit()
	{
		final long delay = (ALT_MODE) ? ALT_MODE_MIN * 60000 : scheduleNextLindviorDate();
		startQuestTimer("start", delay, null, null);
	}
	
	protected long scheduleNextLindviorDate()
	{
		final GregorianCalendar date = new GregorianCalendar();
		date.set(Calendar.MINUTE, RESET_MIN);
		date.set(Calendar.HOUR_OF_DAY, RESET_HOUR);
		if (Chronos.currentTimeMillis() >= date.getTimeInMillis())
		{
			date.add(Calendar.DAY_OF_WEEK, 1);
		}
		
		final int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek <= RESET_DAY_1)
		{
			date.add(Calendar.DAY_OF_WEEK, RESET_DAY_1 - dayOfWeek);
		}
		else if (dayOfWeek <= RESET_DAY_2)
		{
			date.add(Calendar.DAY_OF_WEEK, RESET_DAY_2 - dayOfWeek);
		}
		else
		{
			date.add(Calendar.DAY_OF_WEEK, 1 + RESET_DAY_1);
		}
		return date.getTimeInMillis() - Chronos.currentTimeMillis();
	}
}