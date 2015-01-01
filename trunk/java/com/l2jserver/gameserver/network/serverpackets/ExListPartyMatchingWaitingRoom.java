/*
 * Copyright (C) 2004-2014 L2J Server
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

import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.model.PartyMatchWaitingList;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.instancezone.InstanceWorld;

/**
 * @author Gnacik
 */
public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private final L2PcInstance _activeChar;
	// private final int _page;
	private final int _minlvl;
	private final int _maxlvl;
	private final int _mode;
	private final int _currentTemplateId;
	private final List<L2PcInstance> _members;
	
	public ExListPartyMatchingWaitingRoom(L2PcInstance player, int page, int minlvl, int maxlvl, int mode)
	{
		_activeChar = player;
		// _page = page;
		_minlvl = minlvl;
		_maxlvl = maxlvl;
		_mode = mode;
		_members = new ArrayList<>();
		final InstanceWorld world = InstanceManager.getInstance().getPlayerWorld(player);
		_currentTemplateId = (world != null) && (world.getTemplateId() >= 0) ? world.getTemplateId() : -1;
	}
	
	@Override
	protected void writeImpl()
	{
		writeC(0xFE);
		writeH(0x36);
		if (_mode == 0)
		{
			writeD(0);
			writeD(0);
			return;
		}
		
		for (L2PcInstance cha : PartyMatchWaitingList.getInstance().getPlayers())
		{
			if ((cha == null) || (cha == _activeChar))
			{
				continue;
			}
			
			if (!cha.isPartyWaiting())
			{
				PartyMatchWaitingList.getInstance().removePlayer(cha);
				continue;
			}
			
			else if ((cha.getLevel() < _minlvl) || (cha.getLevel() > _maxlvl))
			{
				continue;
			}
			
			_members.add(cha);
		}
		
		writeD(0x01); // Page?
		writeD(_members.size());
		for (L2PcInstance member : _members)
		{
			writeS(member.getName());
			writeD(member.getActiveClass());
			writeD(member.getLevel());
			writeD(_currentTemplateId);
			writeD(0x00); // TODO: Instance ID reuse size
			// TODO: Loop for instanceId
		}
	}
}