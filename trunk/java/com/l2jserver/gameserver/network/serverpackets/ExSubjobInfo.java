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

import com.l2jserver.gameserver.enums.SubclassType;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.SubClass;

/**
 * @author Sdw
 */
public class ExSubjobInfo extends L2GameServerPacket
{
	private final int _currClassId;
	private final int _currClassIndex;
	private final int _currRace;
	private final List<SubInfo> _subs;
	
	public ExSubjobInfo(L2PcInstance player)
	{
		_subs = new ArrayList<>();
		
		_currClassId = player.getClassId().getId();
		_currClassIndex = player.getClassIndex();
		_currRace = player.getRace().ordinal();
		
		_subs.add(0, new SubInfo(player));
		
		for (SubClass sub : player.getSubClasses().values())
		{
			_subs.add(new SubInfo(sub));
		}
	}
	
	private final class SubInfo
	{
		private final int _index;
		private final int _classId;
		private final int _level;
		private final int _type;
		
		public SubInfo(SubClass sub)
		{
			_index = sub.getClassIndex();
			_classId = sub.getClassId();
			_level = sub.getLevel();
			_type = SubclassType.SUBCLASS.ordinal();
		}
		
		public SubInfo(L2PcInstance player)
		{
			_index = 0;
			_classId = player.getBaseClass();
			_level = player.getStat().getBaseLevel();
			_type = SubclassType.BASECLASS.ordinal();
		}
		
		public int getIndex()
		{
			return _index;
		}
		
		public int getClassId()
		{
			return _classId;
		}
		
		public int getLevel()
		{
			return _level;
		}
		
		public int getType()
		{
			return _type;
		}
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0xEA);
		writeC(_currClassIndex);
		writeD(_currClassId);
		writeD(_currRace);
		writeD(_subs.size());
		for (SubInfo sub : _subs)
		{
			writeD(sub.getIndex());
			writeD(sub.getClassId());
			writeD(sub.getLevel());
			writeC(sub.getType());
		}
	}
}