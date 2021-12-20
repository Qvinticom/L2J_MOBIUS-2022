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
import org.l2jmobius.gameserver.model.Skill;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.SystemMessageId;

public class SystemMessage implements IClientOutgoingPacket
{
	// Packets d d (d S/d d/d dd) -> 0 - String 1-number 2-textref npcname (1000000-1002655) 3-textref itemname 4-textref skills 5-??
	private static final int TYPE_ZONE_NAME = 7;
	private static final int TYPE_SKILL_NAME = 4;
	private static final int TYPE_ITEM_NAME = 3;
	private static final int TYPE_NPC_NAME = 2;
	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_TEXT = 0;
	
	private final int _messageId;
	private final List<Integer> _types = new ArrayList<>();
	private final List<Object> _values = new ArrayList<>();
	private int _skillLevel = 1;
	
	public SystemMessage(SystemMessageId messageId)
	{
		_messageId = messageId.getId();
	}
	
	@Deprecated
	public SystemMessage(int messageId)
	{
		_messageId = messageId;
	}
	
	public static SystemMessage sendString(String msg)
	{
		final SystemMessage sm = new SystemMessage(SystemMessageId.S1_S2);
		sm.addString(msg);
		return sm;
	}
	
	public SystemMessage addString(String text)
	{
		_types.add(TYPE_TEXT);
		_values.add(text);
		return this;
	}
	
	public SystemMessage addNumber(int number)
	{
		_types.add(TYPE_NUMBER);
		_values.add(number);
		return this;
	}
	
	public SystemMessage addNpcName(int id)
	{
		_types.add(TYPE_NPC_NAME);
		_values.add(1000000 + id);
		return this;
	}
	
	public SystemMessage addItemName(int id)
	{
		_types.add(TYPE_ITEM_NAME);
		_values.add(id);
		return this;
	}
	
	public SystemMessage addZoneName(int x, int y, int z)
	{
		_types.add(TYPE_ZONE_NAME);
		final int[] coord =
		{
			x,
			y,
			z
		};
		_values.add(coord);
		return this;
	}
	
	public SystemMessage addSkillName(int id)
	{
		return addSkillName(id, 1);
	}
	
	public SystemMessage addSkillName(int id, int lvl)
	{
		_types.add(TYPE_SKILL_NAME);
		_values.add(id);
		_skillLevel = lvl;
		return this;
	}
	
	public SystemMessage addSkillName(Skill skill)
	{
		_types.add(TYPE_SKILL_NAME);
		_values.add(skill.getId());
		_skillLevel = skill.getLevel();
		return this;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SYSTEM_MESSAGE.writeId(packet);
		packet.writeD(_messageId);
		packet.writeD(_types.size());
		for (int i = 0; i < _types.size(); i++)
		{
			final int t = _types.get(i).intValue();
			packet.writeD(t);
			switch (t)
			{
				case TYPE_TEXT:
				{
					packet.writeS((String) _values.get(i));
					break;
				}
				case TYPE_NUMBER:
				case TYPE_NPC_NAME:
				case TYPE_ITEM_NAME:
				{
					final int t1 = ((Integer) _values.get(i)).intValue();
					packet.writeD(t1);
					break;
				}
				case TYPE_SKILL_NAME:
				{
					final int t1 = ((Integer) _values.get(i)).intValue();
					packet.writeD(t1); // Skill Id
					packet.writeD(_skillLevel); // Skill lvl
					break;
				}
				case TYPE_ZONE_NAME:
				{
					final int t1 = ((int[]) _values.get(i))[0];
					final int t2 = ((int[]) _values.get(i))[1];
					final int t3 = ((int[]) _values.get(i))[2];
					packet.writeD(t1);
					packet.writeD(t2);
					packet.writeD(t3);
					break;
				}
			}
		}
		return true;
	}
	
	public int getMessageID()
	{
		return _messageId;
	}
	
	public static SystemMessage getSystemMessage(SystemMessageId smId)
	{
		return new SystemMessage(smId);
	}
}