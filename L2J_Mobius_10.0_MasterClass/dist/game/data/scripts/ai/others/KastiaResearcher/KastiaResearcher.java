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
package ai.others.KastiaResearcher;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.instancezone.Instance;

import ai.AbstractNpcAI;

/**
 * @author Mobius
 */
public class KastiaResearcher extends AbstractNpcAI
{
	// NPC
	private static final int RESEARCHER = 34566;
	
	public KastiaResearcher()
	{
		addStartNpc(RESEARCHER);
		addTalkId(RESEARCHER);
		addFirstTalkId(RESEARCHER);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equals("exit"))
		{
			final Instance world = player.getInstanceWorld();
			if (world != null)
			{
				world.finishInstance(0);
			}
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		new KastiaResearcher();
	}
}