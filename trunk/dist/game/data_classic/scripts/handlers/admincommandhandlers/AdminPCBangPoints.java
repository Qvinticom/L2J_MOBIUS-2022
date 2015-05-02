/*
 * Copyright (C) 2004-2015 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.admincommandhandlers;

import java.util.Collection;
import java.util.StringTokenizer;

import com.l2jserver.Config;
import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.handler.IAdminCommandHandler;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.serverpackets.ExPCCafePointInfo;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.util.Util;

/**
 * Admin PC Points manage admin commands.<br>
 * Based on AdminPrimePoints by St3eT.
 */
public final class AdminPCBangPoints implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_pcbangpoints",
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		
		if (actualCommand.equals("admin_pcbangpoints"))
		{
			if (st.hasMoreTokens())
			{
				final String action = st.nextToken();
				
				final L2PcInstance target = getTarget(activeChar);
				if ((target == null) || !st.hasMoreTokens())
				{
					return false;
				}
				
				int value = 0;
				try
				{
					value = Integer.parseInt(st.nextToken());
				}
				catch (Exception e)
				{
					showMenuHtml(activeChar);
					activeChar.sendMessage("Invalid Value!");
					return false;
				}
				
				switch (action)
				{
					case "set":
					{
						if (value > Config.PC_BANG_MAX_POINTS)
						{
							showMenuHtml(activeChar);
							activeChar.sendMessage("You cannot set more than " + Config.PC_BANG_MAX_POINTS + " PC points!");
							return false;
						}
						if (value < 0)
						{
							value = 0;
						}
						
						target.setPcBangPoints(value);
						target.sendMessage("Admin set your PC point(s) to " + value + "!");
						activeChar.sendMessage("You set " + value + " PC point(s) to player " + target.getName());
						target.sendPacket(new ExPCCafePointInfo(value, value, 1));
						break;
					}
					case "increase":
					{
						if (target.getPcBangPoints() == Config.PC_BANG_MAX_POINTS)
						{
							showMenuHtml(activeChar);
							activeChar.sendMessage(target.getName() + " already have max count of PC points!");
							return false;
						}
						
						int pcBangCount = Math.min((target.getPcBangPoints() + value), Config.PC_BANG_MAX_POINTS);
						if (pcBangCount < 0)
						{
							pcBangCount = Config.PC_BANG_MAX_POINTS;
						}
						target.setPcBangPoints(pcBangCount);
						target.sendMessage("Admin increased your PC point(s) by " + value + "!");
						activeChar.sendMessage("You increased PC point(s) of " + target.getName() + " by " + value);
						target.sendPacket(new ExPCCafePointInfo(pcBangCount, value, 1));
						break;
					}
					case "decrease":
					{
						if (target.getPcBangPoints() == 0)
						{
							showMenuHtml(activeChar);
							activeChar.sendMessage(target.getName() + " already have min count of PC points!");
							return false;
						}
						
						final int pcBangCount = Math.max(target.getPcBangPoints() - value, 0);
						target.setPcBangPoints(pcBangCount);
						target.sendMessage("Admin decreased your PC point(s) by " + value + "!");
						activeChar.sendMessage("You decreased PC point(s) of " + target.getName() + " by " + value);
						target.sendPacket(new ExPCCafePointInfo(pcBangCount, value, 1));
						break;
					}
					case "rewardOnline":
					{
						int range = 0;
						try
						{
							range = Integer.parseInt(st.nextToken());
						}
						catch (Exception e)
						{
							
						}
						
						if (range <= 0)
						{
							final int count = increaseForAll(L2World.getInstance().getPlayers(), value);
							activeChar.sendMessage("You increased PC point(s) of all online players (" + count + ") by " + value + ".");
						}
						else if (range > 0)
						{
							final int count = increaseForAll(activeChar.getKnownList().getKnownPlayers().values(), value);
							activeChar.sendMessage("You increased PC point(s) of all players (" + count + ") in range " + range + " by " + value + ".");
						}
						break;
					}
				}
				showMenuHtml(activeChar);
			}
			else
			{
				showMenuHtml(activeChar);
			}
		}
		return true;
	}
	
	private int increaseForAll(Collection<L2PcInstance> playerList, int value)
	{
		int counter = 0;
		for (L2PcInstance temp : playerList)
		{
			if ((temp != null) && (temp.isOnlineInt() == 1))
			{
				if (temp.getPcBangPoints() == Integer.MAX_VALUE)
				{
					continue;
				}
				
				int pcBangCount = Math.min((temp.getPcBangPoints() + value), Integer.MAX_VALUE);
				if (pcBangCount < 0)
				{
					pcBangCount = Integer.MAX_VALUE;
				}
				temp.setPcBangPoints(pcBangCount);
				temp.sendMessage("Admin increased your PC point(s) by " + value + "!");
				temp.sendPacket(new ExPCCafePointInfo(pcBangCount, value, 1));
				counter++;
			}
		}
		return counter;
	}
	
	private L2PcInstance getTarget(L2PcInstance activeChar)
	{
		return ((activeChar.getTarget() != null) && (activeChar.getTarget().getActingPlayer() != null)) ? activeChar.getTarget().getActingPlayer() : activeChar;
	}
	
	private void showMenuHtml(L2PcInstance activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0, 0);
		final L2PcInstance target = getTarget(activeChar);
		final int points = target.getPcBangPoints();
		html.setHtml(HtmCache.getInstance().getHtm(activeChar.getHtmlPrefix(), "html/admin/pcbang.htm"));
		html.replace("%points%", Util.formatAdena(points));
		html.replace("%targetName%", target.getName());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}