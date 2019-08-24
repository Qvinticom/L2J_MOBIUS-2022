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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;

public class SellListProcure extends GameServerPacket
{
	private final PlayerInstance _player;
	private final int _money;
	private final Map<ItemInstance, Integer> _sellList = new HashMap<>();
	private List<CropProcure> _procureList = new ArrayList<>();
	private final int _castle;
	
	public SellListProcure(PlayerInstance player, int castleId)
	{
		_money = player.getAdena();
		_player = player;
		_castle = castleId;
		_procureList = CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
		for (CropProcure c : _procureList)
		{
			final ItemInstance item = _player.getInventory().getItemByItemId(c.getId());
			if ((item != null) && (c.getAmount() > 0))
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE9);
		writeD(_money); // money
		writeD(0x00); // lease ?
		writeH(_sellList.size()); // list size
		
		for (ItemInstance item : _sellList.keySet())
		{
			writeH(item.getItem().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(_sellList.get(item)); // count
			writeH(item.getItem().getType2());
			writeH(0); // unknown
			writeD(0); // price, u shouldnt get any adena for crops, only raw materials
		}
	}
}
