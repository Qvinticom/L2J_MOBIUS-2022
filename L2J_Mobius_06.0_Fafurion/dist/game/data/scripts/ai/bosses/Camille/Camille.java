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
package ai.bosses.Camille;

import org.l2jmobius.gameserver.enums.Movie;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.actor.instance.Door;
import org.l2jmobius.gameserver.model.instancezone.Instance;

import instances.AbstractInstance;

/**
 * Camille instance zone.
 * @author Sero
 * @URL https://www.youtube.com/watch?v=jpv9S_xQVrA
 */
public class Camille extends AbstractInstance
{
	// NPCs
	private static final int CAMILLE = 26236;
	private static final int MAMUT = 26243;
	private static final int ISBURG = 26244;
	private static final int TRANSMISSION_UNIT = 34324;
	private static final int ERDA = 34319;
	// Locations
	private static final Location ENTER_LOCATION = new Location(-245768, 147832, 4662);
	private static final Location CAMILLE_LOCATION = new Location(-245752, 150392, 11845);
	// Misc
	private static final int TEMPLATE_ID = 266;
	
	public Camille()
	{
		super(TEMPLATE_ID);
		addStartNpc(ERDA);
		addTalkId(ERDA, TRANSMISSION_UNIT);
		addFirstTalkId(TRANSMISSION_UNIT);
		addKillId(CAMILLE, MAMUT, ISBURG);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		switch (event)
		{
			case "enterInstance":
			{
				enterInstance(player, npc, TEMPLATE_ID);
				final Instance world = player.getInstanceWorld();
				if (isInInstance(world))
				{
					world.getPlayers().forEach(p -> p.teleToLocation(ENTER_LOCATION));
					world.getDoors().forEach(Door::closeMe);
				}
				break;
			}
			case "teleup":
			{
				final Instance world = npc.getInstanceWorld();
				if (isInInstance(world) && (npc.getId() == TRANSMISSION_UNIT))
				{
					world.getPlayers().forEach(p -> p.teleToLocation(CAMILLE_LOCATION));
					world.getDoors().forEach(Door::closeMe);
				}
				break;
			}
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onKill(Npc npc, Player player, boolean isSummon)
	{
		final Instance world = npc.getInstanceWorld();
		if (isInInstance(world))
		{
			switch (npc.getId())
			{
				case MAMUT:
				{
					world.openCloseDoor(world.getTemplateParameters().getInt("firstDoorId"), true);
					world.openCloseDoor(world.getTemplateParameters().getInt("secondDoorId"), true);
					world.setReenterTime();
					break;
				}
				case ISBURG:
				{
					world.spawnGroup("teleport");
					world.setReenterTime();
					break;
				}
				case CAMILLE:
				{
					playMovie(world, Movie.SC_CAMILLE_ENDING);
					world.finishInstance();
					break;
				}
			}
		}
		return super.onKill(npc, player, isSummon);
	}
	
	public static void main(String[] args)
	{
		new Camille();
	}
}