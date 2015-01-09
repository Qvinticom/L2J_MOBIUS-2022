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

import com.l2jserver.gameserver.instancemanager.MentorManager;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.network.NpcStringId;
import com.l2jserver.gameserver.network.SystemMessageId;

public final class CreatureSay extends L2GameServerPacket
{
	private final int _objectId;
	private final int _textType;
	private String _charName = null;
	private int _charId = 0;
	private String _text = null;
	private int _npcString = -1;
	private int _mask;
	private int _charLevel = -1;
	private List<String> _parameters;
	
	/**
	 * @param sender
	 * @param receiver
	 * @param name
	 * @param messageType
	 * @param text
	 */
	public CreatureSay(L2PcInstance sender, L2PcInstance receiver, String name, int messageType, String text)
	{
		_objectId = sender.getObjectId();
		_charName = name;
		_charLevel = sender.getLevel();
		_textType = messageType;
		_text = text;
		if (receiver.getFriendList().contains(sender.getObjectId()))
		{
			_mask |= 0x01;
		}
		if ((receiver.getClanId() > 0) && (receiver.getClanId() == sender.getClanId()))
		{
			_mask |= 0x02;
		}
		if ((MentorManager.getInstance().getMentee(receiver.getObjectId(), sender.getObjectId()) != null) || (MentorManager.getInstance().getMentee(sender.getObjectId(), receiver.getObjectId()) != null))
		{
			_mask |= 0x04;
		}
		if ((receiver.getAllyId() > 0) && (receiver.getAllyId() == sender.getAllyId()))
		{
			_mask |= 0x08;
		}
		
		// Does not shows level
		if (sender.isGM())
		{
			_mask |= 0x10;
		}
	}
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, int messageType, String charName, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_text = text;
	}
	
	public CreatureSay(L2PcInstance player, int messageType, String text)
	{
		_objectId = player.getObjectId();
		_textType = messageType;
		_charName = player.getAppearance().getVisibleName();
		_text = text;
	}
	
	public CreatureSay(int objectId, int messageType, int charId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, int messageType, String charName, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charName = charName;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, int messageType, int charId, SystemMessageId sysString)
	{
		_objectId = objectId;
		_textType = messageType;
		_charId = charId;
		_npcString = sysString.getId();
	}
	
	/**
	 * String parameter for argument S1,S2,.. in npcstring-e.dat
	 * @param text
	 */
	public void addStringParameter(String text)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x4A);
		writeD(_objectId);
		writeD(_textType);
		if (_charName != null)
		{
			writeS(_charName);
		}
		else
		{
			writeD(_charId);
		}
		writeD(_npcString); // High Five NPCString ID
		if (_text != null)
		{
			writeS(_text);
			if ((_charLevel > 0) && (_textType == 2))
			{
				writeC(_mask);
				if ((_mask & 0x10) == 0)
				{
					writeC(_charLevel);
				}
			}
		}
		else if (_parameters != null)
		{
			for (String s : _parameters)
			{
				writeS(s);
			}
		}
	}
	
	@Override
	public final void runImpl()
	{
		L2PcInstance _pci = getClient().getActiveChar();
		if (_pci != null)
		{
			_pci.broadcastSnoop(_textType, _charName, _text);
		}
	}
}
