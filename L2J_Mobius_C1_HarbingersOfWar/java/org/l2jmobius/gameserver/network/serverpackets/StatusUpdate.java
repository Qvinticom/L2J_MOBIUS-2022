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

public class StatusUpdate extends ServerBasePacket
{
	public static final int LEVEL = 1;
	public static final int EXP = 2;
	public static final int STR = 3;
	public static final int DEX = 4;
	public static final int CON = 5;
	public static final int INT = 6;
	public static final int WIT = 7;
	public static final int MEN = 8;
	public static final int CUR_HP = 9;
	public static final int MAX_HP = 10;
	public static final int CUR_MP = 11;
	public static final int MAX_MP = 12;
	public static final int SP = 13;
	public static final int CUR_LOAD = 14;
	public static final int MAX_LOAD = 15;
	public static final int P_ATK = 17;
	public static final int ATK_SPD = 18;
	public static final int P_DEF = 19;
	public static final int EVASION = 20;
	public static final int ACCURACY = 21;
	public static final int CRITICAL = 22;
	public static final int M_ATK = 23;
	public static final int CAST_SPD = 24;
	public static final int M_DEF = 25;
	public static final int PVP_FLAG = 26;
	public static final int KARMA = 27;
	
	private final int _objectId;
	private final List<Attribute> _attributes = new ArrayList<>();
	
	public StatusUpdate(int objectId)
	{
		_objectId = objectId;
	}
	
	public void addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
	}
	
	@Override
	public void writeImpl()
	{
		writeC(0x1A);
		writeD(_objectId);
		writeD(_attributes.size());
		for (Attribute att : _attributes)
		{
			writeD(att.id);
			writeD(att.value);
		}
	}
	
	class Attribute
	{
		public int id;
		public int value;
		
		Attribute(int id, int value)
		{
			this.id = id;
			this.value = value;
		}
	}
}
