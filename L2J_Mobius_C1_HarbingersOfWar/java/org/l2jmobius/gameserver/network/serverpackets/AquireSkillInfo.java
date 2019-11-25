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

public class AquireSkillInfo extends ServerBasePacket
{
	private final List<Req> _reqs = new ArrayList<>();
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
	public void writeImpl()
	{
		writeC(0xA4);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeD(_reqs.size());
		for (Req req : _reqs)
		{
			writeD(req.type);
			writeD(req.id);
			writeD(req.count);
			writeD(req.unk);
		}
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
