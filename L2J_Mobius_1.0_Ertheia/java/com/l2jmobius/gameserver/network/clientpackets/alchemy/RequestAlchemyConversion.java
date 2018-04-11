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
package com.l2jmobius.gameserver.network.clientpackets.alchemy;

import java.util.HashSet;
import java.util.Set;

import com.l2jmobius.commons.network.PacketReader;
import com.l2jmobius.commons.util.Rnd;
import com.l2jmobius.gameserver.data.xml.impl.AlchemyData;
import com.l2jmobius.gameserver.enums.PrivateStoreType;
import com.l2jmobius.gameserver.enums.Race;
import com.l2jmobius.gameserver.model.actor.instance.L2PcInstance;
import com.l2jmobius.gameserver.model.alchemy.AlchemyCraftData;
import com.l2jmobius.gameserver.model.holders.ItemHolder;
import com.l2jmobius.gameserver.model.items.instance.L2ItemInstance;
import com.l2jmobius.gameserver.network.L2GameClient;
import com.l2jmobius.gameserver.network.SystemMessageId;
import com.l2jmobius.gameserver.network.clientpackets.IClientIncomingPacket;
import com.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jmobius.gameserver.network.serverpackets.alchemy.ExAlchemyConversion;
import com.l2jmobius.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * @author Sdw
 */
public class RequestAlchemyConversion implements IClientIncomingPacket
{
	private int _craftTimes;
	private int _skillId;
	private int _skillLevel;
	
	private final Set<ItemHolder> _ingredients = new HashSet<>();
	
	@Override
	public boolean read(L2GameClient client, PacketReader packet)
	{
		_craftTimes = packet.readD();
		packet.readH(); // TODO: Find me
		_skillId = packet.readD();
		_skillLevel = packet.readD();
		
		final int ingredientsSize = packet.readD();
		for (int i = 0; i < ingredientsSize; i++)
		{
			_ingredients.add(new ItemHolder(packet.readD(), packet.readQ()));
		}
		return true;
	}
	
	@Override
	public void run(L2GameClient client)
	{
		final L2PcInstance player = client.getActiveChar();
		if ((player == null) || (player.getRace() != Race.ERTHEIA))
		{
			return;
		}
		
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(player))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_DURING_BATTLE);
			return;
		}
		else if (player.isInStoreMode() || (player.getPrivateStoreType() != PrivateStoreType.NONE))
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_TRADING_OR_USING_A_PRIVATE_STORE_OR_SHOP);
			return;
		}
		else if (player.isDead())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_DEAD);
			return;
		}
		else if (player.isMovementDisabled())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_ALCHEMY_WHILE_IMMOBILE);
			return;
		}
		
		final AlchemyCraftData data = AlchemyData.getInstance().getCraftData(_skillId, _skillLevel);
		if (data == null)
		{
			LOGGER.warning("Missing AlchemyData for skillId: " + _skillId + ", skillLevel: " + _skillLevel);
			return;
		}
		
		// TODO : Implement this
		// if (!_ingredients.equals(data.getIngredients()))
		// {
		// LOGGER.warning("Client ingredients are not same as server ingredients for alchemy conversion player: "+ +"", player);
		// return;
		// }
		
		// TODO: Figure out the chance
		final int baseChance;
		switch (data.getGrade())
		{
			case 1: // Elementary
			{
				baseChance = 100;
				break;
			}
			case 2: // Intermediate
			{
				baseChance = 80;
				break;
			}
			case 3: // Advanced
			{
				baseChance = 60;
				break;
			}
			default: // Master
			{
				baseChance = 50;
				break;
			}
		}
		
		int successCount = 0;
		int failureCount = 0;
		
		// Run _craftItems iteration of item craft
		final InventoryUpdate ui = new InventoryUpdate();
		CRAFTLOOP: for (int i = 0; i < _craftTimes; i++)
		{
			// for each tries, check if player have enough items and destroy
			for (ItemHolder ingredient : data.getIngredients())
			{
				final L2ItemInstance item = player.getInventory().getItemByItemId(ingredient.getId());
				if (item == null)
				{
					break CRAFTLOOP;
				}
				if (item.getCount() < ingredient.getCount())
				{
					break CRAFTLOOP;
				}
				
				player.getInventory().destroyItem("Alchemy", item, ingredient.getCount(), player, null);
				ui.addItem(item);
			}
			
			if (Rnd.get(100) < baseChance)
			{
				successCount++;
			}
			else
			{
				failureCount++;
			}
		}
		
		if (successCount > 0)
		{
			final L2ItemInstance item = player.getInventory().addItem("Alchemy", data.getProductionSuccess().getId(), data.getProductionSuccess().getCount() * successCount, player, null);
			ui.addItem(item);
		}
		
		if (failureCount > 0)
		{
			final L2ItemInstance item = player.getInventory().addItem("Alchemy", data.getProductionFailure().getId(), data.getProductionFailure().getCount() * failureCount, player, null);
			ui.addItem(item);
		}
		
		player.sendPacket(new ExAlchemyConversion(successCount, failureCount));
		player.sendInventoryUpdate(ui);
		
	}
}
