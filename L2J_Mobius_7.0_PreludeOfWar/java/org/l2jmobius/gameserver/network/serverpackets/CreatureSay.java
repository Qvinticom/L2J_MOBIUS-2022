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
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.instancemanager.MentorManager;
import org.l2jmobius.gameserver.instancemanager.RankManager;
import org.l2jmobius.gameserver.model.World;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.model.actor.instance.PlayerInstance;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.OutgoingPackets;
import org.l2jmobius.gameserver.network.SystemMessageId;

public class CreatureSay implements IClientOutgoingPacket
{
	private final int _objectId;
	private final ChatType _chatType;
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
	public CreatureSay(PlayerInstance sender, PlayerInstance receiver, String name, ChatType messageType, String text)
	{
		_objectId = sender.getObjectId();
		_charName = name;
		_charLevel = sender.getLevel();
		_chatType = messageType;
		_text = text;
		if (receiver != null)
		{
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
		}
		
		// Does not shows level
		if (sender.isGM())
		{
			_mask |= 0x10;
		}
	}
	
	/**
	 * Used by fake players.
	 * @param sender
	 * @param name
	 * @param messageType
	 * @param text
	 */
	public CreatureSay(Npc sender, String name, ChatType messageType, String text)
	{
		_objectId = sender.getObjectId();
		_charName = name;
		_charLevel = sender.getLevel();
		_chatType = messageType;
		_text = text;
	}
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param charName
	 * @param text
	 */
	public CreatureSay(int objectId, ChatType messageType, String charName, String text)
	{
		_objectId = objectId;
		_chatType = messageType;
		_charName = charName;
		_text = text;
	}
	
	public CreatureSay(PlayerInstance player, ChatType messageType, String text)
	{
		_objectId = player.getObjectId();
		_chatType = messageType;
		_charName = player.getAppearance().getVisibleName();
		_text = text;
	}
	
	public CreatureSay(int objectId, ChatType messageType, int charId, NpcStringId npcString)
	{
		_objectId = objectId;
		_chatType = messageType;
		_charId = charId;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, ChatType messageType, String charName, NpcStringId npcString)
	{
		_objectId = objectId;
		_chatType = messageType;
		_charName = charName;
		_npcString = npcString.getId();
	}
	
	public CreatureSay(int objectId, ChatType messageType, int charId, SystemMessageId sysString)
	{
		_objectId = objectId;
		_chatType = messageType;
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
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SAY2.writeId(packet);
		
		packet.writeD(_objectId);
		packet.writeD(_chatType.getClientId());
		if (_charName != null)
		{
			packet.writeS(_charName);
		}
		else
		{
			packet.writeD(_charId);
		}
		packet.writeD(_npcString); // High Five NPCString ID
		if (_text != null)
		{
			packet.writeS(_text);
			if ((_charLevel > 0) && (_chatType == ChatType.WHISPER))
			{
				packet.writeC(_mask);
				if ((_mask & 0x10) == 0)
				{
					packet.writeC(_charLevel);
				}
			}
		}
		else if (_parameters != null)
		{
			for (String s : _parameters)
			{
				packet.writeS(s);
			}
		}
		
		// Rank
		final PlayerInstance player = World.getInstance().getPlayer(_objectId);
		if (player != null)
		{
			if ((player.getClan() != null) && ((_chatType == ChatType.CLAN) || (_chatType == ChatType.ALLIANCE)))
			{
				packet.writeC(0); // unknown clan byte
			}
			
			final int rank = RankManager.getInstance().getPlayerGlobalRank(player);
			if ((rank == 0) || (rank > 100))
			{
				packet.writeC(0);
			}
			else if (rank <= 10)
			{
				packet.writeC(1);
			}
			else if (rank <= 50)
			{
				packet.writeC(2);
			}
			else if (rank <= 100)
			{
				packet.writeC(3);
			}
			
			if (player.getClan() != null)
			{
				packet.writeC(player.getClan().getCastleId());
			}
			else
			{
				packet.writeC(0);
			}
		}
		else
		{
			packet.writeC(0);
		}
		
		return true;
	}
	
	@Override
	public void runImpl(PlayerInstance player)
	{
		if (player != null)
		{
			player.broadcastSnoop(_chatType, _charName, _text);
		}
	}
}
