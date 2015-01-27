/*
 * Copyright (C) 2004-2015 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import com.l2jserver.gameserver.network.NpcStringId;

/**
 * @author UnAfraid
 */
public class ExQuestNpcLogList extends L2GameServerPacket
{
	private final int _questId;
	private final List<Holder> _npcLogList = new ArrayList<>();
	
	public ExQuestNpcLogList(int questId)
	{
		_questId = questId;
	}
	
	public void addNpc(int npcId, int count)
	{
		_npcLogList.add(new Holder(npcId, false, count));
	}
	
	public void addNpcString(NpcStringId npcStringId, int count)
	{
		_npcLogList.add(new Holder(npcStringId.getId(), true, count));
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xC6);
		writeD(_questId);
		writeC(_npcLogList.size());
		for (Holder holder : _npcLogList)
		{
			writeD((holder.getId()));
			writeC(holder.isNpcString() ? 0x01 : 0x00);
			writeD(holder.getCount());
		}
	}
	
	private class Holder
	{
		private final int _id;
		private final boolean _isNpcString;
		private final int _count;
		
		public Holder(int id, boolean isNpcString, int count)
		{
			_id = id;
			_isNpcString = isNpcString;
			_count = count;
		}
		
		public int getId()
		{
			return _id;
		}
		
		public boolean isNpcString()
		{
			return _isNpcString;
		}
		
		public int getCount()
		{
			return _count;
		}
	}
}