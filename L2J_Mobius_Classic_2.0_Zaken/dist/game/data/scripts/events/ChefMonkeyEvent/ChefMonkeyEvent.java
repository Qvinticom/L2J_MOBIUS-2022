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
package events.ChefMonkeyEvent;

import com.l2jmobius.gameserver.data.xml.impl.MultisellData;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.events.EventType;
import com.l2jmobius.gameserver.model.events.ListenerRegisterType;
import com.l2jmobius.gameserver.model.events.annotations.Id;
import com.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import com.l2jmobius.gameserver.model.events.annotations.RegisterType;
import com.l2jmobius.gameserver.model.events.impl.character.npc.OnNpcMenuSelect;
import com.l2jmobius.gameserver.model.quest.LongTimeEvent;

/**
 * Chef Monkey Event<br>
 * http://www.lineage2.com/en/news/events/chef-monkey-event-2016.php
 * @author ChaosPaladin
 */
public final class ChefMonkeyEvent extends LongTimeEvent
{
	// NPCs
	private static final int EV_CHEF_MONKEY = 34292;
	
	private ChefMonkeyEvent()
	{
		addStartNpc(EV_CHEF_MONKEY);
		addFirstTalkId(EV_CHEF_MONKEY);
		addTalkId(EV_CHEF_MONKEY);
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return "ev_chef_monkey001.htm";
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		
		switch (event)
		{
			case "ev_chef_monkey001.htm":
			case "ev_chef_monkey002.htm":
			case "ev_chef_monkey003.htm":
			case "ev_chef_monkey004.htm":
			case "ev_chef_monkey005.htm":
			{
				htmltext = event;
				break;
			}
		}
		return htmltext;
	}
	
	@RegisterEvent(EventType.ON_NPC_MENU_SELECT)
	@RegisterType(ListenerRegisterType.NPC)
	@Id(EV_CHEF_MONKEY)
	public final void OnNpcMenuSelect(OnNpcMenuSelect event)
	{
		final L2PcInstance player = event.getTalker();
		final L2Npc npc = event.getNpc();
		final int ask = event.getAsk();
		final int reply = event.getReply();
		
		if (ask == -303)
		{
			if (reply == 2209)
			{
				// I want to buy Monkey Bait
				MultisellData.getInstance().separateAndSend(2209, player, npc, false);
			}
			else if (reply == 2212)
			{
				// I want to exchange Tuna fish
				MultisellData.getInstance().separateAndSend(2212, player, npc, false);
			}
			else if (reply == 3043)
			{
				// buy buff cocktail
				MultisellData.getInstance().separateAndSend(3043, player, npc, false);
				
			}
		}
	}
	
	public static void main(String[] args)
	{
		new ChefMonkeyEvent();
	}
}
