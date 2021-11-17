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
import java.util.Map.Entry;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.CastleManager;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager.CropProcure;
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.items.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

public class SellListProcure implements IClientOutgoingPacket
{
	private final Player _player;
	private final int _money;
	private final Map<Item, Integer> _sellList = new HashMap<>();
	private List<CropProcure> _procureList = new ArrayList<>();
	private final int _castle;
	
	public SellListProcure(Player player, int castleId)
	{
		_money = player.getAdena();
		_player = player;
		_castle = castleId;
		_procureList = CastleManager.getInstance().getCastleById(_castle).getCropProcure(0);
		for (CropProcure c : _procureList)
		{
			final Item item = _player.getInventory().getItemByItemId(c.getId());
			if ((item != null) && (c.getAmount() > 0))
			{
				_sellList.put(item, c.getAmount());
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SELL_LIST_PROCURE.writeId(packet);
		packet.writeD(_money); // money
		packet.writeD(0x00); // lease ?
		packet.writeH(_sellList.size()); // list size
		
		for (Entry<Item, Integer> entry : _sellList.entrySet())
		{
			final Item item = entry.getKey();
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItemId());
			packet.writeD(entry.getValue()); // count
			packet.writeH(item.getItem().getType2());
			packet.writeH(0); // unknown
			packet.writeD(0); // price, u shouldnt get any adena for crops, only raw materials
		}
		return true;
	}
}
