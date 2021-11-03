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
package events.EveTheFortuneTeller;

import org.l2jmobius.gameserver.enums.LuckyGameType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.LongTimeEvent;
import org.l2jmobius.gameserver.network.serverpackets.luckygame.ExStartLuckyGame;

/**
 * Eve the Fortune Teller Returns<br>
 * Info - http://www.lineage2.com/en/news/events/11182015-eve-the-fortune-teller-returns.php
 * @author Mobius
 */
public class EveTheFortuneTeller extends LongTimeEvent
{
	// NPCs
	private static final int EVE = 31855;
	// Items
	private static final int FORTUNE_READING_TICKET = 23767;
	private static final int LUXURY_FORTUNE_READING_TICKET = 23768;
	
	private EveTheFortuneTeller()
	{
		addStartNpc(EVE);
		addFirstTalkId(EVE);
		addTalkId(EVE);
		addSpawnId(EVE);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "31855.htm":
			case "31855-1.htm":
			{
				htmltext = event;
				break;
			}
			case "FortuneReadingGame":
			{
				player.sendPacket(new ExStartLuckyGame(LuckyGameType.NORMAL, player.getInventory().getInventoryItemCount(FORTUNE_READING_TICKET, -1)));
				break;
			}
			case "LuxuryFortuneReadingGame":
			{
				player.sendPacket(new ExStartLuckyGame(LuckyGameType.LUXURY, player.getInventory().getInventoryItemCount(LUXURY_FORTUNE_READING_TICKET, -1)));
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		return "31855.htm";
	}
	
	public static void main(String[] args)
	{
		new EveTheFortuneTeller();
	}
}
