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
package org.l2jmobius.gameserver.network.serverpackets;

import org.l2jmobius.Config;
import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.stats.Stat;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author -Wooden-, KenM
 */
public class ExStorageMaxCount implements IClientOutgoingPacket
{
	private Player _player;
	private int _inventory;
	private int _warehouse;
	private int _freight;
	private int _clan;
	private int _privateSell;
	private int _privateBuy;
	private int _receipeD;
	private int _recipe;
	private int _inventoryExtraSlots;
	private int _inventoryQuestItems;
	
	public ExStorageMaxCount(Player player)
	{
		if (!player.isSubclassLocked()) // Changing class.
		{
			_player = player;
			_inventory = player.getInventoryLimit();
			_warehouse = player.getWareHouseLimit();
			_freight = Config.ALT_FREIGHT_SLOTS;
			_privateSell = player.getPrivateSellStoreLimit();
			_privateBuy = player.getPrivateBuyStoreLimit();
			_clan = Config.WAREHOUSE_SLOTS_CLAN;
			_receipeD = player.getDwarfRecipeLimit();
			_recipe = player.getCommonRecipeLimit();
			_inventoryExtraSlots = (int) player.getStat().getValue(Stat.INVENTORY_NORMAL, 0);
			_inventoryQuestItems = Config.INVENTORY_MAXIMUM_QUEST_ITEMS;
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		if (_player == null)
		{
			return false;
		}
		
		OutgoingPackets.EX_STORAGE_MAX_COUNT.writeId(packet);
		packet.writeD(_inventory);
		packet.writeD(_warehouse);
		packet.writeD(_freight);
		packet.writeD(_clan);
		packet.writeD(_privateSell);
		packet.writeD(_privateBuy);
		packet.writeD(_receipeD);
		packet.writeD(_recipe);
		packet.writeD(_inventoryExtraSlots); // Belt inventory slots increase count
		packet.writeD(_inventoryQuestItems);
		packet.writeD(40); // TODO: Find me!
		packet.writeD(40); // TODO: Find me!
		return true;
	}
}
