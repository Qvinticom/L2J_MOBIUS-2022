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
import org.l2jmobius.gameserver.model.actor.Player;
import org.l2jmobius.gameserver.model.clan.Clan;
import org.l2jmobius.gameserver.model.item.ItemTemplate;
import org.l2jmobius.gameserver.model.item.Weapon;
import org.l2jmobius.gameserver.model.item.instance.Item;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * Sdh(h dddhh [dhhh] d) Sdh ddddd ddddd ddddd ddddd
 * @version $Revision: 1.1.2.1.2.5 $ $Date: 2007/11/26 16:10:05 $
 */
public class GMViewWarehouseWithdrawList implements IClientOutgoingPacket
{
	private final Collection<Item> _items;
	private final String _playerName;
	private final int _money;
	
	public GMViewWarehouseWithdrawList(Player player)
	{
		_items = player.getWarehouse().getItems();
		_playerName = player.getName();
		_money = player.getAdena();
	}
	
	public GMViewWarehouseWithdrawList(Clan clan)
	{
		_playerName = clan.getLeaderName();
		_items = clan.getWarehouse().getItems();
		_money = clan.getWarehouse().getAdena();
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.GM_VIEW_WAREHOUSE_WITHDRAW_LIST.writeId(packet);
		packet.writeS(_playerName);
		packet.writeD(_money);
		packet.writeH(_items.size());
		for (Item item : _items)
		{
			packet.writeH(item.getItem().getType1());
			packet.writeD(item.getObjectId());
			packet.writeD(item.getItemId());
			packet.writeD(item.getCount());
			packet.writeH(item.getItem().getType2());
			packet.writeH(item.getCustomType1());
			switch (item.getItem().getType2())
			{
				case ItemTemplate.TYPE2_WEAPON:
				{
					packet.writeD(item.getItem().getBodyPart());
					packet.writeH(item.getEnchantLevel());
					packet.writeH(((Weapon) item.getItem()).getSoulShotCount());
					packet.writeH(((Weapon) item.getItem()).getSpiritShotCount());
					break;
				}
				case ItemTemplate.TYPE2_SHIELD_ARMOR:
				case ItemTemplate.TYPE2_ACCESSORY:
				case ItemTemplate.TYPE2_PET_WOLF:
				case ItemTemplate.TYPE2_PET_HATCHLING:
				case ItemTemplate.TYPE2_PET_STRIDER:
				case ItemTemplate.TYPE2_PET_BABY:
				{
					packet.writeD(item.getItem().getBodyPart());
					packet.writeH(item.getEnchantLevel());
					packet.writeH(0);
					packet.writeH(0);
					break;
				}
			}
			packet.writeD(item.getObjectId());
		}
		return true;
	}
}
