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
package ai.areas.Aden.Herphah;

import com.l2jmobius.gameserver.model.actor.Npc;
import com.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Aden Faction Npc AI
 * @author NightBR
 * @date 2019-03-27
 */
public class Herphah extends AbstractNpcAI
{
	// NPC
	private static final int HERPHAH = 34362;
	
	private Herphah()
	{
		addStartNpc(HERPHAH);
		addTalkId(HERPHAH);
		addFirstTalkId(HERPHAH);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("34362-01.html"))
		{
			// TODO: return event;
		}
		return null;
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		player.sendPacket(new PlaySound(3, "Npcdialog1.herphah_faction_1", 0, 0, 0, 0, 0));
		return "34362.html";
	}
	
	public static void main(String[] args)
	{
		new Herphah();
	}
}
