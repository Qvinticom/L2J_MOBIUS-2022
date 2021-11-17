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
package ai.areas.CrumaTower;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Cruma Tower teleport AI.
 * @author Plim
 */
public class CrumaTower extends AbstractNpcAI
{
	// NPC
	private static final int MOZELLA = 30483;
	// Locations
	private static final Location TELEPORT_LOC1 = new Location(17776, 113968, -11671);
	private static final Location TELEPORT_LOC2 = new Location(17680, 113968, -11671);
	// Misc
	private static final int MAX_LEVEL = 55;
	
	private CrumaTower()
	{
		addFirstTalkId(MOZELLA);
		addStartNpc(MOZELLA);
		addTalkId(MOZELLA);
	}
	
	@Override
	public String onTalk(Npc npc, Player talker)
	{
		if (talker.getLevel() <= MAX_LEVEL)
		{
			talker.teleToLocation(getRandomBoolean() ? TELEPORT_LOC1 : TELEPORT_LOC2);
			return null;
		}
		return "30483-1.html";
	}
	
	public static void main(String[] args)
	{
		new CrumaTower();
	}
}