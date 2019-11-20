/*
 * This file is part of the L2J Mobius project.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.l2jmobius.gameserver.network.serverpackets;

public class ShortCutRegister extends ServerBasePacket
{
	private static final String _S__56_SHORTCUTREGISTER = "[S] 56 ShortCutRegister";
	public int _slot;
	public int _type;
	public int _typeId;
	public int _level;
	public int _dat2;
	
	public ShortCutRegister(int slot, int type, int typeId, int level, int dat2)
	{
		_slot = slot;
		_type = type;
		_typeId = typeId;
		_level = level;
		_dat2 = dat2;
	}
	
	public ShortCutRegister(int slot, int type, int typeId, int dat2)
	{
		this(slot, type, typeId, -1, dat2);
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(86);
		writeD(_type);
		writeD(_slot);
		writeD(_typeId);
		if (_level > -1)
		{
			writeD(_level);
		}
		writeD(_dat2);
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__56_SHORTCUTREGISTER;
	}
}
