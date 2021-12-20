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
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.instancemanager.CastleManorManager;
import org.l2jmobius.gameserver.model.SeedProduction;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author l3x
 */
public class BuyListSeed implements IClientOutgoingPacket
{
	private final int _manorId;
	private final long _money;
	private final List<SeedProduction> _list = new ArrayList<>();
	
	public BuyListSeed(long currentMoney, int castleId)
	{
		_money = currentMoney;
		_manorId = castleId;
		for (SeedProduction s : CastleManorManager.getInstance().getSeedProduction(castleId, false))
		{
			if ((s.getAmount() > 0) && (s.getPrice() > 0))
			{
				_list.add(s);
			}
		}
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.BUY_LIST_SEED.writeId(packet);
		packet.writeQ(_money); // current money
		packet.writeD(_manorId); // manor id
		if (!_list.isEmpty())
		{
			packet.writeH(_list.size()); // list length
			for (SeedProduction s : _list)
			{
				packet.writeH(4); // item->type1
				packet.writeD(0); // objectId
				packet.writeD(s.getId()); // item id
				packet.writeQ(s.getAmount()); // item count
				packet.writeH(4); // item->type2
				packet.writeH(0); // unknown :)
				packet.writeQ(s.getPrice()); // price
			}
			_list.clear();
		}
		else
		{
			packet.writeH(0);
		}
		return true;
	}
}