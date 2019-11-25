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

import java.util.ArrayList;
import java.util.List;

import org.l2jmobius.gameserver.templates.CharTemplate;

public class CharTemplates extends ServerBasePacket
{
	private final List<CharTemplate> _chars = new ArrayList<>();
	
	public void addChar(CharTemplate template)
	{
		_chars.add(template);
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x23);
		writeD(_chars.size());
		for (CharTemplate charTemplate : _chars)
		{
			writeD(charTemplate.getRaceId());
			writeD(charTemplate.getClassId());
			writeD(70);
			writeD(charTemplate.getStr());
			writeD(10);
			writeD(70);
			writeD(charTemplate.getDex());
			writeD(10);
			writeD(70);
			writeD(charTemplate.getCon());
			writeD(10);
			writeD(70);
			writeD(charTemplate.getInt());
			writeD(10);
			writeD(70);
			writeD(charTemplate.getWit());
			writeD(10);
			writeD(70);
			writeD(charTemplate.getMen());
			writeD(10);
		}
	}
}
