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
package ai.areas.AteliaRefinery;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * @author NviX
 */
public class AteliaRefinery extends AbstractNpcAI
{
	// NPC
	private static final int ATELIA_REFINERY_TELEPORT_DEVICE = 34441;
	// Special Mobs
	private static final int HARKE = 24161;
	private static final int ERGALION = 24162;
	private static final int SPIRA = 24163;
	// Mobs
	private static final int[] MOBS =
	{
		24144, // Death Rogue
		24145, // Death Shooter
		24146, // Death Warrior
		24147, // Death Sorcerer
		24148, // Death Pondus
		24149, // Devil Nightmare
		24150, // Devil Warrior
		24151, // Devil Guardian
		24152, // Devil Sinist
		24153, // Devil Varos
		24154, // Demonic Wizard
		24155, // Demonic Warrior
		24156, // Demonic Archer
		24157, // Demonic Keras
		24158, // Demonic Weiss
		24159, // Atelia Yuyurina
		24160 // Atelia Popobena
	};
	
	private AteliaRefinery()
	{
		addTalkId(ATELIA_REFINERY_TELEPORT_DEVICE);
		addFirstTalkId(ATELIA_REFINERY_TELEPORT_DEVICE);
		addKillId(MOBS);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = null;
		switch (event)
		{
			case "first_area":
			{
				player.teleToLocation(-59493, 52620, -8610);
				break;
			}
			case "second_area":
			{
				player.teleToLocation(-56096, 49688, -8729);
				break;
			}
			case "third_area":
			{
				player.teleToLocation(-56160, 45406, -8847);
				break;
			}
			case "fourth_area":
			{
				player.teleToLocation(-56140, 41067, -8965);
				break;
			}
			case "fifth_area":
			{
				player.teleToLocation(-251728, 178576, -8928);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onKill(Npc npc, Player killer, boolean isSummon)
	{
		int chance = 1;
		if (getRandom(10000) < chance)
		{
			addSpawn(HARKE, npc, false, 300000);
		}
		else if (getRandom(10000) < chance)
		{
			addSpawn(ERGALION, npc, false, 300000);
		}
		else if (getRandom(100000) < chance)
		{
			addSpawn(SPIRA, npc, false, 300000);
		}
		return super.onKill(npc, killer, isSummon);
	}
	
	public static void main(String[] args)
	{
		new AteliaRefinery();
	}
}
