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
package ai.areas.AteliaRefinery.Leona;

import java.util.List;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.instancemanager.GrandBossManager;
import org.l2jmobius.gameserver.model.Location;
import org.l2jmobius.gameserver.model.Party;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.quest.QuestState;
import org.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;

import ai.AbstractNpcAI;
import quests.Q10890_SaviorsPathHallOfEtina.Q10890_SaviorsPathHallOfEtina;

/**
 * @author Liamxroy
 */
public class Leona extends AbstractNpcAI
{
	// NPCs
	private static final int LEONA = 34426;
	private static final int ETINA_RAID = 29318;
	// Location
	private static final Location ENTER_LOC = new Location(-245778, 181088, 2860);
	private static final Location REFINERY = new Location(-59328, 52624, -8608);
	
	public Leona()
	{
		addFirstTalkId(LEONA);
		addTalkId(LEONA);
	}
	
	@Override
	public String onAdvEvent(String event, Npc npc, PlayerInstance player)
	{
		if (event.equals("teleport"))
		{
			final int status = GrandBossManager.getInstance().getBossStatus(ETINA_RAID);
			if (player.isGM())
			{
				player.teleToLocation(ENTER_LOC, true);
				GrandBossManager.getInstance().setBossStatus(ETINA_RAID, 1);
			}
			else
			{
				if (status == 1)
				{
					return "34426-1.html";
				}
				if (status == 2)
				{
					return "34426-2.html";
				}
				if (!player.isInParty())
				{
					return "34426-3.html";
				}
				final Party party = player.getParty();
				final boolean isInCC = party.isInCommandChannel();
				final List<PlayerInstance> members = (isInCC) ? party.getCommandChannel().getMembers() : party.getMembers();
				final boolean isPartyLeader = (isInCC) ? party.getCommandChannel().isLeader(player) : party.isLeader(player);
				if (!isPartyLeader)
				{
					return "34426-3.html";
				}
				if ((members.size() < Config.ETINA_MIN_PLAYERS) || (members.size() > Config.ETINA_MAX_PLAYERS))
				{
					final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
					packet.setHtml(getHtm(player, "34426-4.html"));
					packet.replace("%min%", Integer.toString(Config.ETINA_MIN_PLAYERS));
					packet.replace("%max%", Integer.toString(Config.ETINA_MAX_PLAYERS));
					player.sendPacket(packet);
					return null;
				}
				for (PlayerInstance member : members)
				{
					if (member.getLevel() < Config.ETINA_MIN_PLAYER_LEVEL)
					{
						final NpcHtmlMessage packet = new NpcHtmlMessage(npc.getObjectId());
						packet.setHtml(getHtm(player, "34426-5.html"));
						packet.replace("%minlvl%", Integer.toString(Config.ETINA_MIN_PLAYER_LEVEL));
						player.sendPacket(packet);
						return null;
					}
				}
				for (PlayerInstance member : members)
				{
					if (member.isInsideRadius3D(npc, Config.ALT_PARTY_RANGE))
					{
						member.teleToLocation(ENTER_LOC, false);
						GrandBossManager.getInstance().setBossStatus(ETINA_RAID, 1);
					}
				}
			}
		}
		else if (event.equals("tp_inner"))
		{
			player.teleToLocation(REFINERY, true);
		}
		return super.onAdvEvent(event, npc, player);
	}
	
	@Override
	public String onFirstTalk(Npc npc, PlayerInstance player)
	{
		final QuestState qs = player.getQuestState(Q10890_SaviorsPathHallOfEtina.class.getSimpleName());
		String htmltext = null;
		if (((qs != null) && qs.isCompleted()))
		{
			htmltext = "34426-0.html";
		}
		else
		{
			htmltext = "34426.html";
		}
		return htmltext;
	}
	
	public static void main(String[] args)
	{
		new Leona();
	}
}
