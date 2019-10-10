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
package org.l2jmobius.gameserver.network.serverpackets.timedhunting;

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.serverpackets.IClientOutgoingPacket;

public class TimedHuntingZoneList implements IClientOutgoingPacket
{
	public TimedHuntingZoneList()
	{
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.EX_TIME_RESTRICT_FIELD_LIST.writeId(packet);
		
		List<TimeRestrictedFieldInfo> infos = new ArrayList<>();
		
		addField(infos);
		
		packet.writeD(infos.size());
		
		for (TimeRestrictedFieldInfo info : infos)
		{
			packet.writeD(info.requiredItems.size());
			
			for (FieldRequiredItem item : info.requiredItems)
			{
				packet.writeD(item.itemId);
				packet.writeQ(item.count);
			}
			
			packet.writeD(info.resetCycle);
			packet.writeD(info.fieldId);
			packet.writeD(info.minLevel);
			packet.writeD(info.maxLevel);
			packet.writeD(info.remainTimeBase);
			packet.writeD(info.remainTime);
			packet.writeD(info.remainTimeMax);
			packet.writeD(info.remainRefillTime);
			packet.writeD(info.refillTimeMax);
			packet.writeC(info.fieldActivated ? 1 : 0);
		}
		
		return true;
	}
	
	private void addField(List<TimeRestrictedFieldInfo> infos)
	{
		final TimeRestrictedFieldInfo field = new TimeRestrictedFieldInfo();
		field.resetCycle = 1;
		field.fieldId = 2;
		field.minLevel = 78;
		field.maxLevel = 999;
		field.remainTimeBase = 3600;
		field.remainTime = 3600;
		field.remainTimeMax = 21600;
		field.remainRefillTime = 18000;
		field.refillTimeMax = 18000;
		field.fieldActivated = true;
		
		final FieldRequiredItem item = new FieldRequiredItem();
		item.itemId = 57;
		item.count = 10000;
		
		field.requiredItems = List.of(item);
		infos.add(field);
	}
	
	static class TimeRestrictedFieldInfo
	{
		List<FieldRequiredItem> requiredItems;
		int resetCycle;
		int fieldId;
		int minLevel;
		int maxLevel;
		int remainTimeBase;
		int remainTime;
		int remainTimeMax;
		int remainRefillTime;
		int refillTimeMax;
		boolean fieldActivated;
		
	}
	
	static class FieldRequiredItem
	{
		int itemId;
		long count;
	}
}