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

public class AquireSkillInfo extends ServerBasePacket
{
	private static final String _S__A4_AQUIRESKILLINFO = "[S] A4 AquireSkillInfo";
	private final Vector<Req> _reqs = new Vector<>();
	private final int _id;
	private final int _level;
	private final int _spCost;
	
	public AquireSkillInfo(int id, int level, int spCost)
	{
		_id = id;
		_level = level;
		_spCost = spCost;
	}
	
	public void addRequirement(int type, int id, int count, int unk)
	{
		_reqs.add(new Req(type, id, count, unk));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(164);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_reqs.size());
		for (int i = 0; i < _reqs.size(); ++i)
		{
			Req temp = _reqs.get(i);
			writeD(temp.type);
			writeD(temp.id);
			writeD(temp.count);
			writeD(temp.unk);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__A4_AQUIRESKILLINFO;
	}
	
	class Req
	{
		public int id;
		public int count;
		public int type;
		public int unk;
		
		Req(int type, int id, int count, int unk)
		{
			this.id = id;
			this.type = type;
			this.count = count;
			this.unk = unk;
		}
	}
	
}
