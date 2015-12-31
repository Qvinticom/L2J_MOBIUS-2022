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
package com.l2jmobius.gameserver.network.serverpackets;

import com.l2jmobius.gameserver.enums.MacroUpdateType;
import com.l2jmobius.gameserver.model.Macro;
import com.l2jmobius.gameserver.model.MacroCmd;

public class SendMacroList extends L2GameServerPacket
{
	private final int _count;
	private final Macro _macro;
	private final MacroUpdateType _updateType;
	
	public SendMacroList(int count, Macro macro, MacroUpdateType updateType)
	{
		_count = count;
		_macro = macro;
		_updateType = updateType;
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0xE8);
		
		writeC(_updateType.getId());
		writeD(_updateType != MacroUpdateType.LIST ? _macro.getId() : 0x00); // modified, created or deleted macro's id
		writeC(_count); // count of Macros
		writeC(_macro != null ? 1 : 0); // unknown
		
		if ((_macro != null) && (_updateType != MacroUpdateType.DELETE))
		{
			writeD(_macro.getId()); // Macro ID
			writeS(_macro.getName()); // Macro Name
			writeS(_macro.getDescr()); // Desc
			writeS(_macro.getAcronym()); // acronym
			writeC(_macro.getIcon()); // icon
			
			writeC(_macro.getCommands().size()); // count
			
			int i = 1;
			for (MacroCmd cmd : _macro.getCommands())
			{
				writeC(i++); // command count
				writeC(cmd.getType().ordinal()); // type 1 = skill, 3 = action, 4 = shortcut
				writeD(cmd.getD1()); // skill id
				writeC(cmd.getD2()); // shortcut id
				writeS(cmd.getCmd()); // command name
			}
		}
	}
}
