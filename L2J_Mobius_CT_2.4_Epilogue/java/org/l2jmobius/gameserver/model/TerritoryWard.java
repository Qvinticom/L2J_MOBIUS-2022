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
package org.l2jmobius.gameserver.model;

import org.l2jmobius.Config;
import org.l2jmobius.gameserver.data.ItemTable;
import org.l2jmobius.gameserver.instancemanager.TerritoryWarManager;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.SystemMessageId;
import org.l2jmobius.gameserver.network.serverpackets.InventoryUpdate;
import org.l2jmobius.gameserver.network.serverpackets.ItemList;
import org.l2jmobius.gameserver.network.serverpackets.SystemMessage;

public class TerritoryWard
{
	protected Player _player = null;
	public int playerId = 0;
	private Item _item = null;
	private Npc _npc = null;
	
	private Location _location;
	private Location _oldLocation;
	
	private final int _itemId;
	private int _ownerCastleId;
	
	private final int _territoryId;
	
	public TerritoryWard(int territoryId, int x, int y, int z, int heading, int itemId, int castleId, Npc npc)
	{
		_territoryId = territoryId;
		_location = new Location(x, y, z, heading);
		_itemId = itemId;
		_ownerCastleId = castleId;
		_npc = npc;
	}
	
	public int getTerritoryId()
	{
		return _territoryId;
	}
	
	public int getOwnerCastleId()
	{
		return _ownerCastleId;
	}
	
	public void setOwnerCastleId(int newOwner)
	{
		_ownerCastleId = newOwner;
	}
	
	public Npc getNpc()
	{
		return _npc;
	}
	
	public void setNpc(Npc npc)
	{
		_npc = npc;
	}
	
	public Player getPlayer()
	{
		return _player;
	}
	
	public synchronized void spawnBack()
	{
		if (_player != null)
		{
			dropIt();
		}
		
		// Init the dropped WardInstance and add it in the world as a visible object at the position where last Pc got it
		_npc = TerritoryWarManager.getInstance().spawnNPC(36491 + _territoryId, _oldLocation);
	}
	
	public synchronized void spawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		
		// Init the dropped WardInstance and add it in the world as a visible object at the position where Pc was last
		_npc = TerritoryWarManager.getInstance().spawnNPC(36491 + _territoryId, _location);
	}
	
	public synchronized void unSpawnMe()
	{
		if (_player != null)
		{
			dropIt();
		}
		if ((_npc != null) && !_npc.isDecayed())
		{
			_npc.deleteMe();
		}
	}
	
	public boolean activate(Player player, Item item)
	{
		if (player.isMounted())
		{
			player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		else if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(player) == 0)
		{
			player.sendMessage("Non participants can't pickup Territory Wards!");
			player.destroyItem("CombatFlag", item, null, true);
			spawnMe();
			return false;
		}
		
		// Player holding it data
		_player = player;
		playerId = _player.getObjectId();
		_oldLocation = new Location(_npc.getX(), _npc.getY(), _npc.getZ(), _npc.getHeading());
		_npc = null;
		
		// Equip with the weapon
		if (item == null)
		{
			_item = ItemTable.getInstance().createItem("Combat", _itemId, 1, null, null);
		}
		else
		{
			_item = item;
		}
		_player.getInventory().equipItem(_item);
		final SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_EQUIPPED_YOUR_S1);
		sm.addItemName(_item);
		_player.sendPacket(sm);
		
		// Refresh inventory
		if (!Config.FORCE_INVENTORY_UPDATE)
		{
			final InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(_item);
			_player.sendPacket(iu);
		}
		else
		{
			_player.sendPacket(new ItemList(_player, false));
		}
		
		// Refresh player stats
		_player.broadcastUserInfo();
		_player.setCombatFlagEquipped(true);
		_player.sendPacket(SystemMessageId.YOU_VE_ACQUIRED_THE_WARD_MOVE_QUICKLY_TO_YOUR_FORCES_OUTPOST);
		TerritoryWarManager.getInstance().giveTWPoint(player, _territoryId, 5);
		return true;
	}
	
	public void dropIt()
	{
		// Reset player stats
		_player.setCombatFlagEquipped(false);
		final int slot = _player.getInventory().getSlotFromItem(_item);
		_player.getInventory().unEquipItemInBodySlot(slot);
		_player.destroyItem("CombatFlag", _item, null, true);
		_item = null;
		_player.broadcastUserInfo();
		_location = new Location(_player.getX(), _player.getY(), _player.getZ(), _player.getHeading());
		_player = null;
		playerId = 0;
	}
}
