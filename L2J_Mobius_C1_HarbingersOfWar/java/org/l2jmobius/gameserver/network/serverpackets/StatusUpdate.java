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

public class StatusUpdate extends ServerBasePacket
{
	private static final String _S__1A_STATUSUPDATE = "[S] 1A StatusUpdate";
	public static int LEVEL = 1;
	public static int EXP = 2;
	public static int STR = 3;
	public static int DEX = 4;
	public static int CON = 5;
	public static int INT = 6;
	public static int WIT = 7;
	public static int MEN = 8;
	public static int CUR_HP = 9;
	public static int MAX_HP = 10;
	public static int CUR_MP = 11;
	public static int MAX_MP = 12;
	public static int SP = 13;
	public static int CUR_LOAD = 14;
	public static int MAX_LOAD = 15;
	public static int P_ATK = 17;
	public static int ATK_SPD = 18;
	public static int P_DEF = 19;
	public static int EVASION = 20;
	public static int ACCURACY = 21;
	public static int CRITICAL = 22;
	public static int M_ATK = 23;
	public static int CAST_SPD = 24;
	public static int M_DEF = 25;
	public static int PVP_FLAG = 26;
	public static int KARMA = 27;
	private final int _objectId;
	private final Vector<Attribute> _attributes = new Vector<>();
	
	public StatusUpdate(int objectId)
	{
		_objectId = objectId;
	}
	
	public void addAttribute(int id, int level)
	{
		_attributes.add(new Attribute(id, level));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(26);
		writeD(_objectId);
		writeD(_attributes.size());
		for (int i = 0; i < _attributes.size(); ++i)
		{
			Attribute temp = _attributes.get(i);
			writeD(temp.id);
			writeD(temp.value);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__1A_STATUSUPDATE;
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
