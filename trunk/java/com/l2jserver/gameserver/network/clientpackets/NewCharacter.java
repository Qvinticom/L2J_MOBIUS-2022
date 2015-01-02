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
package com.l2jserver.gameserver.network.clientpackets;

import com.l2jserver.Config;
import com.l2jserver.gameserver.datatables.CharTemplateTable;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.network.serverpackets.NewCharacterSuccess;

/**
 * @author Zoey76
 */
public final class NewCharacter extends L2GameClientPacket
{
	private static final String _C__13_NEWCHARACTER = "[C] 13 NewCharacter";
	
	@Override
	protected void readImpl()
	{
		
	}
	
	@Override
	protected void runImpl()
	{
		if (Config.DEBUG)
		{
			_log.fine(_C__13_NEWCHARACTER);
		}
		
		final NewCharacterSuccess ct = new NewCharacterSuccess();
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.FIGHTER)); // Human Figther
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.MAGE)); // Human Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ELVEN_FIGHTER)); // Elven Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ELVEN_MAGE)); // Elven Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DARK_FIGHTER)); // Dark Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DARK_MAGE)); // Dark Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ORC_FIGHTER)); // Orc Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ORC_MAGE)); // Orc Mystic
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.DWARVEN_FIGHTER)); // Dwarf Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.MALE_SOLDIER)); // Male Kamael Soldier
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.FEMALE_SOLDIER)); // Female Kamael Soldier
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ERTHEIA_FIGHTER)); // Ertheia Fighter
		ct.addChar(CharTemplateTable.getInstance().getTemplate(ClassId.ERTHEIA_WIZARD)); // Ertheia Wizard
		sendPacket(ct);
	}
	
	@Override
	public String getType()
	{
		return _C__13_NEWCHARACTER;
	}
}
