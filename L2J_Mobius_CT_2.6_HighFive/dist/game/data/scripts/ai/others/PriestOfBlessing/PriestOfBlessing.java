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
package ai.others.PriestOfBlessing;

import org.l2jmobius.commons.util.Chronos;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.itemcontainer.Inventory;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.Util;

import ai.AbstractNpcAI;

/**
 * Priest Of Blessing AI.
 * @author Gnacik
 */
public class PriestOfBlessing extends AbstractNpcAI
{
	// NPC
	private static final int PRIEST = 32783;
	// Spawn state
	private static boolean SPAWNED = false;
	// Items
	private static final int NEVIT_VOICE = 17094;
	// @formatter:off
	private static final int[][] HOURGLASSES =
	{
		{ 17095, 17096, 17097, 17098, 17099 },
		{ 17100, 17101, 17102, 17103, 17104 },
		{ 17105, 17106, 17107, 17108, 17109 },
		{ 17110, 17111, 17112, 17113, 17114 },
		{ 17115, 17116, 17117, 17118, 17119 },
		{ 17120, 17121, 17122, 17123, 17124 },
		{ 17125, 17126, 17127, 17128, 17129 }
	};
	// @formatter:on
	// Prices
	private static final int PRICE_VOICE = 100000;
	private static final int[] PRICE_HOURGLASS =
	{
		4000,
		30000,
		110000,
		310000,
		970000,
		2160000,
		5000000
	};
	// Locations
	private static final Location[] SPAWNS =
	{
		new Location(-84144, 243148, -3713, 8473),
		new Location(-119694, 44553, 365, 33023),
		new Location(45412, 48358, -3060, 50020),
		new Location(115626, -177944, -905, 38058),
		new Location(12086, 16583, -4587, 63000),
		new Location(-45032, -113561, -192, 32767),
		new Location(-83123, 150921, -3132, 2280),
		new Location(-13921, 121937, -2989, 30212),
		new Location(87123, -141327, -1340, 49153),
		new Location(43520, -47590, -792, 43738),
		new Location(148060, -55314, -2728, 40961),
		new Location(82801, 149381, -3464, 53707),
		new Location(82431, 53281, -1496, 16383),
		new Location(147063, 25943, -2013, 49151),
		new Location(111169, 221052, -3549, 0),
		new Location(15907, 142901, -2688, 14324),
		new Location(116973, 77245, -2697, 41760)
	};
	
	private PriestOfBlessing()
	{
		addStartNpc(PRIEST);
		addFirstTalkId(PRIEST);
		addTalkId(PRIEST);
		
		if (!SPAWNED)
		{
			for (Location spawn : SPAWNS)
			{
				addSpawn(PRIEST, spawn, false, 0);
			}
			SPAWNED = true;
		}
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, Player player)
	{
		String htmltext = event;
		if (event.equalsIgnoreCase("buy_voice"))
		{
			if (player.getAdena() >= PRICE_VOICE)
			{
				final String value = player.getVariables().getString("PriestOfBlessing_voice", "");
				final long reuseTime = value.isEmpty() ? 0 : Long.parseLong(value);
				if (Chronos.currentTimeMillis() > reuseTime)
				{
					takeItems(player, Inventory.ADENA_ID, PRICE_VOICE);
					giveItems(player, NEVIT_VOICE, 1);
					player.getVariables().set("PriestOfBlessing_voice", Long.toString(Chronos.currentTimeMillis() + (20 * 3600000)));
				}
				else
				{
					final long remainingTime = (reuseTime - Chronos.currentTimeMillis()) / 1000;
					final int hours = (int) (remainingTime / 3600);
					final int minutes = (int) ((remainingTime % 3600) / 60);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_FOR_RE_USE_AFTER_S2_HOUR_S_S3_MINUTE_S);
					sm.addItemName(NEVIT_VOICE);
					sm.addInt(hours);
					sm.addInt(minutes);
					player.sendPacket(sm);
				}
				return null;
			}
			htmltext = "32783-adena.htm";
		}
		else if (event.equalsIgnoreCase("buy_hourglass"))
		{
			final int index = getHGIndex(player.getLevel());
			final int priceHourglass = PRICE_HOURGLASS[index];
			if (player.getAdena() >= priceHourglass)
			{
				final String value = player.getVariables().getString("PriestOfBlessing_hg_" + index, "");
				final long reuseTime = value.isEmpty() ? 0 : Long.parseLong(value);
				if (Chronos.currentTimeMillis() > reuseTime)
				{
					final int[] hg = HOURGLASSES[index];
					final int nevitHourglass = hg[getRandom(0, hg.length - 1)];
					takeItems(player, Inventory.ADENA_ID, priceHourglass);
					giveItems(player, nevitHourglass, 1);
					player.getVariables().set("PriestOfBlessing_hg_" + index, Long.toString(Chronos.currentTimeMillis() + (20 * 3600000)));
				}
				else
				{
					final long remainingTime = (reuseTime - Chronos.currentTimeMillis()) / 1000;
					final int hours = (int) (remainingTime / 3600);
					final int minutes = (int) ((remainingTime % 3600) / 60);
					final SystemMessage sm = new SystemMessage(SystemMessageId.S1_WILL_BE_AVAILABLE_FOR_RE_USE_AFTER_S2_HOUR_S_S3_MINUTE_S);
					sm.addString("Nevit's Hourglass");
					sm.addInt(hours);
					sm.addInt(minutes);
					player.sendPacket(sm);
				}
				return null;
			}
			htmltext = "32783-adena.htm";
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(Npc npc, Player player)
	{
		String content = getHtm(player, "32783.htm");
		content = content.replace("%donate%", Util.formatAdena(PRICE_HOURGLASS[getHGIndex(player.getLevel())]));
		return content;
	}
	
	private int getHGIndex(int level)
	{
		int index = 0;
		if (level < 20)
		{
			index = 0;
		}
		else if (level < 40)
		{
			index = 1;
		}
		else if (level < 52)
		{
			index = 2;
		}
		else if (level < 61)
		{
			index = 3;
		}
		else if (level < 76)
		{
			index = 4;
		}
		else if (level < 80)
		{
			index = 5;
		}
		else if (level < 86)
		{
			index = 6;
		}
		return index;
	}
	
	public static void main(String[] args)
	{
		new PriestOfBlessing();
	}
}
