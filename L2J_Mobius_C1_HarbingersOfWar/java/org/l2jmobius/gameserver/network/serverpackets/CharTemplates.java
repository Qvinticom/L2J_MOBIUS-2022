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

import java.util.Vector;

import org.l2jmobius.gameserver.templates.CharTemplate;

public class CharTemplates extends ServerBasePacket
{
	private static final String _S__23_CHARTEMPLATES = "[S] 23 CharTemplates";
	private final Vector<CharTemplate> _chars = new Vector<>();
	
	public void addChar(CharTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	public byte[] getContent()
	{
		_bao.write(35);
		writeD(_chars.size());
		for (int i = 0; i < _chars.size(); ++i)
		{
			final CharTemplate temp = _chars.get(i);
			writeD(temp.getRaceId());
			writeD(temp.getClassId());
			writeD(70);
			writeD(temp.getStr());
			writeD(10);
			writeD(70);
			writeD(temp.getDex());
			writeD(10);
			writeD(70);
			writeD(temp.getCon());
			writeD(10);
			writeD(70);
			writeD(temp.getInt());
			writeD(10);
			writeD(70);
			writeD(temp.getWit());
			writeD(10);
			writeD(70);
			writeD(temp.getMen());
			writeD(10);
		}
		return _bao.toByteArray();
	}
	
	@Override
	public String getType()
	{
		return _S__23_CHARTEMPLATES;
	}
}
