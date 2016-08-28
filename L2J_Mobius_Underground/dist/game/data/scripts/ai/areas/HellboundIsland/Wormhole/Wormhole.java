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
package ai.areas.HellboundIsland.Wormhole;

import java.util.List;

import com.l2jmobius.gameserver.model.L2Party;
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
	// Minimum and maximum command channel members
	private static final int MIN_MEMBERS = 49;
	private static final int MAX_MEMBERS = 350;
	
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
				if (!player.isInParty())
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player.getHtmlPrefix(), "33901-2.html"));
					packet.replace("%min%", Integer.toString(MIN_MEMBERS));
					player.sendPacket(packet);
					break;
				}
				else if (player.isInParty())
				{
					final L2Party party = player.getParty();
					final boolean isInCC = party.isInCommandChannel();
					final List<L2PcInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
					final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
					if (!isPartyLeader)
					{
						htmltext = "33901-3.html";
						break;
						
					}
					else if ((members.size() < MIN_MEMBERS) || (members.size() > MAX_MEMBERS))
					{
						final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
						packet.setHtml(getHtm(player.getHtmlPrefix(), "33901-2.html"));
						packet.replace("%min%", Integer.toString(MIN_MEMBERS));
						player.sendPacket(packet);
						break;
					}
					else
					{
						for (L2PcInstance member : members)
						{
							if (member.isInsideRadius(npc, 1000, true, false))
							{
								// TODO: need teleport in instance?
								member.teleToLocation(-17556 + getRandom(700), 245951 + getRandom(700), -832);
							}
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
		packet.replace("%min%", Integer.toString(MIN_MEMBERS));
		packet.replace("%max%", Integer.toString(MAX_MEMBERS));
		player.sendPacket(packet);
		return null;
	}
	
	public static void main(String[] args)
	{
		new Wormhole();
	}
}