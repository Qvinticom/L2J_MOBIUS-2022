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
package com.l2jmobius.gameserver.network.clientpackets;

import com.l2jmobius.Config;
import com.l2jmobius.gameserver.data.sql.impl.CharNameTable;
import com.l2jmobius.gameserver.network.serverpackets.ExIsCharNameCreatable;
import com.l2jmobius.gameserver.util.Util;

/**
 * @author UnAfraid
 */
public class RequestCharacterNameCreatable extends L2GameClientPacket
{
	private String _name;
	private int result;
	
	public static int CHARACTER_CREATE_FAILED = 1;
	public static int NAME_ALREADY_EXISTS = 2;
	public static int INVALID_LENGTH = 3;
	public static int INVALID_NAME = 4;
	public static int CANNOT_CREATE_SERVER = 5;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final int charId = CharNameTable.getInstance().getIdByName(_name);
		
		if (!Util.isAlphaNumeric(_name) || !isValidName(_name))
		{
			result = INVALID_NAME;
		}
		else if (charId > 0)
		{
			result = NAME_ALREADY_EXISTS;
		}
		else if (_name.length() > 16)
		{
			result = INVALID_LENGTH;
		}
		else
		{
			result = -1;
		}
		
		sendPacket(new ExIsCharNameCreatable(result));
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:B0 RequestCharacterNameCreatable";
	}
	
	private boolean isValidName(String text)
	{
		return Config.CHARNAME_TEMPLATE_PATTERN.matcher(text).matches();
	}
}