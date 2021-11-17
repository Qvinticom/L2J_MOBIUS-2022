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
package ai.others.EinhasadStore;

import org.l2jmobius.gameserver.cache.HtmCache;
import org.l2jmobius.gameserver.data.xml.MultisellData;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.events.EventType;
import org.l2jmobius.gameserver.model.events.ListenerRegisterType;
import org.l2jmobius.gameserver.model.events.annotations.RegisterEvent;
import org.l2jmobius.gameserver.model.events.annotations.RegisterType;
import org.l2jmobius.gameserver.model.events.impl.creature.player.OnPlayerBypass;
import org.l2jmobius.gameserver.network.serverpackets.ExPremiumManagerShowHtml;

import ai.AbstractNpcAI;

/**
 * @author Index
 */
public class EinhasadStore extends AbstractNpcAI
{
	// NPC
	private static final int MERCHANT = 34487;
	// Multisells
	private static final int JEWELS_STONE = 34487001;
	private static final int ACCESSORIES = 34487002;
	private static final int SCROLLS = 34487003;
	private static final int ENHANCEMENT = 34487004;
	private static final int OTHER = 34487005;
	private static final int CLOAK = 34487006;
	// Others
	private static final String COMMAND_BYPASS = "Quest EinhasadStore ";
	
	private EinhasadStore()
	{
		addStartNpc(MERCHANT);
		addFirstTalkId(MERCHANT);
		addTalkId(MERCHANT);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		final String htmltext = null;
		switch (event)
		{
			
			case "back":
			{
				player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/EinhasadStore/34487.html")));
				break;
			}
			// Bypass
			case "Chat_Jewell_Stones":
			{
				MultisellData.getInstance().separateAndSend(JEWELS_STONE, player, null, false);
				break;
			}
			case "Chat_Accessories":
			{
				MultisellData.getInstance().separateAndSend(ACCESSORIES, player, null, false);
				break;
			}
			case "Chat_Scrolls":
			{
				MultisellData.getInstance().separateAndSend(SCROLLS, player, null, false);
				break;
			}
			case "Chat_Enhancement":
			{
				MultisellData.getInstance().separateAndSend(ENHANCEMENT, player, null, false);
				break;
			}
			case "Chat_Others":
			{
				MultisellData.getInstance().separateAndSend(OTHER, player, null, false);
				break;
			}
			case "Chat_Cloak":
			{
				MultisellData.getInstance().separateAndSend(CLOAK, player, null, false);
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		player.sendPacket(new ExPremiumManagerShowHtml(HtmCache.getInstance().getHtm(player, "data/scripts/ai/others/EinhasadStore/34487.html")));
		return null;
	}
	
	@RegisterEvent(EventType.ON_PLAYER_BYPASS)
	@RegisterType(ListenerRegisterType.GLOBAL_PLAYERS)
	public void onPlayerBypass(OnPlayerBypass event)
	{
		final Player player = event.getPlayer();
		if (event.getCommand().startsWith(COMMAND_BYPASS))
		{
			notifyEvent(event.getCommand().replace(COMMAND_BYPASS, ""), null, player);
		}
	}
	
	public static void main(String[] args)
	{
		new EinhasadStore();
	}
}