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
import org.l2jmobius.gameserver.model.Macro;
import org.l2jmobius.gameserver.network.OutgoingPackets;

/**
 * packet type id 0xe7 sample e7 d // unknown change of Macro edit,add,delete c // unknown c //count of Macros c // unknown d // id S // macro name S // desc S // acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdhcdSSScc (ccdcS)
 */
public class SendMacroList implements IClientOutgoingPacket
{
	private final int _rev;
	private final int _count;
	private final Macro _macro;
	
	public SendMacroList(int rev, int count, Macro macro)
	{
		_rev = rev;
		_count = count;
		_macro = macro;
	}
	
	@Override
	public boolean write(PacketWriter packet)
	{
		OutgoingPackets.SEND_MACRO_LIST.writeId(packet);
		packet.writeD(_rev); // macro change revision (changes after each macro edition)
		packet.writeC(0); // unknown
		packet.writeC(_count); // count of Macros
		packet.writeC(_macro != null ? 1 : 0); // unknown
		if (_macro != null)
		{
			packet.writeD(_macro.id); // Macro ID
			packet.writeS(_macro.name); // Macro Name
			packet.writeS(_macro.descr); // Desc
			packet.writeS(_macro.acronym); // acronym
			packet.writeC(_macro.icon); // icon
			packet.writeC(_macro.commands.length); // count
			for (int i = 0; i < _macro.commands.length; i++)
			{
				final Macro.MacroCmd cmd = _macro.commands[i];
				packet.writeC(i + 1); // i of count
				packet.writeC(cmd.type); // type 1 = skill, 3 = action, 4 = shortcut
				packet.writeD(cmd.d1); // skill id
				packet.writeC(cmd.d2); // shortcut id
				packet.writeS(cmd.cmd); // command name
			}
		}
		// writeD(1); //unknown change of Macro edit,add,delete
		// packet.writeC(0); //unknown
		// packet.writeC(1); //count of Macros
		// packet.writeC(1); //unknown
		//
		// writeD(1430); //Macro ID
		// writeS("Admin"); //Macro Name
		// writeS("Admin Command"); //Desc
		// writeS("ADM"); //acronym
		// packet.writeC(0); //icon
		// packet.writeC(2); //count
		//
		// packet.writeC(1); //i of count
		// packet.writeC(3); //type 1 = skill, 3 = action, 4 = shortcut
		// writeD(0); // skill id
		// packet.writeC(0); // shortcut id
		// writeS("/loc"); // command name
		//
		// packet.writeC(2); //i of count
		// packet.writeC(3); //type 1 = skill, 3 = action, 4 = shortcut
		// writeD(0); // skill id
		// packet.writeC(0); // shortcut id
		// writeS("//admin"); // command name
		return true;
	}
}
