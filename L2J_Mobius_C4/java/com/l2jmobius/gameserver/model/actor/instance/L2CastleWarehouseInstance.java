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
package com.l2jmobius.gameserver.model.actor.instance;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.model.L2Clan;
import com.l2jmobius.gameserver.network.serverpackets.ActionFailed;
import com.l2jmobius.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import com.l2jmobius.gameserver.network.serverpackets.WareHouseDepositList;
import com.l2jmobius.gameserver.network.serverpackets.WareHouseWithdrawalList;
import com.l2jmobius.gameserver.templates.L2NpcTemplate;

/**
 * @author l3x
 */
public class L2CastleWarehouseInstance extends L2FolkInstance
{
	protected static int Cond_All_False = 0;
	protected static int Cond_Busy_Because_Of_Siege = 1;
	protected static int Cond_Owner = 2;
	
	/**
	 * @param objectId
	 * @param template
	 */
	public L2CastleWarehouseInstance(int objectId, L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	private void showRetrieveWindow(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		player.setActiveWarehouse(player.getWarehouse());
		
		if (player.getActiveWarehouse().getSize() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.NOTHING_IN_WAREHOUSE));
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.fine("Showing stored items");
		}
		
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.Private));
	}
	
	private void showDepositWindow(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		player.setActiveWarehouse(player.getWarehouse());
		player.tempInventoryDisable();
		
		if (Config.DEBUG)
		{
			_log.fine("Showing items to deposit");
		}
		
		player.sendPacket(new WareHouseDepositList(player, WareHouseDepositList.Private));
	}
	
	private void showDepositWindowClan(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		if (player.getClan() != null)
		{
			if (player.getClan().getLevel() == 0)
			{
				
				player.sendPacket(new SystemMessage(SystemMessage.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
				
				return;
			}
			
			player.setActiveWarehouse(player.getClan().getWarehouse());
			player.tempInventoryDisable();
			
			if (Config.DEBUG)
			{
				_log.fine("Showing items to deposit - clan");
			}
			
			final WareHouseDepositList dl = new WareHouseDepositList(player, WareHouseDepositList.Clan);
			player.sendPacket(dl);
			
		}
	}
	
	private void showWithdrawWindowClan(L2PcInstance player)
	{
		player.sendPacket(new ActionFailed());
		if ((player.getClanPrivileges() & L2Clan.CP_CL_VIEW_WAREHOUSE) != L2Clan.CP_CL_VIEW_WAREHOUSE)
		{
			player.sendPacket(new SystemMessage(SystemMessage.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE));
			return;
		}
		if (player.getClan().getLevel() == 0)
		{
			player.sendPacket(new SystemMessage(SystemMessage.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE));
			return;
		}
		
		player.setActiveWarehouse(player.getClan().getWarehouse());
		
		if (Config.DEBUG)
		{
			_log.fine("Showing items to withdraw - clan");
		}
		
		player.sendPacket(new WareHouseWithdrawalList(player, WareHouseWithdrawalList.Clan));
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// little check to prevent enchant exploit
		if (player.getActiveEnchantItem() != null)
		{
			_log.info("Player " + player.getName() + " trying to use enchant exploit, ban this player!");
			player.logout();
			return;
		}
		
		if (command.startsWith("WithdrawP"))
		{
			showRetrieveWindow(player);
		}
		else if (command.equals("DepositP"))
		{
			showDepositWindow(player);
		}
		else if (command.equals("WithdrawC"))
		{
			showWithdrawWindowClan(player);
		}
		else if (command.equals("DepositC"))
		{
			showDepositWindowClan(player);
		}
		else if (command.startsWith("Chat"))
		{
			int val = 0;
			try
			{
				val = Integer.parseInt(command.substring(5));
			}
			catch (final IndexOutOfBoundsException ioobe)
			{
			}
			catch (final NumberFormatException nfe)
			{
			}
			showChatWindow(player, val);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		player.sendPacket(new ActionFailed());
		String filename = "data/html/castlewarehouse/castlewarehouse-no.htm";
		
		final int condition = validateCondition(player);
		if (condition > Cond_All_False)
		{
			if (condition == Cond_Busy_Because_Of_Siege)
			{
				filename = "data/html/castlewarehouse/castlewarehouse-busy.htm"; // Busy because of siege
			}
			else if (condition == Cond_Owner) // Clan owns castle
			{
				if (val == 0)
				{
					filename = "data/html/castlewarehouse/castlewarehouse.htm";
				}
				else
				{
					filename = "data/html/castlewarehouse/castlewarehouse-" + val + ".htm";
				}
			}
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		html.replace("%npcname%", getName());
		player.sendPacket(html);
	}
	
	protected int validateCondition(L2PcInstance player)
	{
		if (player.isGM())
		{
			return Cond_Owner;
		}
		
		if ((getCastle() != null) && (getCastle().getCastleId() > 0))
		{
			if (player.getClan() != null)
			{
				if (getCastle().getSiege().getIsInProgress())
				{
					return Cond_Busy_Because_Of_Siege; // Busy because of siege
				}
				else if (getCastle().getOwnerId() == player.getClanId())
				{
					return Cond_Owner;
				}
			}
		}
		
		return Cond_All_False;
	}
}