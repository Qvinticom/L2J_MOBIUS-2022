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
package com.l2jmobius.gameserver.model.entity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import com.l2jmobius.gameserver.datatables.NpcTable;
import com.l2jmobius.gameserver.datatables.SpawnTable;
import com.l2jmobius.gameserver.model.L2Spawn;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.network.clientpackets.Say2;
import com.l2jmobius.gameserver.network.serverpackets.CreatureSay;
import com.l2jmobius.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;
import com.l2jmobius.util.EventData;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.3.4.1 $ $Date: 2005/03/27 15:29:32 $
 */

public class L2Event
{
	public static String eventName = "";
	public static int teamsNumber = 0;
	public static HashMap<Integer, String> names = new HashMap<>();
	public static LinkedList<String> participatingPlayers = new LinkedList<>();
	public static HashMap<Integer, LinkedList<String>> players = new HashMap<>();
	public static int id = 12760;
	public static LinkedList<String> npcs = new LinkedList<>();
	public static boolean active = false;
	public static HashMap<String, EventData> connectionLossData = new HashMap<>();
	
	public static int getTeamOfPlayer(String name)
	{
		for (int i = 1; i <= players.size(); i++)
		{
			final LinkedList<?> temp = players.get(i);
			final Iterator<?> it = temp.iterator();
			while (it.hasNext())
			{
				if (it.next().equals(name))
				{
					return i;
				}
			}
		}
		return 0;
	}
	
	public static String[] getTopNKillers(int N)
	{
		// this will return top N players sorted by kills, first element in the array will be the one with more kills
		final String[] killers = new String[N];
		String playerTemp = "";
		int kills = 0;
		final LinkedList<String> killersTemp = new LinkedList<>();
		
		for (int k = 0; k < N; k++)
		{
			kills = 0;
			for (int i = 1; i <= teamsNumber; i++)
			{
				final LinkedList<?> temp = players.get(i);
				final Iterator<?> it = temp.iterator();
				while (it.hasNext())
				{
					try
					{
						final L2PcInstance player = L2World.getInstance().getPlayer((String) it.next());
						if (!killersTemp.contains(player.getName()))
						{
							if (player.kills.size() > kills)
							{
								kills = player.kills.size();
								playerTemp = player.getName();
							}
						}
					}
					catch (final Exception e)
					{
					}
				}
			}
			killersTemp.add(playerTemp);
		}
		
		for (int i = 0; i < N; i++)
		{
			kills = 0;
			final Iterator<String> it = killersTemp.iterator();
			while (it.hasNext())
			{
				try
				{
					final L2PcInstance player = L2World.getInstance().getPlayer(it.next());
					if (player.kills.size() > kills)
					{
						kills = player.kills.size();
						playerTemp = player.getName();
					}
				}
				catch (final Exception e)
				{
				}
			}
			killers[i] = playerTemp;
			killersTemp.remove(playerTemp);
		}
		return killers;
	}
	
	public static void showEventHtml(L2PcInstance player, String objectid)
	{
		try (FileInputStream fs = new FileInputStream("data/events/" + eventName);
			BufferedInputStream bs = new BufferedInputStream(fs);
			DataInputStream in = new DataInputStream(bs);
			InputStreamReader ir = new InputStreamReader(in);
			BufferedReader inbr = new BufferedReader(ir))
		{
			final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
			
			final TextBuilder replyMSG = new TextBuilder("<html><body>");
			replyMSG.append("<center><font color=\"LEVEL\">" + eventName + "</font><font color=\"FF0000\"> bY " + inbr.readLine() + "</font></center><br>");
			
			replyMSG.append("<br>" + inbr.readLine());
			if (L2Event.participatingPlayers.contains(player.getName()))
			{
				replyMSG.append("<br><center>You are already in the event players list !!</center></body></html>");
			}
			else
			{
				replyMSG.append("<br><center><button value=\"Participate !! \" action=\"bypass -h npc_" + objectid + "_event_participate\" width=90 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>");
			}
			
			adminReply.setHtml(replyMSG.toString());
			player.sendPacket(adminReply);
		}
		catch (final Exception e)
		{
			System.out.println(e);
		}
	}
	
	public static void spawn(L2PcInstance target, int npcid)
	{
		final L2NpcTemplate template1 = NpcTable.getInstance().getTemplate(npcid);
		
		try
		{
			final L2Spawn spawn = new L2Spawn(template1);
			
			spawn.setLocx(target.getX() + 50);
			spawn.setLocy(target.getY() + 50);
			spawn.setLocz(target.getZ());
			spawn.setAmount(1);
			spawn.setHeading(target.getHeading());
			spawn.setRespawnDelay(1);
			
			SpawnTable.getInstance().addNewSpawn(spawn, false);
			
			spawn.init();
			spawn.getLastSpawn().setCurrentHp(999999999);
			spawn.getLastSpawn().setName("event inscriptor");
			spawn.getLastSpawn().setTitle(L2Event.eventName);
			spawn.getLastSpawn().isEventMob = true;
			spawn.getLastSpawn().isAggressive();
			spawn.getLastSpawn().decayMe();
			spawn.getLastSpawn().spawnMe(spawn.getLastSpawn().getX(), spawn.getLastSpawn().getY(), spawn.getLastSpawn().getZ());
			
			spawn.getLastSpawn().broadcastPacket(new MagicSkillUse(spawn.getLastSpawn(), spawn.getLastSpawn(), 1034, 1, 1, 1));
			
			npcs.add(String.valueOf(spawn.getLastSpawn().getObjectId()));
			
		}
		catch (final Exception e)
		{
			System.out.println(e);
		}
	}
	
	public static void announceAllPlayers(String text)
	{
		final CreatureSay cs = new CreatureSay(0, Say2.ANNOUNCEMENT, "", text);
		
		for (final L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			player.sendPacket(cs);
		}
	}
	
	public static boolean isOnEvent(L2PcInstance player)
	{
		for (int k = 0; k < L2Event.teamsNumber; k++)
		{
			final Iterator<?> it = L2Event.players.get(k + 1).iterator();
			boolean temp = false;
			while (it.hasNext())
			{
				temp = player.getName().equalsIgnoreCase(it.next().toString());
				if (temp)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static void inscribePlayer(L2PcInstance player)
	{
		try
		{
			L2Event.participatingPlayers.add(player.getName());
			player.eventkarma = player.getKarma();
			player.eventX = player.getX();
			player.eventY = player.getY();
			player.eventZ = player.getZ();
			player.eventpkkills = player.getPkKills();
			player.eventpvpkills = player.getPvpKills();
			player.eventTitle = player.getTitle();
			player.kills.clear();
			player.atEvent = true;
		}
		catch (final Exception e)
		{
			System.out.println("error when signing in the event:" + e);
		}
	}
	
	public static void restoreChar(L2PcInstance player)
	{
		try
		{
			player.eventX = connectionLossData.get(player.getName()).eventX;
			player.eventY = connectionLossData.get(player.getName()).eventY;
			player.eventZ = connectionLossData.get(player.getName()).eventZ;
			player.eventkarma = connectionLossData.get(player.getName()).eventkarma;
			player.eventpvpkills = connectionLossData.get(player.getName()).eventpvpkills;
			player.eventpkkills = connectionLossData.get(player.getName()).eventpkkills;
			player.eventTitle = connectionLossData.get(player.getName()).eventTitle;
			player.kills = connectionLossData.get(player.getName()).kills;
			player.eventSitForced = connectionLossData.get(player.getName()).eventSitForced;
			player.atEvent = true;
		}
		catch (final Exception e)
		{
		}
	}
	
	public static void restoreAndTeleChar(L2PcInstance target)
	{
		try
		{
			restoreChar(target);
			target.setTitle(target.eventTitle);
			target.setKarma(target.eventkarma);
			target.setPvpKills(target.eventpvpkills);
			target.setPkKills(target.eventpkkills);
			target.teleToLocation(target.eventX, target.eventY, target.eventZ, true);
			target.kills.clear();
			target.eventSitForced = false;
			target.atEvent = false;
		}
		catch (final Exception e)
		{
		}
	}
}