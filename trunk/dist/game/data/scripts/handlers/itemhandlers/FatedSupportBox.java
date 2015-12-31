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
package handlers.itemhandlers;

import com.l2jmobius.gameserver.data.xml.impl.CategoryData;
import com.l2jmobius.gameserver.enums.CategoryType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.handler.IItemHandler;
import com.l2jmobius.gameserver.model.actor.L2Playable;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.base.ClassId;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Mobius
 */
public class FatedSupportBox implements IItemHandler
{
	@Override
	public boolean useItem(L2Playable playable, L2ItemInstance item, boolean forceUse)
	{
		if (!playable.isPlayer())
		{
			playable.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_THIS_ITEM);
			return false;
		}
		
		final L2PcInstance player = playable.getActingPlayer();
		final Race race = player.getRace();
		final ClassId classId = player.getClassId();
		
		// Characters that have gone through their 2nd class transfer/1st liberation will be able to open the Fated Support Box at level 40.
		if ((player.getLevel() < 40) || (classId.level() < (race == Race.ERTHEIA ? 1 : 2)))
		{
			player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item));
			return false;
		}
		
		player.getInventory().destroyItem(getClass().getSimpleName(), item, 1, player, null);
		player.sendPacket(new InventoryUpdate(item));
		
		// It will stay in your inventory after use until you reach level 84.
		if (player.getLevel() > 84)
		{
			player.sendMessage("Fated Support Box was removed because your level has exceeded the maximum requirement."); // custom message
			return true;
		}
		
		switch (race)
		{
			case HUMAN:
			case ELF:
			case DARK_ELF:
			case DWARF:
			{
				if (player.isMageClass())
				{
					player.addItem(getClass().getSimpleName(), 37316, 1, player, true);
				}
				else
				{
					if (CategoryData.getInstance().isInCategory(CategoryType.SUB_GROUP_ROGUE, classId.getId()))
					{
						player.addItem(getClass().getSimpleName(), 37318, 1, player, true);
					}
					else if (CategoryData.getInstance().isInCategory(CategoryType.SUB_GROUP_KNIGHT, classId.getId()))
					{
						player.addItem(getClass().getSimpleName(), 37315, 1, player, true);
					}
					else
					{
						player.addItem(getClass().getSimpleName(), 37317, 1, player, true);
					}
				}
				break;
			}
			case ORC:
			{
				if (player.isMageClass())
				{
					player.addItem(getClass().getSimpleName(), 37321, 1, player, true);
				}
				else
				{
					player.addItem(getClass().getSimpleName(), 37320, 1, player, true);
				}
				break;
			}
			case KAMAEL:
			{
				player.addItem(getClass().getSimpleName(), 37319, 1, player, true);
				break;
			}
			case ERTHEIA:
			{
				if (player.isMageClass())
				{
					player.addItem(getClass().getSimpleName(), 26229, 1, player, true);
				}
				else
				{
					player.addItem(getClass().getSimpleName(), 26230, 1, player, true);
				}
				break;
			}
		}
		return true;
	}
}
