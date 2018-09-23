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
package com.l2jmobius.gameserver.handler.admincommandhandlers;

import com.l2jmobius.gameserver.ai.CtrlIntention;
import com.l2jmobius.gameserver.datatables.sql.ClanTable;
import com.l2jmobius.gameserver.handler.IAdminCommandHandler;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.model.L2Party;
import com.l2jmobius.gameserver.model.L2World;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - recallparty - recallclan - recallally
 * @author Yamaneko
 */
public class AdminMassRecall implements IAdminCommandHandler
{
	private static String[] _adminCommands =
	{
		"admin_recallclan",
		"admin_recallparty",
		"admin_recallally"
	};
	
	@Override
	public boolean useAdminCommand(String command, L2PcInstance activeChar)
	{
		if (command.startsWith("admin_recallclan"))
		{
			try
			{
				String val = command.substring(17).trim();
				
				L2Clan clan = ClanTable.getInstance().getClanByName(val);
				
				if (clan == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "This clan doesn't exists.");
					return true;
				}
				
				L2PcInstance[] m = clan.getOnlineMembers("");
				
				for (L2PcInstance element : m)
				{
					Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Error in recallclan command.");
			}
		}
		else if (command.startsWith("admin_recallally"))
		{
			try
			{
				String val = command.substring(17).trim();
				L2Clan clan = ClanTable.getInstance().getClanByName(val);
				
				if (clan == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "This clan doesn't exists.");
					return true;
				}
				
				final int ally = clan.getAllyId();
				
				if (ally == 0)
				{
					L2PcInstance[] m = clan.getOnlineMembers("");
					
					for (L2PcInstance element : m)
					{
						Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
					}
				}
				else
				{
					for (L2Clan aclan : ClanTable.getInstance().getClans())
					{
						if (aclan.getAllyId() == ally)
						{
							L2PcInstance[] m = aclan.getOnlineMembers("");
							
							for (L2PcInstance element : m)
							{
								Teleport(element, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Error in recallally command.");
			}
		}
		else if (command.startsWith("admin_recallparty"))
		{
			try
			{
				String val = command.substring(18).trim();
				L2PcInstance player = L2World.getInstance().getPlayer(val);
				
				if (player == null)
				{
					BuilderUtil.sendSysMessage(activeChar, "Target error.");
					return true;
				}
				
				if (!player.isInParty())
				{
					BuilderUtil.sendSysMessage(activeChar, "Player is not in party.");
					return true;
				}
				
				L2Party p = player.getParty();
				
				for (L2PcInstance ppl : p.getPartyMembers())
				{
					Teleport(ppl, activeChar.getX(), activeChar.getY(), activeChar.getZ(), "Admin is teleporting you");
				}
			}
			catch (Exception e)
			{
				BuilderUtil.sendSysMessage(activeChar, "Error in recallparty command.");
			}
		}
		return true;
	}
	
	private void Teleport(L2PcInstance player, int X, int Y, int Z, String Message)
	{
		player.sendMessage(Message);
		player.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		player.teleToLocation(X, Y, Z, true);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return _adminCommands;
	}
}
