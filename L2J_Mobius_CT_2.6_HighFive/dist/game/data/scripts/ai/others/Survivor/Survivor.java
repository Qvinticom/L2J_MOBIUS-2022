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
package ai.others.Survivor;

import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;

import ai.AbstractNpcAI;

/**
 * Gracia Survivor teleport AI.<br>
 * Original Jython script by Kerberos.
 * @author Plim
 */
public class Survivor extends AbstractNpcAI
{
	// NPC
	private static final int SURVIVOR = 32632;
	// Misc
	private static final int MIN_LEVEL = 75;
	// Location
	private static final Location TELEPORT = new Location(-149406, 255247, -80);
	
	private Survivor()
	{
		addStartNpc(SURVIVOR);
		addTalkId(SURVIVOR);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if ("32632-2.htm".equals(event))
		{
			if (player.getLevel() < MIN_LEVEL)
			{
				return "32632-3.htm";
			}
			else if (player.getAdena() < 150000)
			{
				return event;
			}
			else
			{
				takeItems(player, Inventory.ADENA_ID, 150000);
				player.teleToLocation(TELEPORT);
				return null;
			}
		}
		return event;
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		return "32632-1.htm";
	}
	
	public static void main(String[] args)
	{
		new Survivor();
	}
}
