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
import java.util.Collections;
import java.util.List;

import org.l2jmobius.commons.network.PacketWriter;
import org.l2jmobius.gameserver.enums.ChatType;
import org.l2jmobius.gameserver.model.actor.Npc;
import org.l2jmobius.gameserver.network.NpcStringId;
import org.l2jmobius.gameserver.network.NpcStringId.NSLocalisation;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * @author Kerberos
 */
public class NpcSay implements IClientOutgoingPacket
{
	private final int _objectId;
	private final ChatType _textType;
	private final int _npcId;
	private String _text;
	private final int _npcString;
	private List<String> _parameters;
	private String _lang;
	
	/**
	 * @param objectId
	 * @param messageType
	 * @param npcId
	 * @param text
	 */
	public NpcSay(int objectId, ChatType messageType, int npcId, String text)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = -1;
		_text = text;
	}
	
	public NpcSay(Npc npc, ChatType messageType, String text)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getTemplate().getDisplayId();
		_npcString = -1;
		_text = text;
	}
	
	public NpcSay(int objectId, ChatType messageType, int npcId, NpcStringId npcString)
	{
		_objectId = objectId;
		_textType = messageType;
		_npcId = 1000000 + npcId;
		_npcString = npcString.getId();
		_text = npcString.getText();
	}
	
	public NpcSay(Npc npc, ChatType messageType, NpcStringId npcString)
	{
		_objectId = npc.getObjectId();
		_textType = messageType;
		_npcId = 1000000 + npc.getTemplate().getDisplayId();
		_npcString = npcString.getId();
		_text = npcString.getText();
	}
	
	/**
	 * @param text the text to add as a parameter for this packet's message (replaces S1, S2 etc.)
	 * @return this NpcSay packet object
	 */
	public NpcSay addStringParameter(String text)
	{
		if (_parameters == null)
		{
			_parameters = new ArrayList<>();
		}
		_parameters.add(text);
		return this;
	}
	
	public void setLang(String lang)
	{
		_lang = lang;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.NPC_SAY.writeId(packet);
		packet.writeD(_objectId);
		packet.writeD(_textType.getClientId());
		packet.writeD(_npcId);
		if (_parameters != null)
		{
			for (int i = 0; i < _parameters.size(); i++)
			{
				_text = _text.replace("$s" + (i + 1), _parameters.get(i));
			}
		}
		// Localisation related.
		if (_lang != null)
		{
			final NpcStringId ns = NpcStringId.getNpcStringId(_npcString);
			if (ns != null)
			{
				final NSLocalisation nsl = ns.getLocalisation(_lang);
				if (nsl != null)
				{
					_text = nsl.getLocalisation(_parameters != null ? _parameters : Collections.emptyList());
				}
			}
		}
		packet.writeS(_text);
		return true;
	}
}