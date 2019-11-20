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

public class SkillList extends ServerBasePacket
{
	private static final String _S__6D_SKILLLIST = "[S] 6D SkillList";
	private final Vector<Skill> _skills = new Vector<>();
	
	public void addSkill(int id, int level, boolean passive)
	{
		_skills.add(new Skill(id, level, passive));
	}
	
	@Override
	public byte[] getContent()
	{
		writeC(109);
		writeD(_skills.size());
		for (int i = 0; i < _skills.size(); ++i)
		{
			Skill temp = _skills.get(i);
			if (temp.passive)
			{
				writeD(1);
			}
			else
			{
				writeD(0);
			}
			writeD(temp.level);
			writeD(temp.id);
		}
		return getBytes();
	}
	
	@Override
	public String getType()
	{
		return _S__6D_SKILLLIST;
	}
	
	class Skill
	{
		public int id;
		public int level;
		public boolean passive;
		
		Skill(int id, int level, boolean passive)
		{
			this.id = id;
			this.level = level;
			this.passive = passive;
		}
	}
	
}
