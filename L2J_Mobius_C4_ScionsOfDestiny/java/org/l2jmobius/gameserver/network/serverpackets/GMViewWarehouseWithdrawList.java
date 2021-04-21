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

import java.util.Collection;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.model.items.Item;
import org.l2jmobius.gameserver.model.items.Weapon;
import org.l2jmobius.gameserver.model.items.instance.ItemInstance;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Sdh(h dddhh [dhhh] d) Sdh ddddd ddddd ddddd ddddd
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList implements IClientOutgoingPacket
{
	private final Collection<ItemInstance> _items;
	private final String _playerName;
	private final PlayerInstance _player;
	private final int _money;
	
	public GMViewWarehouseWithdrawList(PlayerInstance player)
	{
		_player = player;
		_items = _player.getWarehouse().getItems();
		_playerName = _player.getName();
		_money = _player.getAdena();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GM_VIEW_WAREHOUSE_WITHDRAW_LIST.writeId(packet);
		packet.writeS(_playerName);
		packet.writeD(_money);
		packet.writeH(_items.size());
		
		for (ItemInstance item : _items)
		{
			packet.writeH(item.getItem().getType1());
			
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItemId());
			packet.writeD(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(item.getCustomType1());
			
			switch (item.getItem().getType2())
			{
				case Item.TYPE2_WEAPON:
				{
					packet.writeD(item.getItem().getBodyPart());
					packet.writeH(item.getEnchantLevel());
					packet.writeH(((Weapon) item.getItem()).getSoulShotCount());
					packet.writeH(((Weapon) item.getItem()).getSpiritShotCount());
					break;
				}
				case Item.TYPE2_SHIELD_ARMOR:
				case Item.TYPE2_ACCESSORY:
				case Item.TYPE2_PET_WOLF:
				case Item.TYPE2_PET_HATCHLING:
				case Item.TYPE2_PET_STRIDER:
				case Item.TYPE2_PET_BABY:
				{
					packet.writeD(item.getItem().getBodyPart());
					packet.writeH(item.getEnchantLevel());
					packet.writeH(0x00);
					packet.writeH(0x00);
					break;
				}
			}
			
			packet.writeD(item.getObjectId());
		}
		return true;
	}
}
