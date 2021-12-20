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

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.model.TradeList;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Yme
 * @version $Revision: 1.2.2.1.2.3 $ $Date: 2005/03/27 15:29:39 $
 */
public class TradeOwnAdd implements IClientOutgoingPacket
{
	private final TradeList.TradeItem _item;
	
	public TradeOwnAdd(TradeList.TradeItem item)
	{
		_item = item;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.TRADE_OWN_ADD.writeId(packet);
		packet.writeH(1); // item count
		packet.writeH(_item.getItem().getType1()); // item type1
		packet.writeD(_item.getObjectId());
		packet.writeD(_item.getItem().getItemId());
		packet.writeD(_item.getCount());
		packet.writeH(_item.getItem().getType2()); // item type2
		packet.writeH(0); // ?
		packet.writeD(_item.getItem().getBodyPart()); // rev 415 slot 0006-lr.ear 0008-neck 0030-lr.finger 0040-head 0080-?? 0100-l.hand 0200-gloves 0400-chest 0800-pants 1000-feet 2000-?? 4000-r.hand 8000-r.hand
		packet.writeH(_item.getEnchant()); // enchant level
		packet.writeH(0); // ?
		packet.writeH(0);
		return true;
	}
}
