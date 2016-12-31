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
package ai.bosses.Beleth.Wormhole;

import java.util.List;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.instancemanager.GrandBossManager;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.Location;
import com.l2jmobius.gameserver.model.actor.L2Npc;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.AbstractNpcAI;

/**
 * Wormhole AI (33901).
 * @author gigi
 */
public final class Wormhole extends AbstractNpcAI
{
	// NPCs
	private static final int WORMHOLE = 33901;
	private static final int BELETH = 29118;
	// Location
	private static final Location BELETH_LOCATION = new Location(16327, 209228, -9357);
	// TODO: New location
	// private static final Location BELETH_LOCATION = new Location(-17551, 245949, -832);
	
	public Wormhole()
	{
		addFirstTalkId(WORMHOLE);
		addTalkId(WORMHOLE);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		String htmltext = null;
		switch (event)
		{
			case "teleport":
			{
				if (GrandBossManager.getInstance().getBossStatus(BELETH) > 0)
				{
					htmltext = "33901-4.html";
					break;
				}
				
				if (!player.isInParty())
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player.getHtmlPrefix(), "33901-2.html"));
					packet.replace("%min%", Integer.toString(Config.BELETH_MIN_PLAYERS));
					player.sendPacket(packet);
					break;
				}
				
				final L2Party party = player.getParty();
				final boolean isInCC = party.isInCommandChannel();
				final List<L2PcInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
				final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
				if (!isPartyLeader)
				{
					htmltext = "33901-3.html";
					break;
					
				}
				else if ((members.size() < Config.BELETH_MIN_PLAYERS) || (members.size() > Config.BELETH_MAX_PLAYERS))
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player.getHtmlPrefix(), "33901-2.html"));
					packet.replace("%min%", Integer.toString(Config.BELETH_MIN_PLAYERS));
					player.sendPacket(packet);
					break;
				}
				else
				{
					for (L2PcInstance member : members)
					{
						if (member.isInsideRadius(npc, 1000, true, false))
						{
							member.teleToLocation(BELETH_LOCATION, true);
						}
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
		final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
		packet.setHtml(getHtm(player.getHtmlPrefix(), "33901-1.html"));
		packet.replace("%min%", Integer.toString(Config.BELETH_MIN_PLAYERS));
		packet.replace("%max%", Integer.toString(Config.BELETH_MAX_PLAYERS));
		player.sendPacket(packet);
		return null;
	}
	
	public static void main(String[] args)
	{
		new Wormhole();
	}
}