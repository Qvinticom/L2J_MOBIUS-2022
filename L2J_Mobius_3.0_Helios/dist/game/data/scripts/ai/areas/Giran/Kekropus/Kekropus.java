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
package ai.areas.Giran.Kekropus;

import java.util.List;

import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.PlaySound;

import ai.AbstractNpcAI;

/**
 * Kekropus AI
 * @author Gigi
 */
public final class Kekropus extends AbstractNpcAI
{
	// NPC
	private static final int KEKROPUS = 34222;
	// Teleports
	private static final Location TELEPORT = new Location(79827, 152588, 2304);
	private static final Location ENTER_LOC = new Location(79313, 153617, 2307);
	// Config
	private static final int HELIOS_MIN_PLAYER = 70;
	private static final int HELIOS_MIN_PLAYER_LVL = 102;
	
	private Kekropus()
	{
		addStartNpc(KEKROPUS);
		addTalkId(KEKROPUS);
		addFirstTalkId(KEKROPUS);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "teleport":
			{
				player.teleToLocation(TELEPORT);
				break;
			}
			case "helios":
			{
				if (!player.isInParty())
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player.getHtmlPrefix(), "34222-01.html"));
					packet.replace("%min%", Integer.toString(HELIOS_MIN_PLAYER));
					packet.replace("%minlvl%", Integer.toString(HELIOS_MIN_PLAYER_LVL));
					player.sendPacket(packet);
					return null;
				}
				final L2Party party = player.getParty();
				final boolean isInCC = party.isInCommandChannel();
				final List<L2PcInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
				final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
				if (!isPartyLeader)
				{
					return "34222-02.html";
				}
				if (members.size() < HELIOS_MIN_PLAYER)
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player.getHtmlPrefix(), "34222-01.html"));
					packet.replace("%min%", Integer.toString(HELIOS_MIN_PLAYER));
					packet.replace("%minlvl%", Integer.toString(HELIOS_MIN_PLAYER_LVL));
					player.sendPacket(packet);
					return null;
				}
				for (L2PcInstance member : members)
				{
					if (member.getLevel() < HELIOS_MIN_PLAYER_LVL)
					{
						final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
						packet.setHtml(getHtm(player.getHtmlPrefix(), "34222-01.html"));
						packet.replace("%min%", Integer.toString(HELIOS_MIN_PLAYER));
						packet.replace("%minlvl%", Integer.toString(HELIOS_MIN_PLAYER_LVL));
						player.sendPacket(packet);
						return null;
					}
				}
				for (L2PcInstance member : members)
				{
					if ((member.calculateDistance(npc, false, false) < 1000) && (npc.getId() == KEKROPUS))
					{
						member.teleToLocation(ENTER_LOC, true);
					}
				}
				break;
			}
		}
		return htmltext;
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		final int i = getRandom(0, 12);
		if ((i > 0) && (i <= 3))
		{
			player.sendPacket(new PlaySound(3, "Npcdialog1.kekrops_greeting_8", 0, 0, 0, 0, 0));
		}
		else if ((i > 3) && (i <= 6))
		{
			player.sendPacket(new PlaySound(3, "Npcdialog1.kekrops_greeting_7", 0, 0, 0, 0, 0));
		}
		else if ((i > 6) && (i <= 9))
		{
			player.sendPacket(new PlaySound(3, "Npcdialog1.kekrops_greeting_6", 0, 0, 0, 0, 0));
		}
		else
		{
			player.sendPacket(new PlaySound(3, "Npcdialog1.kekrops_greeting_5", 0, 0, 0, 0, 0));
		}
		return "34222.html";
	}
	
	public static void main(String[] args)
	{
		new Kekropus();
	}
}
