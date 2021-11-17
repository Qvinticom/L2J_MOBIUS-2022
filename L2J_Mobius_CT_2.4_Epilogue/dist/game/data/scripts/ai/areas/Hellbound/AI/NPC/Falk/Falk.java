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
package ai.areas.Hellbound.AI.NPC.Falk;

import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;

import ai.AbstractNpcAI;

/**
 * Falk AI.
 * @author DS
 */
public class Falk extends AbstractNpcAI
{
	// NPCs
	private static final int FALK = 32297;
	// Items
	private static final int DARION_BADGE = 9674;
	private static final int BASIC_CERT = 9850; // Basic Caravan Certificate
	private static final int STANDART_CERT = 9851; // Standard Caravan Certificate
	private static final int PREMIUM_CERT = 9852; // Premium Caravan Certificate
	
	public Falk()
	{
		addFirstTalkId(FALK);
		addStartNpc(FALK);
		addTalkId(FALK);
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		if (hasAtLeastOneQuestItem(player, BASIC_CERT, STANDART_CERT, PREMIUM_CERT))
		{
			return "32297-01a.htm";
		}
		return "32297-01.htm";
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		if (hasAtLeastOneQuestItem(player, BASIC_CERT, STANDART_CERT, PREMIUM_CERT))
		{
			return "32297-01a.htm";
		}
		return "32297-02.htm";
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		if (event.equalsIgnoreCase("badges") && !hasAtLeastOneQuestItem(player, BASIC_CERT, STANDART_CERT, PREMIUM_CERT))
		{
			if (getQuestItemsCount(player, DARION_BADGE) >= 20)
			{
				takeItems(player, DARION_BADGE, 20);
				giveItems(player, BASIC_CERT, 1);
				return "32297-02a.htm";
			}
			return "32297-02b.htm";
		}
		return super.onAdvEvent(event, npc, player);
	}
}