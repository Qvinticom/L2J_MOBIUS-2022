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
package ai.areas.EnchantedValley.Celestiel;

import org.l2jmobius.gameserver.enums.Faction;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Celestiel AI
 * @author Gigi
 * @date 2017-06-13 - [20:09:34]
 */
public class Celestiel extends AbstractNpcAI
{
	// NPC
	private static final int CELESTIEL = 34234;
	// Teleports
	private static final Location SOUTH_LOCATION = new Location(110815, 59655, -3720);
	private static final Location NORTH_LOCATION = new Location(124040, 43970, -3720);
	
	private static final String[] CELESTIEL_VOICE =
	{
		"Npcdialog1.selestiel_faction_1",
		"Npcdialog1.selestiel_faction_2"
	};
	
	private Celestiel()
	{
		addTalkId(CELESTIEL);
		addFirstTalkId(CELESTIEL);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "34234-1.html":
			case "34234-2.html":
			case "34234-3.html":
			case "34234-4.html":
			{
				htmltext = event;
				break;
			}
			case "south":
			{
				if (player.getFactionLevel(Faction.MOTHER_TREE_GUARDIANS) < 2)
				{
					htmltext = "34234-5.html";
				}
				else
				{
					player.teleToLocation(SOUTH_LOCATION);
				}
				break;
			}
			case "north":
			{
				if (player.getFactionLevel(Faction.MOTHER_TREE_GUARDIANS) < 2)
				{
					htmltext = "34234-5.html";
				}
				else
				{
					player.teleToLocation(NORTH_LOCATION);
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		player.sendPacket(new PlaySound(3, CELESTIEL_VOICE[getRandom(2)], 0, 0, 0, 0, 0));
		return "34234.html";
	}
	
	public static void main(String[] args)
	{
		new Celestiel();
	}
}
