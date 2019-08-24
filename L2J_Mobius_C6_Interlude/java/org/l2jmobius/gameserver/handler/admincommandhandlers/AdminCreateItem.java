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
package org.l2jmobius.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.l2jmobius.gameserver.datatables.xml.ItemTable;
import org.l2jmobius.gameserver.handler.IAdminCommandHandler;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;
import org.l2jmobius.gameserver.util.BuilderUtil;

/**
 * This class handles following admin commands: - itemcreate = show menu - create_item <id> [num] = creates num items with respective id, if num is not specified, assumes 1.
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/11 10:06:06 $
 */
public class AdminCreateItem implements IAdminCommandHandler
{
	private static Logger LOGGER = Logger.getLogger(AdminCreateItem.class.getName());
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_l2jmobius",
		"admin_itemcreate",
		"admin_create_item",
		"admin_mass_create",
		"admin_clear_inventory"
	};
	
	private enum CommandEnum
	{
		admin_itemcreate,
		admin_create_item,
		admin_mass_create,
		admin_clear_inventory
	}
	
	@Override
	public boolean useAdminCommand(String command, PlayerInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command);
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_itemcreate:
			{
				AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
				return true;
			}
			case admin_create_item:
			{
				if (st.hasMoreTokens())
				{
					if (st.countTokens() == 2)
					{
						final String id = st.nextToken();
						final String num = st.nextToken();
						int idval = 0;
						int numval = 0;
						try
						{
							idval = Integer.parseInt(id);
							numval = Integer.parseInt(num);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
							return false;
						}
						if ((idval > 0) && (numval > 0))
						{
							createItem(activeChar, idval, numval);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
					else if (st.countTokens() == 1)
					{
						final String id = st.nextToken();
						int idval = 0;
						try
						{
							idval = Integer.parseInt(id);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
							return false;
						}
						if (idval > 0)
						{
							createItem(activeChar, idval, 1);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //itemcreate <itemId> (number value > 0) [amount] (number value > 0)");
						return false;
					}
				}
				else
				{
					AdminHelpPage.showHelpPage(activeChar, "itemcreation.htm");
					return true;
				}
				return false;
			}
			case admin_mass_create:
			{
				if (st.hasMoreTokens())
				{
					if (st.countTokens() == 2)
					{
						final String id = st.nextToken();
						final String num = st.nextToken();
						int idval = 0;
						int numval = 0;
						try
						{
							idval = Integer.parseInt(id);
							numval = Integer.parseInt(num);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
							return false;
						}
						if ((idval > 0) && (numval > 0))
						{
							massCreateItem(activeChar, idval, numval);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
						return false;
					}
					else if (st.countTokens() == 1)
					{
						final String id = st.nextToken();
						int idval = 0;
						try
						{
							idval = Integer.parseInt(id);
						}
						catch (NumberFormatException e)
						{
							BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
							return false;
						}
						if (idval > 0)
						{
							massCreateItem(activeChar, idval, 1);
							return true;
						}
						BuilderUtil.sendSysMessage(activeChar, "Usage: //mass_create <itemId> <amount>");
						return false;
					}
				}
				return false;
			}
			case admin_clear_inventory:
			{
				removeAllItems(activeChar);
				return true;
			}
			default:
			{
				return false;
			}
		}
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
	
	private void createItem(PlayerInstance activeChar, int id, int num)
	{
		if (num > 20)
		{
			Item template = ItemTable.getInstance().getTemplate(id);
			
			if ((template != null) && !template.isStackable())
			{
				BuilderUtil.sendSysMessage(activeChar, "This item does not stack - Creation aborted.");
				return;
			}
		}
		
		PlayerInstance Player = null;
		
		if (activeChar.getTarget() != null)
		{
			if (activeChar.getTarget() instanceof PlayerInstance)
			{
				if ((activeChar.getAccessLevel().getLevel() > 0) && (activeChar.getAccessLevel().getLevel() < 3))
				{
					Player = (PlayerInstance) activeChar.getTarget();
				}
				else
				{
					BuilderUtil.sendSysMessage(activeChar, "You have not right to create item on another player");
					return;
				}
			}
			else
			{
				BuilderUtil.sendSysMessage(activeChar, "You can add an item only to a character.");
				return;
			}
		}
		
		if (Player == null)
		{
			activeChar.setTarget(activeChar);
			Player = activeChar;
		}
		
		Player.getInventory().addItem("Admin", id, num, Player, null);
		ItemList il = new ItemList(Player, true);
		Player.sendPacket(il);
		if (activeChar.getName().equalsIgnoreCase(Player.getName()))
		{
			BuilderUtil.sendSysMessage(activeChar, "You have spawned " + num + " item(s) number " + id + " in your inventory.");
		}
		else
		{
			BuilderUtil.sendSysMessage(activeChar, "You have spawned " + num + " item(s) number " + id + " in " + Player.getName() + "'s inventory.");
			Player.sendMessage("Admin has spawned " + num + " item(s) number " + id + " in your inventory.");
		}
	}
	
	private void massCreateItem(PlayerInstance activeChar, int id, int num)
	{
		if (num > 20)
		{
			final Item template = ItemTable.getInstance().getTemplate(id);
			if ((template != null) && !template.isStackable())
			{
				BuilderUtil.sendSysMessage(activeChar, "This item does not stack - Creation aborted.");
				return;
			}
		}
		
		int i = 0;
		ItemInstance item = null;
		for (PlayerInstance player : World.getInstance().getAllPlayers())
		{
			player.sendMessage("Admin is rewarding all online players.");
			item = player.getInventory().addItem("Admin", id, num, null, null);
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			player.sendPacket(iu);
			final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
			sm.addItemName(item.getItemId());
			sm.addNumber(num);
			player.sendPacket(sm);
			i++;
		}
		BuilderUtil.sendSysMessage(activeChar, "Mass-created items in the inventory of " + i + " player(s).");
		LOGGER.info("GM " + activeChar.getName() + " mass_created item Id: " + id + " (" + num + ")");
	}
	
	private void removeAllItems(PlayerInstance activeChar)
	{
		for (ItemInstance item : activeChar.getInventory().getItems())
		{
			if (item.getItemLocation() == ItemInstance.ItemLocation.INVENTORY)
			{
				activeChar.getInventory().destroyItem("Destroy", item.getObjectId(), item.getCount(), activeChar, null);
			}
		}
		activeChar.sendPacket(new ItemList(activeChar, false));
		BuilderUtil.sendSysMessage(activeChar, "Your inventory has been cleared.");
	}
}
