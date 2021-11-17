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
package ai.areas.DwarvenVillage.Toma;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class Toma extends AbstractNpcAI
{
	// NPC
	private static final int TOMA = 30556;
	// Locations
	private static final Location[] LOCATIONS =
	{
		new Location(151680, -174891, -1782),
		new Location(154153, -220105, -3402),
		new Location(178834, -184336, -355, 41400)
	};
	// Misc
	private static final int TELEPORT_DELAY = 1800000; // 30 minutes
	private static Npc _toma;
	
	private Toma()
	{
		addFirstTalkId(TOMA);
		onAdvEvent("RESPAWN_TOMA", null, null);
		startQuestTimer("RESPAWN_TOMA", TELEPORT_DELAY, null, null, true);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("RESPAWN_TOMA"))
		{
			if (_toma != null)
			{
				_toma.deleteMe();
			}
			_toma = addSpawn(TOMA, getRandomEntry(LOCATIONS), false, TELEPORT_DELAY);
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		return "30556.htm";
	}
	
	public static void main(String[] args)
	{
		new Toma();
	}
}
